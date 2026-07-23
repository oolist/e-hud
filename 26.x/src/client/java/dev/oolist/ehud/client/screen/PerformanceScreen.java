package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigSession;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class PerformanceScreen extends Screen {
    private final Screen parent;
    private final ConfigSession session;

    PerformanceScreen(Screen parent, ConfigSession session) {
        super(Component.literal("E HUD Performance"));
        this.parent = parent; this.session = session;
    }

    @Override protected void init() {
        clearWidgets();
        var c = session.working();
        int x = width / 2 - 150; int y = 58;
        addRenderableWidget(Button.builder(Component.literal("Adaptive performance: " + onOff(c.adaptivePerformance)), b -> {
            c.adaptivePerformance = !c.adaptivePerformance; changed();
        }).bounds(x, y, 300, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Scan distance: " + distance(c.scanDistance)), b -> {
            c.scanDistance = nextDistance(c.scanDistance); changed();
        }).bounds(x, y + 28, 300, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Refresh: every " + c.scanIntervalTicks + " ticks"), b -> {
            c.scanIntervalTicks = c.scanIntervalTicks >= 40 ? 1 : c.scanIntervalTicks == 1 ? 5 : c.scanIntervalTicks + 5; changed();
        }).bounds(x, y + 56, 300, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Maximum checked blocks: " + c.maximumCheckedBlocks), b -> {
            c.maximumCheckedBlocks *= 2; if (c.maximumCheckedBlocks > 32768) c.maximumCheckedBlocks = 512; changed();
        }).bounds(x, y + 84, 300, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Performance warnings: " + onOff(c.performanceWarnings)), b -> {
            c.performanceWarnings = !c.performanceWarnings; changed();
        }).bounds(x, y + 112, 300, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Automatic backups: " + onOff(c.weeklyBackups)), b -> {
            c.weeklyBackups = !c.weeklyBackups; changed();
        }).bounds(x, y + 151, 147, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Every " + c.backupIntervalDays + " days"), b -> {
            c.backupIntervalDays = c.backupIntervalDays >= 30 ? 1 : c.backupIntervalDays + 1; changed();
        }).bounds(x + 153, y + 151, 147, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(width / 2 - 60, height - 55, 120, 20).build());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EHudScreenStyle.background(graphics, width, height);
        EHudScreenStyle.header(graphics, font, width, session.working(), "PERFORMANCE & BACKUPS");
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, "Default scan range follows your simulation distance and never exceeds it.",
                width / 2, 236, 0xFF9FD8AE);
        graphics.centeredText(font, "Higher limits may cost performance; adaptive mode can reduce work automatically.",
                width / 2, 250, 0xFF94A89A);
    }

    @Override public void onClose() { VersionClientUi.setScreen(minecraft, parent); }
    private void changed() { session.changed(); rebuildWidgets(); }
    private static String onOff(boolean value) { return value ? "ON" : "OFF"; }
    private static String distance(int value) { return value < 0 ? "Simulation distance" : value + " blocks"; }
    private static int nextDistance(int value) { return switch (value) { case -1 -> 16; case 16 -> 32; case 32 -> 64; case 64 -> 128; default -> -1; }; }
}
