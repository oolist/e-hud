# Changelog

## 0.1.2-alpha

### Clear Sight

- Fixed the target HUD showing Air when the crosshair is not pointing at a block or entity.
- Replaced the technical block-property count with useful, readable block-state details.
- Grass blocks now show `With snow` or `Without snow`.
- Added readable wording for powered, waterlogged, open, lit, occupied, persistent, hanging, and attached block states.
- Added dedicated Fabric alpha builds for Minecraft 26.1 and 26.2.

## 0.1.1-alpha

### Recovery

- Added automatic recovery for corrupted or empty configuration files.
- Invalid configurations are now preserved in the E HUD backup folder before safe defaults are restored.
- Added validation and safe limits for HUD scale, opacity, animation, spacing, offsets, scan settings, backup schedules, gradients, profiles, and per-element settings.
- Improved atomic configuration and preset writes so interrupted saves are less likely to damage player settings.
- Invalid preset files now produce a clear import error instead of destabilizing the settings screen.
- Unexpected target-inspection or HUD-rendering errors now skip the affected frame and write a rate-limited diagnostic to the log instead of crashing the client.
- Fixed cached and unremembered pinned target information surviving world or server changes.
- Fixed the release verifier so it checks the embedded E HUD version in every jar.
- Updated the embedded project and source links to the official Modrinth and GitHub pages.
- Added dedicated alpha builds for Minecraft 1.21 through 1.21.11, plus Minecraft 26.1 and 26.2.

## 0.1.0

- Initial E HUD release.
- Added automatic context-sensitive entity, block, machine, storage, world, and player inspection.
- Added modern Basic/Advanced configuration interface, Mod Menu integration, and Options fallback button.
- Added per-item customization, live preview, drag placement, search, favorites, and recently changed lists.
- Added profiles, text import/export, global and scoped profile support, backups, reset protection, and staged saving.
- Added neon appearance controls, lightweight animation, severity icons, warning sounds, pinning, and performance controls.
- Added optional server capability handshake, vanilla-server notice, operator policies, and E HUD Admin.
- Added Animal HUD hard-incompatibility protection.
- Added dedicated Fabric builds for every stable Minecraft Java 1.21 release from 1.21 through 1.21.11.
- Added dedicated Fabric builds for Minecraft 26.1 and 26.2 using Java 25 and the unobfuscated Fabric toolchain.
- Fixed a 26.x startup crash caused by namespaced networking payload identifiers being parsed as Minecraft paths.
- Fixed a Minecraft 26.x settings crash caused by requesting GUI blur more than once in a frame.
- Added version adapters for the 26.x HUD renderer, key mappings, networking registries, GUI extraction, and screen ownership changes.
- Replaced the short license stub with the full Oolist Project License v1.0 supplied by Oolist.
