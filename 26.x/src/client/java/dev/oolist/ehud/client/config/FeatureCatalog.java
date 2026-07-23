package dev.oolist.ehud.client.config;

import java.util.Arrays;
import java.util.List;

public final class FeatureCatalog {
    private static final List<HudFeature> FEATURES = Arrays.asList(
            f(HudModule.ANIMALS, "health", "Health & maximum health", "Exact health with low-health severity", false),
            f(HudModule.ANIMALS, "age", "Age and growth", "Baby/adult state and growth countdown", false),
            f(HudModule.ANIMALS, "breeding", "Breeding readiness", "Love state, cooldown and readiness", false),
            f(HudModule.ANIMALS, "owner", "Owner & taming", "Owner, sitting and tame state", false),
            f(HudModule.ANIMALS, "temper", "Horse temper", "Temper, maximum temper and inventory", true),
            f(HudModule.ANIMALS, "nectar", "Bee activity", "Nectar, hive, stings, anger and pollination", false),
            f(HudModule.ANIMALS, "variant", "Variants & genes", "Species variants, panda genes and frog types", true),
            f(HudModule.ANIMALS, "horns", "Animal traits", "Goat horns, sheep wool and mooshroom type", true),
            f(HudModule.ANIMALS, "egg", "Eggs & nesting", "Chicken timer, turtle eggs and nesting state", false),
            f(HudModule.ANIMALS, "activity", "Behavior & activity", "Fox, sniffer, allay, armadillo and camel state", true),

            f(HudModule.COMBAT, "armor", "Armor", "Armor points for the inspected target", false),
            f(HudModule.COMBAT, "effects", "Status effects", "Active effect count and danger context", false),
            f(HudModule.COMBAT, "target", "Current target", "What a mob is trying to attack", false),
            f(HudModule.COMBAT, "aggressive", "Aggression", "Aggressive and persistent anger state", false),
            f(HudModule.COMBAT, "charged", "Creeper charge & fuse", "Charged state, fuse and ignition", false),
            f(HudModule.COMBAT, "anger", "Warden & golem anger", "Anger levels and current suspect", true),
            f(HudModule.COMBAT, "carried_block", "Enderman carried block", "The exact held block", true),
            f(HudModule.COMBAT, "danger", "Nearby danger", "Severity-ranked contextual threats", true),

            f(HudModule.BLOCKS, "position", "Block position", "Exact world coordinates", false),
            f(HudModule.BLOCKS, "hardness", "Hardness", "Mining hardness and unbreakable state", false),
            f(HudModule.BLOCKS, "light", "Light", "Local light and danger thresholds", false),
            f(HudModule.BLOCKS, "facing", "Facing", "Block orientation", false),
            f(HudModule.BLOCKS, "waterlogged", "Waterlogged", "Hidden fluid state", false),
            f(HudModule.BLOCKS, "properties", "State properties", "Technical block-state property count", true),
            f(HudModule.BLOCKS, "id", "Registry ID", "Namespaced technical identifier", true),
            f(HudModule.BLOCKS, "tool", "Best tool", "Recommended mining tool and harvest warning", true),

            f(HudModule.STORAGE, "capacity", "Occupied slots", "Filled versus total container slots", false),
            f(HudModule.STORAGE, "sample", "Contents sample", "A lightweight sample of stored items", false),
            f(HudModule.STORAGE, "capacity", "Capacity warning", "Nearly full and completely full alerts", false),
            f(HudModule.STORAGE, "locked", "Lock state", "Whether access is restricted", true),
            f(HudModule.STORAGE, "changes", "Recent changes", "Items added and removed while observed", true),
            f(HudModule.STORAGE, "stock", "Stock rules", "Custom low-stock and overflow warnings", true),

            f(HudModule.PROCESSING, "input", "Machine input", "Furnace and processor input", false),
            f(HudModule.PROCESSING, "fuel", "Fuel", "Fuel item and brewing fuel", false),
            f(HudModule.PROCESSING, "output", "Output", "Current machine output", false),
            f(HudModule.PROCESSING, "ingredient", "Brewing ingredient", "Current potion ingredient", false),
            f(HudModule.PROCESSING, "bottles", "Brewing bottles", "Occupied bottle slots", false),
            f(HudModule.PROCESSING, "slots", "Crafter slots", "Enabled, disabled and occupied crafter slots", true),
            f(HudModule.PROCESSING, "triggered", "Machine trigger", "Triggered and redstone-controlled state", true),
            f(HudModule.PROCESSING, "spawner", "Spawner diagnostics", "Spawner state and trial-spawner phase", true),

            f(HudModule.FARMING, "growth_stage", "Crop growth", "Growth stage and maturity", false),
            f(HudModule.FARMING, "hydration", "Farmland hydration", "Moisture and dry-out risk", false),
            f(HudModule.FARMING, "pollination", "Pollination", "Bee and crop pollination context", false),
            f(HudModule.FARMING, "obstruction", "Growth obstruction", "Detects blocked plants and saplings", true),
            f(HudModule.FARMING, "soil", "Soil suitability", "Explains planting and support failures", true),
            f(HudModule.FARMING, "harvest", "Harvest readiness", "Ready-to-harvest summary", false),

            f(HudModule.REDSTONE, "redstone", "Signal strength", "Current power level", false),
            f(HudModule.REDSTONE, "powered", "Powered state", "Whether the component is powered", false),
            f(HudModule.REDSTONE, "signal", "Crafter comparator signal", "Current calculated signal", true),
            f(HudModule.REDSTONE, "transfer", "Hopper enabled state", "Whether transfers are currently allowed", false),
            f(HudModule.REDSTONE, "movement", "Piston movement", "Movement and immovable-block warnings", true),
            f(HudModule.REDSTONE, "fault", "Circuit fault hints", "Explains common signal failures", true),

            f(HudModule.BUILDING, "support", "Support check", "Warns when placement lacks support", false),
            f(HudModule.BUILDING, "fluid", "Fluid interaction", "Waterlogging and flow warnings", false),
            f(HudModule.BUILDING, "fire", "Fire risk", "Nearby flammability and spread warning", false),
            f(HudModule.BUILDING, "drop_warning", "Falling-block risk", "Gravity and unsupported-block warnings", false),
            f(HudModule.BUILDING, "collision", "Placement collision", "Entity and shape collision hints", true),
            f(HudModule.BUILDING, "replace", "Replacement preview", "Shows what placement will replace", true),

            f(HudModule.WORLD, "position", "Coordinates", "Current block coordinates", false),
            f(HudModule.WORLD, "biome", "Biome", "Current biome registry name", false),
            f(HudModule.WORLD, "time", "Time", "Readable local world time", false),
            f(HudModule.WORLD, "weather", "Weather", "Clear, rain or thunder", false),
            f(HudModule.WORLD, "light", "Local light", "Light level with contextual warning", false),
            f(HudModule.WORLD, "dimension", "Dimension", "Current dimension identifier", false),
            f(HudModule.WORLD, "chunk", "Chunk & region", "Chunk borders, region and local position", true),
            f(HudModule.WORLD, "portal", "Portal links", "Portal destination and coordinate conversion", true),

            f(HudModule.PLAYER, "free_inventory", "Free inventory space", "Empty slots and full-inventory warning", false),
            f(HudModule.PLAYER, "held_durability", "Held durability", "Remaining uses and break warning", false),
            f(HudModule.PLAYER, "food", "Food & saturation", "Hunger and saturation context", false),
            f(HudModule.PLAYER, "health", "Player health", "Health with low-health severity", false),
            f(HudModule.PLAYER, "armor", "Player armor", "Current armor protection", false),
            f(HudModule.PLAYER, "air", "Air", "Remaining breath and drowning warning", false),
            f(HudModule.PLAYER, "experience", "Experience", "Level and progress", false),
            f(HudModule.PLAYER, "effects", "Player effects", "Active effects and expiry warnings", true),
            f(HudModule.PLAYER, "equipment", "Equipment health", "Armor and elytra durability summary", true),

            f(HudModule.MULTIPLAYER, "server_capability", "Server capability", "Shows whether E HUD server features are available", false),
            f(HudModule.MULTIPLAYER, "operator_policy", "Operator policy", "Displays settings disabled by an operator", false),
            f(HudModule.MULTIPLAYER, "team", "Team context", "Teammates and friendly-fire state", true),
            f(HudModule.MULTIPLAYER, "latency", "Connection quality", "Latency and packet health", true),
            f(HudModule.MULTIPLAYER, "profile", "Per-server profile", "Automatically select a saved server profile", false),

            f(HudModule.SMALL_TWEAKS, "pin", "Pinned inspection", "Keep the current target visible", false),
            f(HudModule.SMALL_TWEAKS, "warning_icon", "Warning icons", "Severity icons beside important messages", false),
            f(HudModule.SMALL_TWEAKS, "warning_sound", "Warning sounds", "Configurable sound for important alerts", false),
            f(HudModule.SMALL_TWEAKS, "animation", "Lightweight animation", "Fade, slide and gradient movement", false),
            f(HudModule.SMALL_TWEAKS, "recent", "Recently changed", "Quickly return to edited settings", true),
            f(HudModule.SMALL_TWEAKS, "favorites", "Favorites", "Pin frequently changed settings", true),
            f(HudModule.SMALL_TWEAKS, "debug", "Debug details", "Diagnostics for compatibility reports", true)
    );

    private FeatureCatalog() {
    }

    public static List<HudFeature> forModule(HudModule module, boolean advanced) {
        return FEATURES.stream().filter(feature -> feature.module() == module)
                .filter(feature -> advanced || !feature.advanced()).toList();
    }

    public static int count(HudModule module) {
        return (int) FEATURES.stream().filter(feature -> feature.module() == module).count();
    }

    public static List<HudFeature> all(boolean advanced) {
        return FEATURES.stream().filter(feature -> advanced || !feature.advanced()).toList();
    }

    public static HudModule moduleForKey(String key) {
        String simple = key.substring(key.indexOf('.') + 1);
        if (key.startsWith("entity.")) {
            return switch (simple) {
                case "armor", "effects", "target", "aggressive", "anger", "charged", "fuse",
                        "carrying", "provoked", "danger" -> HudModule.COMBAT;
                default -> HudModule.ANIMALS;
            };
        }
        if (key.startsWith("block.")) {
            if (simple.equals("capacity") || simple.equals("visible_items") || simple.equals("contents") || simple.equals("locked")) return HudModule.STORAGE;
            if (simple.equals("input") || simple.equals("fuel") || simple.equals("output") || simple.equals("ingredient")
                    || simple.equals("bottles") || simple.equals("enabled_slots") || simple.equals("triggered") || simple.equals("spawner") || simple.equals("trial_state")) return HudModule.PROCESSING;
            if (simple.equals("growth_stage") || simple.equals("hydration") || simple.equals("pollination") || simple.equals("obstruction") || simple.equals("soil")) return HudModule.FARMING;
            if (simple.equals("redstone") || simple.equals("powered") || simple.equals("signal") || simple.equals("transfer") || simple.equals("output_direction")) return HudModule.REDSTONE;
            if (simple.equals("support") || simple.equals("fluid") || simple.equals("drop_warning")
                    || simple.equals("fire") || simple.equals("collision") || simple.equals("replace")) return HudModule.BUILDING;
            return HudModule.BLOCKS;
        }
        if (key.startsWith("world.")) {
            return simple.equals("free_inventory") || simple.equals("held_durability") || simple.equals("food")
                    || simple.equals("health") || simple.equals("armor") || simple.equals("effects")
                    || simple.equals("air") || simple.equals("experience") || simple.equals("equipment")
                    ? HudModule.PLAYER : HudModule.WORLD;
        }
        return HudModule.SMALL_TWEAKS;
    }

    private static HudFeature f(HudModule module, String key, String title, String description, boolean advanced) {
        String namespace = switch (module) {
            case ANIMALS, COMBAT -> "entity";
            case BLOCKS, STORAGE, PROCESSING, FARMING, REDSTONE, BUILDING -> "block";
            case WORLD, PLAYER -> "world";
            case MULTIPLAYER -> "server";
            case SMALL_TWEAKS -> "ui";
        };
        return new HudFeature(module, namespace + "." + key, title, description, advanced);
    }
}
