package dev.oolist.ehud.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;

final class EntityDetailProvider {
    private EntityDetailProvider() {
    }

    static void append(Inspection result, Entity entity) {
        if (entity instanceof Mob mob) {
            if (mob.getTarget() != null) result.add("Target", mob.getTarget().getName().getString(), InfoLine.Severity.CAUTION);
            if (mob.isAggressive()) result.add("Behavior", "Aggressive", InfoLine.Severity.DANGER);
            if (!mob.getNavigation().isDone()) result.add("Pathfinding", "Moving to a destination", InfoLine.Severity.INFORMATION);
        }
        if (entity instanceof NeutralMob neutral && neutral.isAngry()) {
            result.add("Anger", "Active", InfoLine.Severity.DANGER);
        }
        if (entity instanceof TamableAnimal tameable) appendTameable(result, tameable);

        String kind = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();
        if (kind.contains("horse") || kind.equals("donkey") || kind.equals("mule")) appendHorse(result, entity);
        switch (kind) {
            case "bee" -> appendBee(result, entity);
            case "fox" -> {
                result.add("Variant", clean(call(entity, "getVariant")));
                result.add("Behavior", bool(entity, "isSleeping") ? "Sleeping" : bool(entity, "isSitting") ? "Sitting"
                        : bool(entity, "isPouncing") ? "Pouncing" : bool(entity, "isCrouching") ? "Crouching" : "Roaming");
            }
            case "turtle" -> { result.add("Carrying egg", yesNo(bool(entity, "hasEgg"))); result.add("Egg laying", yesNo(bool(entity, "isLayingEgg"))); }
            case "sniffer" -> { result.add("Searching", yesNo(bool(entity, "isSearching"))); result.add("Can dig", yesNo(bool(entity, "canSniff"))); result.add("Tempted", yesNo(bool(entity, "isTempted"))); }
            case "allay" -> appendAllay(result, entity);
            case "goat" -> { result.add("Variant", bool(entity, "isScreamingGoat") ? "Screaming" : "Normal"); result.add("Horns", (bool(entity, "hasLeftHorn") ? 1 : 0) + (bool(entity, "hasRightHorn") ? 1 : 0) + " / 2"); }
            case "sheep" -> { result.add("Wool", clean(call(call(entity, "getColor"), "getName"))); result.add("Shearing", bool(entity, "readyForShearing") ? "Ready" : "Not ready"); }
            case "chicken" -> appendChicken(result, entity);
            case "mooshroom" -> { result.add("Variant", clean(call(entity, "getVariant"))); result.add("Shearing", bool(entity, "readyForShearing") ? "Ready" : "Unavailable"); }
            case "panda" -> appendPanda(result, entity);
            case "axolotl" -> { result.add("Variant", clean(call(call(entity, "getVariant"), "getName"))); result.add("Playing dead", yesNo(bool(entity, "isPlayingDead"))); result.add("From bucket", yesNo(bool(entity, "fromBucket"))); }
            case "frog" -> appendFrog(result, entity);
            case "camel" -> { result.add("Pose", bool(entity, "isCamelSitting") ? "Sitting" : "Standing"); int cooldown = integer(entity, "getJumpCooldown"); result.add("Dash", cooldown > 0 ? formatTicks(cooldown) : "Ready"); result.add("Passengers", Integer.toString(entity.getPassengers().size())); }
            case "armadillo" -> { result.add("State", clean(call(entity, "getState"))); result.add("Scared", yesNo(bool(entity, "isScared"))); }
            case "strider" -> { boolean cold = bool(entity, "isSuffocating"); result.add("Temperature", cold ? "Cold" : "Warm", cold ? InfoLine.Severity.CAUTION : InfoLine.Severity.NORMAL); result.add("Saddled", yesNo(bool(entity, "isSaddled"))); }
            case "villager" -> appendVillager(result, entity);
            case "iron_golem" -> { result.add("Cracks", clean(call(entity, "getCrackiness"))); result.add("Created by player", yesNo(bool(entity, "isPlayerCreated"))); boolean angry = bool(entity, "isAngry"); result.add("Angry", yesNo(angry), angry ? InfoLine.Severity.DANGER : InfoLine.Severity.NORMAL); }
            case "creeper" -> appendCreeper(result, entity);
            case "enderman" -> { Object block = call(entity, "getCarriedBlock"); result.add("Carrying", block == null ? "Nothing" : clean(call(call(block, "getBlock"), "getName"))); boolean creepy = bool(entity, "isCreepy"); result.add("Provoked", yesNo(creepy), creepy ? InfoLine.Severity.DANGER : InfoLine.Severity.NORMAL); }
            case "warden" -> { result.add("Anger", clean(call(entity, "getAngerLevel")), InfoLine.Severity.DANGER); result.add("Anger value", Integer.toString(integer(entity, "getClientAngerLevel"))); }
            default -> { }
        }
    }

    private static void appendTameable(Inspection result, TamableAnimal tameable) {
        result.add("Tamed", yesNo(tameable.isTame()));
        if (!tameable.isTame()) return;
        if (tameable.getOwner() != null) result.add("Owner", tameable.getOwner().getName().getString());
        else {
            Object reference = first(tameable, "getOwnerReference", "getOwnerUUID");
            Object uuid = reference == null ? null : first(reference, "getUUID");
            String ownerId = uuid == null ? clean(reference) : uuid.toString();
            if (!"Unknown".equals(ownerId) && ownerId.length() >= 8) result.add("Owner", ownerId.substring(0, 8));
        }
        result.add("Command", tameable.isOrderedToSit() ? "Sitting" : "Following");
        result.add("Can teleport", yesNo(tameable.shouldTryTeleportToOwner()));
    }

    private static void appendHorse(Inspection result, Entity entity) {
        result.add("Tamed", yesNo(bool(entity, "isTamed")));
        result.add("Temper", integer(entity, "getTemper") + " / " + integer(entity, "getMaxTemper"));
        int slots = integer(entity, "getInventorySize"); if (slots > 0) result.add("Inventory", slots + " slots");
        if (bool(entity, "isEating") || bool(entity, "isStanding")) result.add("Behavior", bool(entity, "isEating") ? "Eating" : "Rearing");
    }

    private static void appendBee(Inspection result, Entity entity) {
        result.add("Nectar", yesNo(bool(entity, "hasNectar")));
        boolean stung = bool(entity, "hasStung"); result.add("Stung", yesNo(stung), stung ? InfoLine.Severity.CAUTION : InfoLine.Severity.NORMAL);
        Object hive = call(entity, "getHivePos"); result.add("Hive", bool(entity, "hasHive") && hive != null ? clean(hive) : "Homeless",
                bool(entity, "hasHive") ? InfoLine.Severity.NORMAL : InfoLine.Severity.CAUTION);
    }

    private static void appendAllay(Inspection result, Entity entity) {
        Object held = call(entity, "getMainHandItem");
        result.add("Held item", held instanceof ItemStack stack && !stack.isEmpty() ? itemName(stack) : "Nothing");
        result.add("Dancing", yesNo(bool(entity, "isDancing")));
        Object inventory = call(entity, "getInventory"); result.add("Inventory", bool(inventory, "isEmpty") ? "Empty" : "Contains items");
    }

    private static void appendChicken(Inspection result, Entity entity) {
        result.add("Variant", clean(call(entity, "getVariant")));
        int egg = fieldInt(entity, "eggTime"); result.add("Next egg", bool(entity, "isBaby") ? "After growing" : formatTicks(egg));
        if (bool(entity, "isChickenJockey")) result.add("Special", "Chicken jockey", InfoLine.Severity.INFORMATION);
    }

    private static void appendPanda(Inspection result, Entity entity) {
        result.add("Personality", clean(call(entity, "getVariant"))); result.add("Hidden gene", clean(call(entity, "getHiddenGene")));
        result.add("Behavior", bool(entity, "isEating") ? "Eating" : bool(entity, "isSneezing") ? "Sneezing"
                : bool(entity, "isRolling") ? "Rolling" : bool(entity, "isSitting") ? "Sitting" : "Roaming");
    }

    private static void appendFrog(Inspection result, Entity entity) {
        result.add("Variant", clean(call(entity, "getVariant"))); Object target = call(entity, "getTongueTarget");
        if (target instanceof Optional<?> optional) target = optional.orElse(null);
        result.add("Tongue target", target instanceof Entity targetEntity ? targetEntity.getName().getString() : "None");
    }

    private static void appendVillager(Inspection result, Entity entity) {
        Object data = call(entity, "getVillagerData"); Object profession = first(data, "profession", "getProfession");
        Object level = first(data, "level", "getLevel"); result.add("Profession", clean(profession)); result.add("Level", clean(level));
        result.add("Experience", Integer.toString(integer(entity, "getVillagerXp")));
        Object player = Minecraft.getInstance().player;
        Object reputation = player == null ? null : call(entity, "getPlayerReputation", player);
        if (reputation != null) result.add("Your reputation", clean(reputation));
        result.add("Restock", bool(entity, "canRestock") ? "Available" : "Unavailable");
    }

    private static void appendCreeper(Inspection result, Entity entity) {
        result.add("Charged", yesNo(bool(entity, "isPowered"))); int direction = integer(entity, "getSwellDir");
        Object swelling = call(entity, "getSwelling", 1.0F); int percent = swelling instanceof Number n ? Math.round(n.floatValue() * 100) : 0;
        result.add("Fuse", direction > 0 ? percent + "%" : "Idle", direction > 0 ? InfoLine.Severity.CRITICAL : InfoLine.Severity.NORMAL);
    }

    private static Object first(Object target, String... names) {
        for (String name : names) { Object value = call(target, name); if (value != null) return value; }
        return null;
    }

    private static Object call(Object target, String name, Object... arguments) {
        if (target == null) return null;
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name) || method.getParameterCount() != arguments.length) continue;
            try { return method.invoke(target, arguments); } catch (ReflectiveOperationException | IllegalArgumentException ignored) { }
        }
        return null;
    }

    private static boolean bool(Object target, String method) { return Boolean.TRUE.equals(call(target, method)); }
    private static int integer(Object target, String method) { Object value = call(target, method); return value instanceof Number n ? n.intValue() : 0; }
    private static int fieldInt(Object target, String name) {
        try { Field field = target.getClass().getField(name); return field.getInt(target); }
        catch (ReflectiveOperationException ignored) { return 0; }
    }

    private static String itemName(ItemStack stack) { return stack.getHoverName().getString() + (stack.getCount() > 1 ? " x" + stack.getCount() : ""); }
    private static String formatTicks(int ticks) { int seconds = Math.max(0, ticks / 20); return seconds >= 60 ? String.format(Locale.ROOT, "%dm %02ds", seconds / 60, seconds % 60) : seconds + "s"; }
    private static String yesNo(boolean value) { return value ? "Yes" : "No"; }
    private static String clean(Object value) {
        if (value == null) return "Unknown";
        if (value instanceof net.minecraft.network.chat.Component component) return component.getString();
        String text = String.valueOf(value).replace("minecraft:", "").replace('_', ' ');
        int slash = Math.max(text.lastIndexOf('/'), text.lastIndexOf(':'));
        if (slash >= 0) text = text.substring(slash + 1);
        return text.replace("]", "").trim();
    }
}
