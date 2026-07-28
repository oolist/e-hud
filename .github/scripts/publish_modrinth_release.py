#!/usr/bin/env python3
"""Publish a tested E HUD GitHub release to Modrinth.

The script is intentionally idempotent: versions that already exist are skipped,
so a failed workflow can be safely rerun without creating duplicates.
"""

from __future__ import annotations

import argparse
import json
import mimetypes
import os
import re
import secrets
import sys
import urllib.error
import urllib.request
from pathlib import Path


API_ROOT = "https://api.modrinth.com/v2"
PROJECT = "e-hud"
USER_AGENT = "oolist/e-hud GitHub release publisher"


def request_json(
    method: str,
    path: str,
    token: str,
    *,
    body: bytes | None = None,
    content_type: str | None = None,
) -> object:
    headers = {
        "Authorization": token,
        "User-Agent": USER_AGENT,
        "Accept": "application/json",
    }
    if content_type:
        headers["Content-Type"] = content_type
    request = urllib.request.Request(
        f"{API_ROOT}{path}", data=body, headers=headers, method=method
    )
    try:
        with urllib.request.urlopen(request, timeout=180) as response:
            payload = response.read()
    except urllib.error.HTTPError as error:
        details = error.read().decode("utf-8", errors="replace")
        raise RuntimeError(
            f"Modrinth returned HTTP {error.code} for {method} {path}: {details}"
        ) from error
    return json.loads(payload) if payload else {}


def multipart(metadata: dict[str, object], filename: str, file_bytes: bytes) -> tuple[bytes, str]:
    boundary = f"----ehud-{secrets.token_hex(16)}"
    chunks: list[bytes] = []

    def add(value: bytes) -> None:
        chunks.append(value)

    add(f"--{boundary}\r\n".encode())
    add(b'Content-Disposition: form-data; name="data"\r\n')
    add(b"Content-Type: application/json\r\n\r\n")
    add(json.dumps(metadata, separators=(",", ":")).encode("utf-8"))
    add(b"\r\n")

    content_type = mimetypes.guess_type(filename)[0] or "application/java-archive"
    add(f"--{boundary}\r\n".encode())
    add(
        (
            f'Content-Disposition: form-data; name="{filename}"; '
            f'filename="{filename}"\r\n'
        ).encode()
    )
    add(f"Content-Type: {content_type}\r\n\r\n".encode())
    add(file_bytes)
    add(b"\r\n")
    add(f"--{boundary}--\r\n".encode())
    return b"".join(chunks), f"multipart/form-data; boundary={boundary}"


def minecraft_version(filename: str, release_version: str) -> str:
    pattern = re.compile(
        rf"^e-hud-(?P<minecraft>.+)-{re.escape(release_version)}\.jar$"
    )
    match = pattern.match(filename)
    if not match:
        raise ValueError(
            f"Unexpected release filename {filename!r}; expected "
            f"e-hud-<minecraft>-{release_version}.jar"
        )
    return match.group("minecraft")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--version", required=True)
    parser.add_argument("--artifacts", type=Path, required=True)
    parser.add_argument("--changelog", type=Path, required=True)
    arguments = parser.parse_args()

    token = os.environ.get("MODRINTH_TOKEN", "").strip()
    if not token:
        raise RuntimeError("MODRINTH_TOKEN is not configured.")

    jars = sorted(arguments.artifacts.glob(f"e-hud-*-{arguments.version}.jar"))
    if len(jars) != 14:
        raise RuntimeError(f"Expected 14 release jars, found {len(jars)}.")
    changelog = arguments.changelog.read_text(encoding="utf-8")

    project = request_json("GET", f"/project/{PROJECT}", token)
    if not isinstance(project, dict) or not project.get("id"):
        raise RuntimeError("Could not resolve the E HUD Modrinth project ID.")
    project_id = str(project["id"])
    existing = request_json("GET", f"/project/{project_id}/version", token)
    existing_numbers = {
        item["version_number"]
        for item in existing
        if isinstance(item, dict) and "version_number" in item
    }

    published = 0
    skipped = 0
    for jar in jars:
        game_version = minecraft_version(jar.name, arguments.version)
        version_number = f"{arguments.version}+mc{game_version}"
        if version_number in existing_numbers:
            print(f"Already published: {version_number}")
            skipped += 1
            continue

        metadata: dict[str, object] = {
            "name": f"E HUD {arguments.version} for Minecraft {game_version}",
            "version_number": version_number,
            "changelog": changelog,
            "dependencies": [],
            "game_versions": [game_version],
            "version_type": "alpha",
            "loaders": ["fabric"],
            "featured": False,
            "project_id": project_id,
            "file_parts": [jar.name],
            "primary_file": jar.name,
            "status": "listed",
        }
        body, content_type = multipart(metadata, jar.name, jar.read_bytes())
        response = request_json(
            "POST", "/version", token, body=body, content_type=content_type
        )
        print(f"Published {version_number}: {response.get('id', 'unknown id')}")
        existing_numbers.add(version_number)
        published += 1

    print(f"Modrinth publishing complete: {published} published, {skipped} skipped.")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:
        print(f"error: {error}", file=sys.stderr)
        raise SystemExit(1)
