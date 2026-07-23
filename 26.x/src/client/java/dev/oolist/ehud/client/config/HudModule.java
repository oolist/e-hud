package dev.oolist.ehud.client.config;

public enum HudModule {
    ANIMALS("Animals & Pets", "Health, breeding, ownership, herds and animal diagnostics"),
    COMBAT("Combat & Hostiles", "Targets, armor, effects and danger states"),
    BLOCKS("Blocks", "Block state, tools, drops, light and orientation"),
    STORAGE("Storage", "Capacity, contents, changes and stock warnings"),
    PROCESSING("Machines", "Furnaces, brewing, hoppers, crafters and diagnostics"),
    FARMING("Farming", "Growth, hydration, soil, pollination and obstructions"),
    REDSTONE("Redstone", "Power, signals, movement and fault explanations"),
    BUILDING("Building Safety", "Placement, support, fluid, fire and drop warnings"),
    WORLD("World & Travel", "Position, time, light, maps, portals and chunks"),
    PLAYER("Player", "Inventory, durability, effects and survival warnings"),
    MULTIPLAYER("Multiplayer", "Server capabilities, teammates and operator policy"),
    SMALL_TWEAKS("Small Tweaks", "Notifications, polish and minor conveniences");

    private final String title;
    private final String description;

    HudModule(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }
}
