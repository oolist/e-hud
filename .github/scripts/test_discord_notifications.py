import importlib.util
import os
from pathlib import Path
import unittest
from unittest.mock import patch


SCRIPT = Path(__file__).with_name("discord_notifications.py")
SPEC = importlib.util.spec_from_file_location("discord_notifications", SCRIPT)
notifications = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(notifications)


def version(
    version_id: str,
    release: str,
    minecraft: str,
    changelog: str = "Full release changelog",
) -> dict:
    return {
        "id": version_id,
        "version_number": f"{release}+mc{minecraft}",
        "name": f"E HUD {release} for Minecraft {minecraft}",
        "version_type": "alpha",
        "game_versions": [minecraft],
        "changelog": changelog,
        "date_published": "2026-07-29T12:00:00Z",
        "files": [],
    }


class DiscordNotificationTests(unittest.TestCase):
    def test_base_release_removes_minecraft_build_suffix(self):
        self.assertEqual(
            notifications.base_modrinth_version(
                version("one", "0.1.2-alpha", "1.21.11")
            ),
            "0.1.2-alpha",
        )

    def test_grouped_payload_contains_one_changelog_and_all_versions(self):
        builds = [
            version("one", "0.1.2-alpha", "1.21.9"),
            version("two", "0.1.2-alpha", "1.21.11"),
            version("three", "0.1.2-alpha", "1.21.10"),
        ]

        payload = notifications.modrinth_payload(
            "0.1.2-alpha", builds, "123456"
        )

        self.assertEqual(payload["content"].count("<@&123456>"), 1)
        self.assertEqual(
            payload["embeds"][0]["description"], "Full release changelog"
        )
        self.assertEqual(
            payload["embeds"][0]["fields"][2]["value"],
            "1.21.9, 1.21.10, 1.21.11",
        )
        self.assertEqual(payload["embeds"][0]["fields"][3]["value"], "3")

    @patch.dict(
        os.environ,
        {
            "DISCORD_MODRINTH_WEBHOOK": "https://discord.invalid/webhook",
            "MODRINTH_ROLE_ID": "123456",
        },
        clear=False,
    )
    def test_one_release_with_many_builds_posts_once(self):
        builds = [
            version(str(index), "0.1.2-alpha", minecraft)
            for index, minecraft in enumerate(
                ["1.21.1", "1.21.4", "1.21.6", "1.21.10", "1.21.11"]
            )
        ]
        state = {
            "modrinth_ids": ["old-id"],
            "modrinth_releases": ["0.1.1-alpha"],
        }

        with (
            patch.object(notifications, "request_json", return_value=builds),
            patch.object(notifications, "post_webhook") as post,
        ):
            notifications.process_modrinth(state)

        post.assert_called_once()
        self.assertEqual(state["modrinth_releases"], ["0.1.2-alpha"])

    @patch.dict(
        os.environ,
        {
            "DISCORD_MODRINTH_WEBHOOK": "https://discord.invalid/webhook",
            "MODRINTH_ROLE_ID": "123456",
        },
        clear=False,
    )
    def test_late_build_for_known_release_does_not_reping(self):
        builds = [
            version("one", "0.1.2-alpha", "1.21.11"),
            version("two", "0.1.2-alpha", "1.21.10"),
        ]
        state = {
            "modrinth_ids": ["one"],
            "modrinth_releases": ["0.1.2-alpha"],
        }

        with (
            patch.object(notifications, "request_json", return_value=builds),
            patch.object(notifications, "post_webhook") as post,
        ):
            notifications.process_modrinth(state)

        post.assert_not_called()

    def test_state_migration_does_not_repost_existing_releases(self):
        builds = [version("one", "0.1.2-alpha", "1.21.11")]
        state = {"modrinth_ids": ["one"]}

        with (
            patch.object(notifications, "request_json", return_value=builds),
            patch.object(notifications, "post_webhook") as post,
        ):
            notifications.process_modrinth(state)

        post.assert_not_called()
        self.assertEqual(state["modrinth_releases"], ["0.1.2-alpha"])


if __name__ == "__main__":
    unittest.main()
