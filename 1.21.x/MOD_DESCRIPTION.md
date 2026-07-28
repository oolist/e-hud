# E HUD — Everything, exactly when it matters

E HUD is a lightweight, context-sensitive information and diagnostics HUD for Minecraft Java Edition. Instead of covering the screen with permanent counters, it automatically shows the most useful information about the animal, mob, block, machine, container, farm, redstone component, player state, or environment you are currently inspecting.

The default panel appears just above the hotbar in a neon-green and orange theme. It is designed to feel like Vanilla Plus: useful immediately, quiet when it has nothing important to say, and deeply customizable when you want full control.

## Main features

- Automatic target inspection with an optional unbound Inspect key.
- Animal and pet information: health, age, breeding, ownership, commands, behavior, variants, horse temper, bee status, eggs, genes, horns, wool, allay inventory, villager information, and much more.
- Combat information: armor, effects, targets, aggression, anger, creeper fuse, warden anger, enderman state, and severity warnings.
- Blocks and building: position, hardness, light, orientation, state properties, support, fluids, falling-block danger, and technical IDs.
- Storage and machines: capacity, visible contents, furnace slots, brewing stands, hoppers, beehives, beacons, spawners, trial spawners, crafters, lecterns, bookshelves, and jukeboxes.
- Farming and redstone: growth, maturity, hydration, power, signals, transfer locks, crafter signals, and contextual warnings.
- World and player information: coordinates, biome, time, weather, light, dimension, chunk position, facing, inventory space, health, food, air, experience, armor, effects, and held-item durability.
- Pinned inspections, severity icons, configurable alert sounds, and lightweight animated gradients.
- Modern large-page settings UI with Basic and Advanced modes.
- Search, favorites, recently changed settings, live preview, and mouse drag placement.
- Individual control for information items, including visibility, offset, scale, color, and display condition.
- Custom ARGB colors, gradient presets, borders, icons, opacity, text shadow, animation, scale, line spacing, and panel placement.
- Named profiles, duplicate/import/export support, and shareable `.txt` preset files.
- Global defaults with optional per-world and per-server profile links.
- Automatic backups, configurable backup schedule, backup-before-reset, and confirmation for more than ten staged changes.
- Adaptive refresh and scan controls. Scan range follows simulation distance by default and cannot exceed it.
- Optional Mod Menu integration. If Mod Menu is absent, E HUD adds its own button to Minecraft's Options screen.
- Optional server capability protocol and E HUD Admin panel. Operators can disable categories for connected clients, which display `Disabled by operator`.
- Vanilla-server warning with continue, leave, and do-not-show-again choices. Server-only controls are shown as unavailable.
- Compatibility diagnostics and a visible Debug Mode button.

All normal settings are staged and only become active after pressing **Save**.

## Requirements

- Minecraft Java Edition 1.21 through 1.21.11, plus 26.1 and 26.2. Use the jar whose filename exactly matches your Minecraft version.
- Fabric Loader.
- Fabric API.
- Java 21 for Minecraft 1.21.x; Java 25 for Minecraft 26.1 and 26.2.
- Mod Menu is optional.

## Common questions

### Will the mod work with Animal HUD?

No. Animal HUD and E HUD control overlapping animal information and are intentionally treated as incompatible. If both are detected, E HUD blocks play and asks you to remove Animal HUD and restart. Fabric cannot safely unload another mod while Minecraft is running.

### Does E HUD need to be installed on the server?

No for normal client-visible information, automatic inspection, customization, profiles, and warnings. Install E HUD on the server to enable capability detection and operator policy controls.

### Does Mod Menu have to be installed?

No. With Mod Menu, its Configure button opens E HUD. Without Mod Menu, E HUD adds an **E HUD Settings** button to Minecraft's Options screen.

### Are there default keybinds?

No. Open Config, Inspect, and Pin are present in Minecraft's Controls menu but are unbound by default. Automatic inspection is enabled by default.

### Can I move and resize the HUD?

Yes. The live editor supports mouse dragging, anchor selection, exact offsets, overall scale, and individual line offsets and scales.

### Can I make my own colors and gradients?

Yes. E HUD includes multiple neon gradient presets and accepts custom 8-digit ARGB colors for the primary, accent, and panel colors.

### Can presets be shared?

Yes. Profiles export as readable `.txt` files containing the complete configuration. They can be copied into another player's E HUD presets folder and imported in game.

### Why are some controls dark or inaccessible on a server?

The server does not advertise that capability, or a server operator disabled the category through E HUD Admin. Operator-disabled controls are labelled clearly.

### What happens when settings are reset?

E HUD first creates a backup. Scheduled backups are enabled weekly by default and the interval is configurable.

### Does E HUD support controllers?

E HUD is designed for keyboard and mouse. Dedicated controller navigation is not included.

## Links

- Author: **oolist**
- Project: https://modrinth.com/mod/e-hud
- Source: https://github.com/oolist/e-hud
- Discord: https://discord.gg/ZYK4UzsHCr
