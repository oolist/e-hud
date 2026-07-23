# Updating E HUD

## Normal update workflow

1. Pull the newest `main` branch before editing.
2. Make changes in `1.21.x`, `26.x`, or both.
3. Build and test every affected Minecraft version.
4. Update the affected `CHANGELOG.md` files.
5. Increase `mod_version` in the affected `gradle.properties` files.
6. Update the root `VERSION_MATRIX.md`.
7. Commit with a clear message and push to `main`.
8. Tag each tested build using:

   `v<mod-version>-mc<minecraft-version>`

Example:

```text
v0.2.0-alpha-mc1.21.11
v0.2.0-alpha-mc26.2
```

## What must not be committed

Do not upload Gradle caches, generated build directories, run directories,
logs, crash reports, local configuration, IDE files, or access tokens.

## Before publishing

- Confirm the source contains no passwords, tokens, or private server data.
- Confirm the changelog matches the code.
- Confirm every version tag points to the tested source commit.
- Confirm the correct license remains present in both source trees.

