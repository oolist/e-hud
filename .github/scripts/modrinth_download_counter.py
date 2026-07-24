"""Update a Discord voice-channel name with total Modrinth downloads."""

from __future__ import annotations

import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request


MODRINTH_API = "https://api.modrinth.com/v2"
DISCORD_API = "https://discord.com/api/v10"
USER_AGENT = "oolist-modrinth-download-counter/1.0 (https://github.com/oolist/e-hud)"


def request_json(
    url: str,
    *,
    method: str = "GET",
    headers: dict[str, str] | None = None,
    body: dict[str, object] | None = None,
) -> dict[str, object]:
    request_headers = {"User-Agent": USER_AGENT}
    if headers:
        request_headers.update(headers)

    data = None
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        request_headers["Content-Type"] = "application/json"

    request = urllib.request.Request(
        url,
        data=data,
        headers=request_headers,
        method=method,
    )
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            return json.load(response)
    except urllib.error.HTTPError as error:
        details = error.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"{method} {url} failed ({error.code}): {details}") from error
    except urllib.error.URLError as error:
        raise RuntimeError(f"{method} {url} failed: {error.reason}") from error


def parse_projects(value: str) -> list[str]:
    projects = [project.strip() for project in value.split(",") if project.strip()]
    if not projects:
        raise ValueError("MODRINTH_PROJECTS must contain at least one project ID or slug")
    return projects


def format_count(value: int) -> str:
    return f"{value:,}"


def build_channel_name(downloads: int) -> str:
    return f"📥 Oolist Downloads: {format_count(downloads)}"


def get_total_downloads(projects: list[str], modrinth_token: str = "") -> int:
    headers = {}
    if modrinth_token:
        headers["Authorization"] = modrinth_token

    total = 0
    for project in projects:
        encoded = urllib.parse.quote(project, safe="")
        data = request_json(f"{MODRINTH_API}/project/{encoded}", headers=headers)
        downloads = data.get("downloads")
        if not isinstance(downloads, int):
            raise RuntimeError(f"Modrinth project {project!r} returned no numeric download count")
        total += downloads
    return total


def update_discord_channel(channel_id: str, bot_token: str, desired_name: str) -> bool:
    headers = {"Authorization": f"Bot {bot_token}"}
    channel_url = f"{DISCORD_API}/channels/{channel_id}"
    channel = request_json(channel_url, headers=headers)
    current_name = channel.get("name")

    if current_name == desired_name:
        print(f"Counter already up to date: {desired_name}")
        return False

    request_json(
        channel_url,
        method="PATCH",
        headers=headers,
        body={"name": desired_name},
    )
    print(f"Updated counter: {desired_name}")
    return True


def main() -> int:
    bot_token = os.getenv("DISCORD_COUNTER_BOT_TOKEN", "").strip()
    channel_id = os.getenv("DISCORD_DOWNLOAD_CHANNEL_ID", "").strip()
    projects_value = os.getenv(
        "MODRINTH_PROJECTS",
        "animal-hud,break-to-teleport,e-hud,oolist-optimize",
    )
    modrinth_token = os.getenv("MODRINTH_TOKEN", "").strip()

    if not bot_token or not channel_id:
        print(
            "Modrinth counter is not configured yet; "
            "set DISCORD_COUNTER_BOT_TOKEN and DISCORD_DOWNLOAD_CHANNEL_ID."
        )
        return 0

    try:
        projects = parse_projects(projects_value)
        downloads = get_total_downloads(projects, modrinth_token)
        update_discord_channel(channel_id, bot_token, build_channel_name(downloads))
    except (RuntimeError, ValueError) as error:
        print(f"Modrinth counter failed: {error}", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
