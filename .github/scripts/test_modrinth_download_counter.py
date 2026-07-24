import importlib.util
from pathlib import Path
import unittest


SCRIPT = Path(__file__).with_name("modrinth_download_counter.py")
SPEC = importlib.util.spec_from_file_location("modrinth_download_counter", SCRIPT)
counter = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(counter)


class CounterTests(unittest.TestCase):
    def test_parse_projects(self):
        self.assertEqual(counter.parse_projects("e-hud, animal-hud "), ["e-hud", "animal-hud"])

    def test_parse_projects_rejects_empty_input(self):
        with self.assertRaises(ValueError):
            counter.parse_projects(" , ")

    def test_channel_name(self):
        self.assertEqual(
            counter.build_channel_name(1234567),
            "📥 Oolist Downloads: 1,234,567",
        )


if __name__ == "__main__":
    unittest.main()
