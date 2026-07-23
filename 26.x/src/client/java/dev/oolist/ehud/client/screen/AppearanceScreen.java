package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigSession;
import dev.oolist.ehud.client.config.EHudConfig;
import dev.oolist.ehud.client.hud.EHudRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

final class AppearanceScreen extends Screen {
    private final Screen parent;
    private final ConfigSession session;
    private EditBox primary;
    private EditBox accent;
    private EditBox panel;
    private String status = "Use 8-digit ARGB hex colors";

    AppearanceScreen(Screen parent, ConfigSession session) {
        super(Component.literal("E HUD Appearance"));
        this.parent = parent;
        this.session = session;
    }

    @Override
    protected void init() {
        clearWidgets();
        EHudConfig config = session.working();
        int left = width / 2 - 210;
        int top = 58;
        primary = colorBox(left + 116, top, config.primaryColor, "Primary color");
        accent = colorBox(left + 116, top + 28, config.accentColor, "Accent color");
        panel = colorBox(left + 116, top + 56, config.panelColor, "Panel color");
        addRenderableWidget(primary); addRenderableWidget(accent); addRenderableWidget(panel);

        int right = width / 2 + 8;
        addRenderableWidget(Button.builder(Component.literal("Gradient: " + config.gradientName), button -> {
            cycleGradient(); rebuildWidgets();
        }).bounds(right, top, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Animation: " + onOff(config.animations)), button -> {
            config.animations = !config.animations; changed();
        }).bounds(right, top + 28, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Animated gradient: " + onOff(config.animatedGradient)), button -> {
            config.animatedGradient = !config.animatedGradient; changed();
        }).bounds(right, top + 56, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Panel opacity: " + Math.round(config.panelOpacity * 100) + "%"), button -> {
            config.panelOpacity += 0.1F; if (config.panelOpacity > 1.01F) config.panelOpacity = 0.4F; changed();
        }).bounds(left, top + 93, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("HUD scale: " + Math.round(config.hudScale * 100) + "%"), button -> {
            config.hudScale += 0.1F; if (config.hudScale > 1.51F) config.hudScale = 0.6F; changed();
        }).bounds(right, top + 93, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Border: " + config.borderStyle), button -> {
            config.borderStyle = cycle(config.borderStyle, "THIN_GLOW", "SOLID", "DOUBLE", "NONE"); changed();
        }).bounds(left, top + 121, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Icons: " + config.iconStyle), button -> {
            config.iconStyle = cycle(config.iconStyle, "NEON", "VANILLA", "MINIMAL", "TEXT_ONLY"); changed();
        }).bounds(right, top + 121, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Font: " + config.fontStyle), button -> {
            config.fontStyle = cycle(config.fontStyle, "MINECRAFT", "COMPACT", "HIGH_CONTRAST"); changed();
        }).bounds(left, top + 149, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Text shadow: " + onOff(config.textShadow)), button -> {
            config.textShadow = !config.textShadow; changed();
        }).bounds(right, top + 149, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Background: " + config.backgroundStyle), button -> {
            config.backgroundStyle = cycle(config.backgroundStyle, "DARK_GLASS", "SOLID", "SOFT", "TRANSPARENT"); changed();
        }).bounds(left, top + 177, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Warning icons: " + onOff(config.warningIcons)), button -> {
            config.warningIcons = !config.warningIcons; changed();
        }).bounds(right, top + 177, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Warning sounds: " + onOff(config.warningSounds)), button -> {
            config.warningSounds = !config.warningSounds; changed();
        }).bounds(left, top + 205, 202, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Alert sound: " + config.alertSound), button -> {
            config.alertSound = cycle(config.alertSound, "EXPERIENCE_ORB", "LEVEL_UP", "ARROW", "TOTEM"); changed();
        }).bounds(right, top + 205, 202, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Apply typed colors"), button -> applyColors())
                .bounds(width / 2 - 125, height - 55, 120, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> { applyColors(); onClose(); })
                .bounds(width / 2 + 5, height - 55, 120, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EHudScreenStyle.background(graphics, width, height);
        EHudScreenStyle.header(graphics, font, width, session.working(), "APPEARANCE");
        graphics.text(font, "Primary", width / 2 - 210, 64, 0xFFB7C8BC, false);
        graphics.text(font, "Accent", width / 2 - 210, 92, 0xFFB7C8BC, false);
        graphics.text(font, "Panel", width / 2 - 210, 120, 0xFFB7C8BC, false);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        if (session.working().showLivePreview) {
            EHudRenderer.renderPreview(graphics, width / 2, height - 78, session.working());
        }
        graphics.centeredText(font, status, width / 2, height - 31, 0xFF9FD8AE);
    }

    @Override
    public void onClose() { VersionClientUi.setScreen(minecraft, parent); }

    private EditBox colorBox(int x, int y, int value, String hint) {
        EditBox box = new EditBox(font, x, y, 94, 20, Component.literal(hint));
        box.setMaxLength(9); box.setValue(String.format("%08X", value));
        return box;
    }

    private void applyColors() {
        try {
            int nextPrimary = parseColor(primary.getValue());
            int nextAccent = parseColor(accent.getValue());
            int nextPanel = parseColor(panel.getValue());
            EHudConfig config = session.working();
            if (config.primaryColor != nextPrimary) { config.primaryColor = nextPrimary; session.changed(); }
            if (config.accentColor != nextAccent) { config.accentColor = nextAccent; session.changed(); }
            if (config.panelColor != nextPanel) { config.panelColor = nextPanel; session.changed(); }
            config.gradientName = "Custom";
            config.gradientColors = List.of(config.accentColor, config.primaryColor);
            status = "Custom colors staged";
        } catch (NumberFormatException exception) {
            status = "Invalid color; use AARRGGBB such as FF42F57B";
        }
    }

    private void cycleGradient() {
        EHudConfig c = session.working();
        switch (c.gradientName) {
            case "Neon Grove" -> setGradient("Sunset Circuit", 0xFFFF3D81, 0xFFFFA321);
            case "Sunset Circuit" -> setGradient("Emerald Pulse", 0xFF00F5A0, 0xFF00D9F5);
            case "Emerald Pulse" -> setGradient("Firefly", 0xFFFFF338, 0xFF47FF68);
            default -> setGradient("Neon Grove", 0xFFFF8A21, 0xFF42F57B);
        }
    }

    private void setGradient(String name, int accentColor, int primaryColor) {
        EHudConfig c = session.working(); c.gradientName = name; c.accentColor = accentColor;
        c.primaryColor = primaryColor; c.gradientColors = List.of(accentColor, primaryColor); session.changed();
    }

    private void changed() { session.changed(); rebuildWidgets(); }
    private static int parseColor(String value) { return (int) Long.parseLong(value.replace("#", ""), 16); }
    private static String onOff(boolean value) { return value ? "ON" : "OFF"; }
    private static String cycle(String current, String... values) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(current)) return values[(i + 1) % values.length];
        return values[0];
    }
}
