package dev.oolist.ehud.client.hud;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;

final class BlockDetailProvider {
    private BlockDetailProvider() {
    }

    static void append(Inspection result, Level level, BlockPos pos, BlockState state) {
        appendStateProperties(result, state);
        result.add("Support", state.canSurvive(level, pos) ? "Stable" : "Will break",
                state.canSurvive(level, pos) ? InfoLine.Severity.NORMAL : InfoLine.Severity.DANGER);
        if (!state.getFluidState().isEmpty()) {
            result.add("Fluid", state.getFluidState().getType().toString(), InfoLine.Severity.INFORMATION);
        }
        if (state.getBlock() instanceof FallingBlock && level.getBlockState(pos.below()).isAir()) {
            result.add("Drop warning", "Unsupported falling block", InfoLine.Severity.DANGER);
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return;
        }
        result.add("Block entity", blockEntity.getType().toString());
        if (blockEntity instanceof Container container) {
            appendContainer(result, container);
        }
        if (blockEntity instanceof AbstractFurnaceBlockEntity furnace) {
            result.add("Input", itemName(furnace.getItem(0)));
            result.add("Fuel", itemName(furnace.getItem(1)), furnace.getItem(1).isEmpty()
                    ? InfoLine.Severity.CAUTION : InfoLine.Severity.NORMAL);
            result.add("Output", itemName(furnace.getItem(2)));
        }
        if (blockEntity instanceof BrewingStandBlockEntity brewing) {
            result.add("Ingredient", itemName(brewing.getItem(3)));
            result.add("Fuel", itemName(brewing.getItem(4)));
            int bottles = 0;
            for (int slot = 0; slot < 3; slot++) {
                if (!brewing.getItem(slot).isEmpty()) {
                    bottles++;
                }
            }
            result.add("Bottles", bottles + " / 3");
        }
        if (blockEntity instanceof HopperBlockEntity) {
            String enabled = property(state, "enabled");
            if (enabled != null) {
                result.add("Transfer", "true".equals(enabled) ? "Enabled" : "Locked by redstone",
                        "true".equals(enabled) ? InfoLine.Severity.NORMAL : InfoLine.Severity.CAUTION);
            }
            String facing = property(state, "facing");
            if (facing != null) {
                result.add("Output direction", facing);
            }
        }
        if (blockEntity instanceof BeehiveBlockEntity hive) {
            result.add("Bees", hive.getOccupantCount() + " / " + BeehiveBlockEntity.MAX_OCCUPANTS);
            result.add("Honey", BeehiveBlockEntity.getHoneyLevel(state) + " / 5");
            result.add("Smoke protection", hive.isSedated() ? "Safe" : "Missing",
                    hive.isSedated() ? InfoLine.Severity.NORMAL : InfoLine.Severity.CAUTION);
            if (hive.isFireNearby()) {
                result.add("Danger", "Fire nearby", InfoLine.Severity.DANGER);
            }
        }
        if (blockEntity instanceof BeaconBlockEntity beacon) {
            result.add("Beam", beacon.getBeamSections().isEmpty() ? "Blocked or inactive" : "Active",
                    beacon.getBeamSections().isEmpty() ? InfoLine.Severity.CAUTION : InfoLine.Severity.NORMAL);
            result.add("Beam sections", Integer.toString(beacon.getBeamSections().size()));
        }
        if (blockEntity instanceof SpawnerBlockEntity spawner) {
            result.add("Spawner", spawner.getSpawner().toString());
        }
        if (blockEntity instanceof TrialSpawnerBlockEntity trial) {
            result.add("Trial state", trial.getState().toString());
        }
        if (blockEntity instanceof ChiseledBookShelfBlockEntity shelf) {
            int books = 0;
            for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
                if (!shelf.getItem(slot).isEmpty()) books++;
            }
            result.add("Books", books + " / " + ChiseledBookShelfBlockEntity.MAX_BOOKS_IN_STORAGE);
            result.add("Last slot", shelf.getLastInteractedSlot() < 0 ? "None"
                    : Integer.toString(shelf.getLastInteractedSlot() + 1));
        }
        if (blockEntity instanceof CrafterBlockEntity crafter) {
            int enabledSlots = 0;
            for (int slot = 0; slot < crafter.getContainerSize(); slot++) {
                if (!crafter.isSlotDisabled(slot)) {
                    enabledSlots++;
                }
            }
            result.add("Enabled slots", enabledSlots + " / 9");
            result.add("Triggered", crafter.isTriggered() ? "Yes" : "No");
            result.add("Signal", Integer.toString(crafter.getRedstoneSignal()));
        }
        if (blockEntity instanceof LecternBlockEntity lectern) {
            result.add("Book", lectern.hasBook() ? lectern.getBook().getHoverName().getString() : "Empty");
            if (lectern.hasBook()) {
                result.add("Page", Integer.toString(lectern.getPage() + 1));
                result.add("Signal", Integer.toString(lectern.getRedstoneSignal()));
            }
        }
        if (blockEntity instanceof JukeboxBlockEntity jukebox) {
            result.add("Record", itemName(jukebox.getTheItem()));
            result.add("Comparator", Integer.toString(jukebox.getComparatorOutput()));
        }
    }

    private static void appendContainer(Inspection result, Container container) {
        int occupied = 0;
        int totalItems = 0;
        List<String> samples = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                occupied++;
                totalItems += stack.getCount();
                if (samples.size() < 3) {
                    samples.add(itemName(stack));
                }
            }
        }
        boolean full = occupied >= container.getContainerSize();
        result.add("Capacity", occupied + " / " + container.getContainerSize() + " slots",
                full ? InfoLine.Severity.DANGER
                        : occupied >= Math.max(1, container.getContainerSize() * 9 / 10)
                        ? InfoLine.Severity.CAUTION : InfoLine.Severity.NORMAL);
        result.add("Visible items", Integer.toString(totalItems));
        if (!samples.isEmpty()) {
            result.add("Contents", String.join(", ", samples));
        }
    }

    private static void appendStateProperties(Inspection result, BlockState state) {
        String facing = property(state, "facing");
        if (facing != null) {
            result.add("Facing", facing);
        }
        String powered = property(state, "powered");
        if (powered != null) {
            result.add("Powered", "true".equals(powered) ? "Yes" : "No");
        }
        String waterlogged = property(state, "waterlogged");
        if (waterlogged != null && "true".equals(waterlogged)) {
            result.add("Waterlogged", "Yes", InfoLine.Severity.INFORMATION);
        }
        String age = property(state, "age");
        if (age != null) {
            int maximum = maximumIntegerProperty(state, "age");
            boolean mature = maximum >= 0 && parseInteger(age) >= maximum;
            result.add("Growth stage", maximum >= 0 ? age + " / " + maximum + (mature ? " - mature" : "") : age,
                    mature ? InfoLine.Severity.INFORMATION : InfoLine.Severity.NORMAL);
        }
        String moisture = property(state, "moisture");
        if (moisture != null) {
            int value = parseInteger(moisture);
            result.add("Hydration", value <= 0 ? "Dry" : moisture,
                    value <= 0 ? InfoLine.Severity.CAUTION : InfoLine.Severity.NORMAL);
        }
        String level = property(state, "level");
        if (level != null) {
            result.add("Level", level);
        }
    }

    private static String property(BlockState state, String name) {
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals(name)) {
                return valueName(state, property);
            }
        }
        return null;
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private static int maximumIntegerProperty(BlockState state, String name) {
        for (Property<?> property : state.getProperties()) {
            if (!property.getName().equals(name)) continue;
            int maximum = -1;
            for (Object value : property.getPossibleValues()) {
                maximum = Math.max(maximum, parseInteger(String.valueOf(value)));
            }
            return maximum;
        }
        return -1;
    }

    private static int parseInteger(String value) {
        try { return Integer.parseInt(value); } catch (NumberFormatException ignored) { return -1; }
    }

    private static String itemName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "Empty";
        }
        return stack.getHoverName().getString() + (stack.getCount() > 1 ? " ×" + stack.getCount() : "");
    }
}
