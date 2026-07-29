# Installing E HUD 0.1.2-alpha

1. Install Fabric Loader for your exact Minecraft Java version.
2. Install Fabric API for that version.
3. Copy the E HUD jar whose filename exactly matches your Minecraft version into the `mods` folder.
4. Optionally install Mod Menu. E HUD remains configurable without it through Minecraft's Options screen.
5. Remove Animal HUD before starting Minecraft; the two mods are intentionally incompatible.

E HUD supports these dedicated Fabric builds. Minecraft 1.21.x requires Java 21;
Minecraft 26.x requires Java 25.

| Minecraft | Java | E HUD file |
|---|---:|---|
| 1.21 | 21 | `e-hud-1.21-0.1.2-alpha.jar` |
| 1.21.1 | 21 | `e-hud-1.21.1-0.1.2-alpha.jar` |
| 1.21.2 | 21 | `e-hud-1.21.2-0.1.2-alpha.jar` |
| 1.21.3 | 21 | `e-hud-1.21.3-0.1.2-alpha.jar` |
| 1.21.4 | 21 | `e-hud-1.21.4-0.1.2-alpha.jar` |
| 1.21.5 | 21 | `e-hud-1.21.5-0.1.2-alpha.jar` |
| 1.21.6 | 21 | `e-hud-1.21.6-0.1.2-alpha.jar` |
| 1.21.7 | 21 | `e-hud-1.21.7-0.1.2-alpha.jar` |
| 1.21.8 | 21 | `e-hud-1.21.8-0.1.2-alpha.jar` |
| 1.21.9 | 21 | `e-hud-1.21.9-0.1.2-alpha.jar` |
| 1.21.10 | 21 | `e-hud-1.21.10-0.1.2-alpha.jar` |
| 1.21.11 | 21 | `e-hud-1.21.11-0.1.2-alpha.jar` |
| 26.1 | 25 | `e-hud-26.1-0.1.2-alpha.jar` |
| 26.2 | 25 | `e-hud-26.2-0.1.2-alpha.jar` |

## Optional server installation

Install the same version-specific E HUD jar and Fabric API on the server to advertise E HUD capability and enable the operator policy panel. Normal client-visible inspection works without a server installation.

## Configuration files

E HUD creates `config/ehud/` with the main configuration, shareable text presets, and scheduled backups. The optional server policy is stored in `config/ehud-server.json`.
