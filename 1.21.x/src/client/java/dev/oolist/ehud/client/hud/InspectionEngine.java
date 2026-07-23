package dev.oolist.ehud.client.hud;

import dev.oolist.ehud.client.config.ConfigManager;
import dev.oolist.ehud.client.config.EHudConfig;
import dev.oolist.ehud.client.config.HudModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;

public final class InspectionEngine {
    private InspectionEngine() {
    }

    public static Inspection inspect(Minecraft client) {
        if (client.level == null || client.player == null || client.hitResult == null) {
            return null;
        }

        EHudConfig config = ConfigManager.get();
        HitResult hit = client.hitResult;
        double distance = effectiveScanDistance(client, config);
        if (!(hit instanceof EntityHitResult) && distance > 5.0D) {
            hit = client.player.pick(distance, 1.0F, false);
        }
        if (hit instanceof EntityHitResult entityHit
                && (config.modules.getOrDefault(HudModule.ANIMALS, true)
                || config.modules.getOrDefault(HudModule.COMBAT, true))) {
            return inspectEntity(entityHit.getEntity(), config);
        }
        if (hit instanceof BlockHitResult blockHit
                && blockInspectionEnabled(config)) {
            return inspectBlock(client, blockHit.getBlockPos(), config);
        }
        return inspectEnvironment(client, config);
    }

    private static Inspection inspectEntity(Entity entity, EHudConfig config) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        Inspection result = new Inspection(entity.getName().getString(), humanize(id), "entity");
        if (config.showRegistryIds) {
            result.add("ID", id);
        }
        result.add("Distance", String.format(Locale.ROOT, "%.1f blocks",
                Minecraft.getInstance().player.distanceTo(entity)));

        if (entity instanceof LivingEntity living) {
            result.add("Health", String.format(Locale.ROOT, "%.1f / %.1f",
                    living.getHealth(), living.getMaxHealth()),
                    living.getHealth() <= living.getMaxHealth() * 0.25F
                            ? InfoLine.Severity.DANGER : InfoLine.Severity.NORMAL);
            result.add("Armor", Integer.toString(living.getArmorValue()));
            if (!living.getActiveEffects().isEmpty()) {
                result.add("Effects", Integer.toString(living.getActiveEffects().size()));
            }
        }
        if (entity instanceof AgeableMob ageable) {
            int age = ageable.getAge();
            result.add("Age", age < 0 ? "Baby · " + formatTicks(-age) : "Adult");
        }
        if (entity instanceof Animal animal) {
            result.add("Breeding", animal.isInLove() ? "Ready now" :
                    animal.getAge() > 0 ? "Cooldown · " + formatTicks(animal.getAge()) : "Available");
        }
        EntityDetailProvider.append(result, entity);
        return result;
    }

    private static Inspection inspectBlock(Minecraft client, BlockPos pos, EHudConfig config) {
        BlockState state = client.level.getBlockState(pos);
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        Inspection result = new Inspection(state.getBlock().getName().getString(), humanize(id), "block");
        if (config.showRegistryIds) {
            result.add("ID", id);
        }
        result.add("Position", pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        result.add("Hardness", formatFloat(state.getDestroySpeed(client.level, pos)));
        result.add("Light", Integer.toString(client.level.getMaxLocalRawBrightness(pos)));
        int signal = state.getSignal(client.level, pos, BlockHitResult.miss(
                pos.getCenter(), net.minecraft.core.Direction.UP, pos).getDirection());
        if (signal > 0) {
            result.add("Redstone", Integer.toString(signal), InfoLine.Severity.INFORMATION);
        }
        if (config.showTechnicalDetails) {
            result.add("Properties", Integer.toString(state.getProperties().size()));
        }
        BlockDetailProvider.append(result, client.level, pos, state);
        return result;
    }

    private static Inspection inspectEnvironment(Minecraft client, EHudConfig config) {
        if (!config.modules.getOrDefault(HudModule.WORLD, true)
                && !config.modules.getOrDefault(HudModule.PLAYER, true)) {
            return null;
        }
        BlockPos pos = client.player.blockPosition();
        Inspection result = new Inspection("Environment", resourceKeyName(client.level.dimension()), "world");
        result.add("Position", pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        result.add("Biome", client.level.getBiome(pos).unwrapKey()
                .map(InspectionEngine::resourceKeyName).orElse("Unknown"));
        long dayTime = client.level.getDayTime() % 24_000L;
        result.add("Time", formatWorldTime(dayTime));
        result.add("Weather", client.level.isThundering() ? "Thunderstorm"
                : client.level.isRaining() ? "Rain" : "Clear");
        int light = client.level.getMaxLocalRawBrightness(pos);
        result.add("Light", Integer.toString(light), light <= 0
                ? InfoLine.Severity.CAUTION : InfoLine.Severity.NORMAL);

        Inventory inventory = client.player.getInventory();
        int freeSlots = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).isEmpty()) freeSlots++;
        }
        result.add("Free inventory", freeSlots + " slots", freeSlots == 0
                ? InfoLine.Severity.DANGER : freeSlots <= 3 ? InfoLine.Severity.CAUTION : InfoLine.Severity.NORMAL);
        ItemStack held = client.player.getMainHandItem();
        if (!held.isEmpty() && held.isDamageableItem()) {
            int remaining = held.getMaxDamage() - held.getDamageValue();
            result.add("Held durability", remaining + " / " + held.getMaxDamage(),
                    remaining <= Math.max(1, held.getMaxDamage() / 10)
                            ? InfoLine.Severity.DANGER : InfoLine.Severity.NORMAL);
        }
        result.add("Health", String.format(Locale.ROOT, "%.1f / %.1f",
                        client.player.getHealth(), client.player.getMaxHealth()),
                client.player.getHealth() <= client.player.getMaxHealth() * 0.25F
                        ? InfoLine.Severity.DANGER : InfoLine.Severity.NORMAL);
        int food = client.player.getFoodData().getFoodLevel();
        result.add("Food", food + " / 20", food <= 6 ? InfoLine.Severity.CAUTION : InfoLine.Severity.NORMAL);
        if (client.player.getAirSupply() < client.player.getMaxAirSupply()) {
            result.add("Air", client.player.getAirSupply() + " / " + client.player.getMaxAirSupply(),
                    client.player.getAirSupply() <= 60 ? InfoLine.Severity.DANGER : InfoLine.Severity.CAUTION);
        }
        result.add("Experience", "Level " + client.player.experienceLevel + " - "
                + Math.round(client.player.experienceProgress * 100) + "%");
        result.add("Armor", Integer.toString(client.player.getArmorValue()));
        if (!client.player.getActiveEffects().isEmpty()) {
            result.add("Effects", Integer.toString(client.player.getActiveEffects().size()));
        }
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        result.add("Chunk", chunkX + ", " + chunkZ + " - local " + (pos.getX() & 15) + ", " + (pos.getZ() & 15));
        result.add("Facing", client.player.getDirection().getName());
        return result;
    }

    private static String humanize(String id) {
        String value = id.substring(id.indexOf(':') + 1).replace('_', ' ');
        StringBuilder result = new StringBuilder(value.length());
        boolean upper = true;
        for (char character : value.toCharArray()) {
            result.append(upper ? Character.toUpperCase(character) : character);
            upper = character == ' ';
        }
        return result.toString();
    }

    private static String formatTicks(int ticks) {
        int seconds = Math.max(0, ticks / 20);
        return seconds >= 60 ? (seconds / 60) + "m " + (seconds % 60) + "s" : seconds + "s";
    }

    private static String formatFloat(float value) {
        if (value < 0) {
            return "Unbreakable";
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String formatWorldTime(long dayTime) {
        long hours = (dayTime / 1000L + 6L) % 24L;
        long minutes = (dayTime % 1000L) * 60L / 1000L;
        return String.format(Locale.ROOT, "%02d:%02d", hours, minutes);
    }

    private static String resourceKeyName(Object key) {
        for (String method : new String[]{"identifier", "location"}) {
            try {
                Object value = key.getClass().getMethod(method).invoke(key);
                if (value != null) return value.toString();
            } catch (ReflectiveOperationException ignored) { }
        }
        return String.valueOf(key);
    }

    private static double effectiveScanDistance(Minecraft client, EHudConfig config) {
        int simulationChunks = 10;
        try {
            Object option = client.options.getClass().getMethod("simulationDistance").invoke(client.options);
            Object value = option.getClass().getMethod("get").invoke(option);
            if (value instanceof Number number) simulationChunks = number.intValue();
        } catch (ReflectiveOperationException ignored) { }
        int maximum = Math.max(16, simulationChunks * 16);
        return config.scanDistance < 0 ? maximum : Math.min(Math.max(1, config.scanDistance), maximum);
    }

    private static boolean blockInspectionEnabled(EHudConfig config) {
        return config.modules.getOrDefault(HudModule.BLOCKS, true)
                || config.modules.getOrDefault(HudModule.STORAGE, true)
                || config.modules.getOrDefault(HudModule.PROCESSING, true)
                || config.modules.getOrDefault(HudModule.FARMING, true)
                || config.modules.getOrDefault(HudModule.REDSTONE, true)
                || config.modules.getOrDefault(HudModule.BUILDING, true);
    }
}
