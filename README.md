# E HUD


E HUD is a customizable context-sensitive information and diagnostics HUD for
Minecraft Java Edition, built for Fabric.


## Source layout


- [`1.21.x/`](1.21.x/) contains the source for Minecraft 1.21 through 1.21.11.
- [`26.x/`](26.x/) contains the source for Minecraft 26.1 and 26.2.
- [`VERSION_MATRIX.md`](VERSION_MATRIX.md) lists every supported Minecraft and
  mod version.


Each source tree has its own build instructions, Gradle configuration,
changelog, and license.


## Current release

E HUD `0.1.2-alpha` supports:


- Minecraft 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7,
  1.21.8, 1.21.9, 1.21.10, and 1.21.11
- Minecraft 26.1 and 26.2


Use the source tree and release file that exactly match the Minecraft version.


## Updating the project


The `main` branch always contains the newest source. For every update:


1. Change the code in both source trees when the change applies to both.
2. Update each affected `gradle.properties` file and changelog.
3. Update `VERSION_MATRIX.md`.
4. Commit and push the changes.
5. Create tags using `v<mod-version>-mc<minecraft-version>`, such as
   `v0.1.2-alpha-mc1.21.11` or `v0.1.2-alpha-mc26.2`.


See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the repeatable update workflow.


## Links


- [Modrinth](https://modrinth.com/mod/e-hud)
- [Discord](https://discord.gg/ZYK4UzsHCr)
- [Patreon](https://www.patreon.com/oolist)


Author: **oolist**



