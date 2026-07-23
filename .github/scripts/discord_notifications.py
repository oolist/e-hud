#!/usr/bin/env python3
"""Post new Oolist YouTube uploads and E HUD Modrinth releases to Discord."""

from __future__ import annotations

import json
import os
import subprocess
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path
from typing import Any


YOUTUBE_CHANNEL_ID = "UCA84v4QhTXZJAdov2jX_E3g"
YOUTUBE_FEED = (
    f"https://www.youtube.com/feeds/videos.xml?channel_id={YOUTUBE_CHANNEL_ID}"
)
MODRINTH_VERSIONS = "https://api.modrinth.com/v2/project/ratzt04c/version"
MODRINTH_PROJECT = "https://modrinth.com/mod/e-hud"
STATE_FILE = Path(".github/notification-state.json")
USER_AGENT = "E-HUD-Discord-Notifier/1.0 (https://github.com/oolist/e-hud)"


def require_env(name: str) -> str:
    value = os.environ.get(name, "").strip()
    if not value:
        raise RuntimeError(f"Required setting {name} is missing")
    return value


def request_bytes(url: str) -> bytes:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.read()


def request_json(url: str) -> Any:
    return json.loads(request_bytes(url).decode("utf-8"))


def post_webhook(webhook: str, payload: dict[str, Any], label: str) -> None:
    body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(
        webhook,
        data=body,
        headers={
            "Content-Type": "application/json",
            "User-Agent": USER_AGENT,
        },
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        if response.status not in (200, 204):
            raise RuntimeError(f"{label} webhook returned HTTP {response.status}")
    print(f"Posted {label} notification")


def load_state() -> dict[str, Any]:
    if not STATE_FILE.exists():
        return {}
    try:
        state = json.loads(STATE_FILE.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError):
        return {}
    return state if isinstance(state, dict) else {}


def save_state(state: dict[str, Any]) -> None:
    STATE_FILE.parent.mkdir(parents=True, exist_ok=True)
    STATE_FILE.write_text(
        json.dumps(state, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )


def parse_youtube_feed() -> list[dict[str, str]]:
    root = ET.fromstring(request_bytes(YOUTUBE_FEED))
    atom = "{http://www.w3.org/2005/Atom}"
    yt = "{http://www.youtube.com/xml/schemas/2015}"
    media = "{http://search.yahoo.com/mrss/}"
    uploads: list[dict[str, str]] = []

    for entry in root.findall(f"{atom}entry"):
        video_id = (entry.findtext(f"{yt}videoId") or "").strip()
        title = (entry.findtext(f"{atom}title") or "New upload").strip()
        published = (entry.findtext(f"{atom}published") or "").strip()
        description = ""
        group = entry.find(f"{media}group")
        if group is not None:
            description = (group.findtext(f"{media}description") or "").strip()
        if video_id:
            uploads.append(
                {
                    "id": video_id,
                    "title": title,
                    "published": published,
                    "description": description,
                    "url": f"https://www.youtube.com/watch?v={video_id}",
                }
            )
    return uploads


def video_metadata(url: str) -> dict[str, Any]:
    try:
        process = subprocess.run(
            [
                "yt-dlp",
                "--dump-single-json",
                "--skip-download",
                "--no-warnings",
                url,
            ],
            check=True,
            capture_output=True,
            text=True,
            timeout=90,
        )
        result = json.loads(process.stdout)
        return result if isinstance(result, dict) else {}
    except (subprocess.SubprocessError, json.JSONDecodeError, OSError):
        return {}


def is_short(metadata: dict[str, Any]) -> bool:
    duration = metadata.get("duration")
    width = metadata.get("width")
    height = metadata.get("height")
    try:
        return float(duration) <= 180 and int(height) >= int(width)
    except (TypeError, ValueError):
        return False


def discord_timestamp(value: str) -> str | None:
    if not value:
        return None
    try:
        parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
        return parsed.astimezone(timezone.utc).isoformat().replace("+00:00", "Z")
    except ValueError:
        return None


def youtube_payload(upload: dict[str, str], role_id: str, short: bool) -> dict[str, Any]:
    kind = "YouTube Short" if short else "YouTube Video"
    colour = 0xFF7A00 if short else 0x39FF88
    description = upload.get("description", "").strip()
    if len(description) > 900:
        description = description[:897].rstrip() + "..."

    embed: dict[str, Any] = {
        "title": upload["title"][:256],
        "url": upload["url"],
        "description": description or f"A new {kind.lower()} is live!",
        "color": colour,
        "author": {
            "name": f"New {kind} from oolist",
            "url": "https://www.youtube.com/@ool1st",
        },
        "thumbnail": {
            "url": f"https://i.ytimg.com/vi/{upload['id']}/maxresdefault.jpg"
        },
        "footer": {"text": "Oolist YouTube notifications"},
    }
    timestamp = discord_timestamp(upload.get("published", ""))
    if timestamp:
        embed["timestamp"] = timestamp

    return {
        "username": "Oolist Uploads",
        "content": f"<@&{role_id}> New {kind.lower()}!",
        "allowed_mentions": {"parse": [], "roles": [role_id]},
        "embeds": [embed],
    }


def newest_file(version: dict[str, Any]) -> dict[str, Any] | None:
    files = version.get("files")
    if not isinstance(files, list) or not files:
        return None
    primary = next((item for item in files if item.get("primary")), None)
    return primary or files[0]


def clean_changelog(value: Any) -> str:
    changelog = str(value or "").strip()
    if not changelog:
        return "*No changelog was supplied for this release.*"
    if len(changelog) > 3400:
        return changelog[:3397].rstrip() + "..."
    return changelog


def modrinth_payload(version: dict[str, Any], role_id: str) -> dict[str, Any]:
    version_id = str(version.get("id", ""))
    version_number = str(version.get("version_number") or "Unknown")
    version_name = str(version.get("name") or version_number)
    release_type = str(version.get("version_type") or "release").capitalize()
    game_versions = ", ".join(map(str, version.get("game_versions") or []))
    if len(game_versions) > 1000:
        game_versions = game_versions[:997].rstrip() + "..."

    page_url = f"{MODRINTH_PROJECT}/version/{version_id}"
    file = newest_file(version)
    download_url = str(file.get("url")) if file else page_url
    filename = str(file.get("filename") or "Download")
    if len(filename) > 100:
        filename = filename[:97] + "..."

    embed: dict[str, Any] = {
        "title": f"E HUD {version_name}"[:256],
        "url": page_url,
        "description": clean_changelog(version.get("changelog")),
        "color": 0x39FF88,
        "fields": [
            {"name": "Version", "value": version_number[:1024], "inline": True},
            {"name": "Release type", "value": release_type[:1024], "inline": True},
            {
                "name": "Minecraft versions",
                "value": game_versions or "Not specified",
                "inline": False,
            },
            {
                "name": "Download",
                "value": f"[{filename}]({download_url})"[:1024],
                "inline": False,
            },
        ],
        "footer": {"text": "E HUD on Modrinth"},
    }
    timestamp = discord_timestamp(str(version.get("date_published") or ""))
    if timestamp:
        embed["timestamp"] = timestamp

    return {
        "username": "E HUD Releases",
        "content": f"<@&{role_id}> A new E HUD version is available on Modrinth!",
        "allowed_mentions": {"parse": [], "roles": [role_id]},
        "embeds": [embed],
    }


def process_youtube(state: dict[str, Any]) -> None:
    uploads = parse_youtube_feed()
    if not uploads:
        print("YouTube feed contained no uploads")
        return

    current_ids = [upload["id"] for upload in uploads]
    known_ids = state.get("youtube_ids")
    if not isinstance(known_ids, list):
        state["youtube_ids"] = current_ids[:50]
        print("Initialized YouTube state without posting old uploads")
        return

    known = {str(item) for item in known_ids}
    unseen = [upload for upload in uploads if upload["id"] not in known]
    for upload in reversed(unseen[-10:]):
        metadata = video_metadata(upload["url"])
        short = is_short(metadata)
        if short:
            webhook = require_env("DISCORD_SHORTS_WEBHOOK")
            role_id = require_env("SHORTS_ROLE_ID")
        else:
            webhook = require_env("DISCORD_YOUTUBE_WEBHOOK")
            role_id = require_env("YOUTUBE_ROLE_ID")
        post_webhook(
            webhook,
            youtube_payload(upload, role_id, short),
            "YouTube Short" if short else "YouTube video",
        )

    state["youtube_ids"] = current_ids[:50]
    if not unseen:
        print("No new YouTube uploads")


def process_modrinth(state: dict[str, Any]) -> None:
    versions = request_json(MODRINTH_VERSIONS)
    if not isinstance(versions, list):
        raise RuntimeError("Unexpected response from Modrinth")

    current_ids = [str(version.get("id")) for version in versions if version.get("id")]
    known_ids = state.get("modrinth_ids")
    if not isinstance(known_ids, list):
        state["modrinth_ids"] = current_ids[:200]
        print("Initialized Modrinth state without posting old releases")
        return

    known = {str(item) for item in known_ids}
    unseen = [
        version
        for version in versions
        if version.get("id") and str(version["id"]) not in known
    ]
    unseen.sort(key=lambda item: str(item.get("date_published") or ""))

    webhook = require_env("DISCORD_MODRINTH_WEBHOOK") if unseen else ""
    role_id = require_env("MODRINTH_ROLE_ID") if unseen else ""
    for version in unseen[-10:]:
        post_webhook(
            webhook,
            modrinth_payload(version, role_id),
            f"Modrinth {version.get('version_number', 'release')}",
        )

    state["modrinth_ids"] = current_ids[:200]
    if not unseen:
        print("No new Modrinth releases")


def main() -> int:
    state = load_state()
    try:
        process_youtube(state)
        process_modrinth(state)
        save_state(state)
    except (RuntimeError, urllib.error.URLError, ET.ParseError) as error:
        print(f"Notification check failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
