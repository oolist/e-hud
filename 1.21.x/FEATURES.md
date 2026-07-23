# E HUD feature map

E HUD exposes 80+ configurable information and quality-of-life entries across these ordered categories:

1. Animals & Pets
2. Combat & Hostiles
3. Blocks
4. Storage
5. Machines
6. Farming
7. Redstone
8. Building Safety
9. World & Travel
10. Player
11. Multiplayer
12. Small Tweaks

Basic mode shows the most useful options. Advanced mode reveals technical identifiers, diagnostics, lower-level block/entity state, extra performance controls, and debugging features.

Every catalogued item can be enabled or disabled, favorited, searched, and opened in its own customization view. Rendered information supports per-item offsets, scale, color, and relevance/danger conditions. Category-level switches and operator policy are applied before rendering.

## Data behavior

- Client-visible data works on normal Fabric or vanilla servers.
- E HUD never fabricates hidden values. Information not synchronized to the client is omitted.
- Installing E HUD server-side advertises server capability and enables operator policy distribution.
- The inspection cache honors the configured refresh interval and adaptive-performance setting.
- Long-distance block inspection is capped at the current simulation distance.
