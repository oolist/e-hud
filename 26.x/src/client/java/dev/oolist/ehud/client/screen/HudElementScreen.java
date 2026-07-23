package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigSession;
import dev.oolist.ehud.client.config.HudElementSettings;
import dev.oolist.ehud.client.config.HudFeature;
import dev.oolist.ehud.client.hud.EHudRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class HudElementScreen extends Screen {
    private final Screen parent;
    private final ConfigSession session;
    private final HudFeature feature;

    HudElementScreen(Screen parent, ConfigSession session, HudFeature feature) {
        super(Component.literal(feature.title()));
        this.parent = parent;
        this.session = session;
        this.feature = feature;
    }

    @Override
    protected void init() {
        clearWidgets();
        HudElementSettings settings = settings();
        int x = width / 2 - 120;
        int y = 70;
        addRenderableWidget(Button.builder(Component.literal("Visible: " + yesNo(settings.enabled)), button -> {
            settings.enabled = !settings.enabled; changed();
        }).bounds(x, y, 240, 20).build());
        addRenderableWidget(Button.builder(Component.literal("X -"), button -> { settings.xOffset -= 2; changed(); })
                .bounds(x, y + 28, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("X: " + signed(settings.xOffset)), button -> {})
                .bounds(x + 82, y + 28, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("X +"), button -> { settings.xOffset += 2; changed(); })
                .bounds(x + 164, y + 28, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Y -"), button -> { settings.yOffset -= 2; changed(); })
                .bounds(x, y + 56, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Y: " + signed(settings.yOffset)), button -> {})
                .bounds(x + 82, y + 56, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Y +"), button -> { settings.yOffset += 2; changed(); })
                .bounds(x + 164, y + 56, 76, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Scale: " + Math.round(settings.scale * 100) + "%"), button -> {
            settings.scale += 0.25F; if (settings.scale > 2.0F) settings.scale = 0.5F; changed();
        }).bounds(x, y + 84, 240, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Color: " + colorName(settings.color)), button -> {
            settings.color = nextColor(settings.color); changed();
        }).bounds(x, y + 112, 240, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Show: " + conditionName(settings.condition)), button -> {
            settings.condition = nextCondition(settings.condition); changed();
        }).bounds(x, y + 140, 240, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset this item"), button -> {
            session.working().elementSettings.put(feature.key(), new HudElementSettings()); changed();
        }).bounds(x, y + 168, 116, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(x + 124, y + 168, 116, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EHudScreenStyle.background(graphics, width, height);
        EHudScreenStyle.header(graphics, font, width, session.working(), "CUSTOMIZE ITEM");
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, feature.title(), width / 2, 47, 0xFFFFFFFF);
        graphics.centeredText(font, feature.description(), width / 2, 58, 0xFF9FD8AE);
        if (session.working().showLivePreview) {
            EHudRenderer.renderPreview(graphics, width / 2, Math.min(height - 34, 340), session.working());
        }
    }

    @Override
    public void onClose() { VersionClientUi.setScreen(minecraft, parent); }

    private HudElementSettings settings() { return session.working().settingsFor(feature.key()); }
    private void changed() {
        session.working().recentSettings.remove(feature.key());
        session.working().recentSettings.add(0, feature.key());
        while (session.working().recentSettings.size() > 20) {
            session.working().recentSettings.remove(session.working().recentSettings.size() - 1);
        }
        session.changed(); rebuildWidgets();
    }
    private static String yesNo(boolean value) { return value ? "Yes" : "No"; }
    private static String signed(int value) { return value > 0 ? "+" + value : Integer.toString(value); }
    private static String conditionName(String value) { return value.replace('_', ' '); }
    private static String nextCondition(String value) {
        return switch (value) { case "RELEVANT" -> "ALWAYS"; case "ALWAYS" -> "DANGER_ONLY"; default -> "RELEVANT"; };
    }
    private static int nextColor(int color) {
        if (color == 0) return 0xFF42F57B;
        if (color == 0xFF42F57B) return 0xFFFF8A21;
        if (color == 0xFFFF8A21) return 0xFFFFFFFF;
        if (color == 0xFFFFFFFF) return 0xFFFF4D4D;
        return 0;
    }
    private static String colorName(int color) {
        return switch (color) { case 0xFF42F57B -> "Neon green"; case 0xFFFF8A21 -> "Neon orange";
            case 0xFFFFFFFF -> "White"; case 0xFFFF4D4D -> "Danger red"; default -> "Automatic"; };
    }
}
