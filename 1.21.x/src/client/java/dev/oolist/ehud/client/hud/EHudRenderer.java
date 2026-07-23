package dev.oolist.ehud.client.hud;

import dev.oolist.ehud.client.config.ConfigManager;
import dev.oolist.ehud.client.config.EHudConfig;
import dev.oolist.ehud.client.EHudClient;
import dev.oolist.ehud.client.config.FeatureCatalog;
import dev.oolist.ehud.client.network.ClientServerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import net.minecraft.sounds.SoundEvents;

public final class EHudRenderer {
    private static Inspection pinned;
    private static Inspection cached;
    private static long lastScanTick = Long.MIN_VALUE;
    private static long lastWarningAt;
    private static String lastWarningKey = "";

    private EHudRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        EHudConfig config = ConfigManager.get();
        if (!config.enabled || client.options.hideGui || client.screen != null) {
            return;
        }
        if (pinned == null && !config.automaticInspection && !EHudClient.inspectHeld()) {
            return;
        }
        Inspection inspection = pinned != null ? pinned : cachedInspection(client, config);
        if (inspection == null) {
            return;
        }
        playWarningIfNeeded(client, inspection, config);
        renderPanel(graphics, inspection, config, false);
    }

    public static void renderPreview(GuiGraphics graphics, int centerX, int bottomY, EHudConfig config) {
        Inspection preview = new Inspection("Cow Pen", "Animal diagnostics")
                .add("Population", "14 adults · 3 babies")
                .add("Breeding", "8 ready · food for 4 pairs")
                .add("Warning", "East gate is open", InfoLine.Severity.CAUTION);
        renderPanelAt(graphics, preview, config, centerX, bottomY, true);
    }

    public static void togglePin() {
        if (pinned != null) {
            pinned = null;
        } else {
            pinned = InspectionEngine.inspect(Minecraft.getInstance());
        }
    }

    private static void renderPanel(GuiGraphics graphics, Inspection inspection, EHudConfig config,
                                    boolean preview) {
        int centerX = graphics.guiWidth() / 2 + config.hudXOffset;
        int bottomY = graphics.guiHeight() - config.hudYOffset;
        renderPanelAt(graphics, inspection, config, centerX, bottomY, preview);
    }

    private static void renderPanelAt(GuiGraphics graphics, Inspection inspection, EHudConfig config,
                                      int centerX, int bottomY, boolean preview) {
        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        List<InfoLine> lines = inspection.lines().stream()
                .filter(line -> config.isLineEnabled(line.key()))
                .filter(line -> config.modules.getOrDefault(FeatureCatalog.moduleForKey(line.key()), true))
                .filter(line -> !ClientServerState.disabledByOperator(FeatureCatalog.moduleForKey(line.key())))
                .filter(line -> !"DANGER_ONLY".equals(config.settingsFor(line.key()).condition)
                        || line.severity().ordinal() >= InfoLine.Severity.DANGER.ordinal())
                .toList();
        int defaultLimit = config.compactMode ? 3 : config.maximumLines;
        int visibleLines = preview ? Math.min(lines.size(), 5) : Math.min(lines.size(), Math.max(1, defaultLimit));
        int width = Math.max(164, font.width(inspection.title()) + 48);
        for (int index = 0; index < visibleLines; index++) {
            InfoLine line = lines.get(index);
            float lineScale = Math.max(0.5F, Math.min(2.0F, config.settingsFor(line.key()).scale));
            width = Math.max(width, Math.round((font.width(line.label() + ": " + line.value()) + 20) * lineScale));
        }
        int rowHeight = Math.max(9, Math.round(11 * config.lineSpacing));
        int height = 32 + visibleLines * rowHeight;
        int left = centerX - width / 2;
        int top = bottomY - height;

        float hudScale = preview ? 1.0F : Math.max(0.5F, Math.min(2.0F, config.hudScale));
        HudPose.push(graphics, centerX, bottomY, hudScale);
        drawGradientPanel(graphics, left, top, width, height, config);
        graphics.drawString(font, inspection.title(), left + 9, top + 7, config.textColor, true);
        graphics.drawString(font, inspection.subtitle(), left + 9, top + 18, 0xFF9FD8AE, false);

        for (int index = 0; index < visibleLines; index++) {
            InfoLine line = lines.get(index);
            var element = config.settingsFor(line.key());
            int color = element.color == 0 ? colorFor(line.severity(), config) : element.color;
            int lineX = left + config.panelPadding + element.xOffset;
            int lineY = top + 32 + index * rowHeight + element.yOffset;
            float fontMultiplier = "COMPACT".equals(config.fontStyle) ? 0.9F
                    : "HIGH_CONTRAST".equals(config.fontStyle) ? 1.1F : 1.0F;
            float lineScale = Math.max(0.5F, Math.min(2.0F, element.scale * fontMultiplier));
            HudPose.push(graphics, lineX, lineY, lineScale);
            String icon = warningIcon(config);
            if (config.showIcons && config.warningIcons && !icon.isEmpty()
                    && line.severity().ordinal() >= InfoLine.Severity.CAUTION.ordinal()) {
                graphics.drawString(font, icon, lineX, lineY, color, true);
                lineX += font.width(icon) + 3;
            }
            graphics.drawString(font, line.label() + ":", lineX, lineY, 0xFFB7C8BC, config.textShadow);
            graphics.drawString(font, line.value(), lineX + 4 + font.width(line.label() + ":"), lineY,
                    color, config.textShadow);
            HudPose.pop(graphics);
        }
        HudPose.pop(graphics);
    }

    private static void drawGradientPanel(GuiGraphics graphics, int left, int top, int width, int height,
                                          EHudConfig config) {
        int backgroundAlpha = switch (config.backgroundStyle) {
            case "SOLID" -> 255;
            case "TRANSPARENT" -> 0;
            case "SOFT" -> Math.round(config.panelOpacity * 170);
            default -> Math.round(config.panelOpacity * 255);
        };
        int panel = withAlpha(config.panelColor, backgroundAlpha);
        if (backgroundAlpha > 0) graphics.fill(left, top, left + width, top + height, panel);
        int segments = 16;
        float phase = config.animations && config.animatedGradient
                ? (System.currentTimeMillis() % 4000L) / 4000.0F : 0.0F;
        for (int index = 0; index < segments; index++) {
            float ratio = (index / (float) (segments - 1) + phase) % 1.0F;
            int color = mix(config.accentColor, config.primaryColor, ratio);
            int x1 = left + index * width / segments;
            int x2 = left + (index + 1) * width / segments;
            graphics.fill(x1, top, x2, top + 2, color);
        }
        if (!"NONE".equals(config.borderStyle)) {
            int glowAlpha = Math.round(Math.max(0.1F, Math.min(1.0F, config.neonIntensity)) * 200);
            graphics.fill(left, top + 2, left + 1, top + height, withAlpha(config.primaryColor, glowAlpha));
            graphics.fill(left + width - 1, top + 2, left + width, top + height, withAlpha(config.accentColor, glowAlpha));
            if ("DOUBLE".equals(config.borderStyle)) {
                graphics.fill(left + 2, top + 3, left + 3, top + height - 2, withAlpha(config.primaryColor, glowAlpha / 2));
                graphics.fill(left + width - 3, top + 3, left + width - 2, top + height - 2,
                        withAlpha(config.accentColor, glowAlpha / 2));
            }
        }
    }

    private static int colorFor(InfoLine.Severity severity, EHudConfig config) {
        return switch (severity) {
            case INFORMATION -> config.primaryColor;
            case CAUTION -> config.warningColor;
            case DANGER, CRITICAL -> config.dangerColor;
            default -> config.textColor;
        };
    }

    private static int mix(int from, int to, float ratio) {
        int alpha = (int) (((from >>> 24) & 0xFF) * (1 - ratio) + ((to >>> 24) & 0xFF) * ratio);
        int red = (int) (((from >>> 16) & 0xFF) * (1 - ratio) + ((to >>> 16) & 0xFF) * ratio);
        int green = (int) (((from >>> 8) & 0xFF) * (1 - ratio) + ((to >>> 8) & 0xFF) * ratio);
        int blue = (int) ((from & 0xFF) * (1 - ratio) + (to & 0xFF) * ratio);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0x00FFFFFF);
    }

    private static Inspection cachedInspection(Minecraft client, EHudConfig config) {
        long tick = client.level == null ? 0L : client.level.getGameTime();
        int interval = Math.max(1, config.scanIntervalTicks);
        if (config.adaptivePerformance && config.maximumCheckedBlocks > 8192) interval *= 2;
        if (cached == null || tick - lastScanTick >= interval || EHudClient.inspectHeld()) {
            cached = InspectionEngine.inspect(client);
            lastScanTick = tick;
        }
        return cached;
    }

    private static void playWarningIfNeeded(Minecraft client, Inspection inspection, EHudConfig config) {
        if (!config.warningSounds || client.player == null) return;
        InfoLine warning = inspection.lines().stream()
                .filter(line -> line.severity().ordinal() >= InfoLine.Severity.DANGER.ordinal())
                .findFirst().orElse(null);
        if (warning == null) return;
        String key = inspection.title() + "|" + warning.key() + "|" + warning.value();
        long now = System.currentTimeMillis();
        if (!key.equals(lastWarningKey) || now - lastWarningAt > 10_000L) {
            client.player.playSound(alertSound(config), 0.35F,
                    warning.severity() == InfoLine.Severity.CRITICAL ? 0.65F : 1.25F);
            lastWarningKey = key;
            lastWarningAt = now;
        }
    }

    private static net.minecraft.sounds.SoundEvent alertSound(EHudConfig config) {
        return switch (config.alertSound) {
            case "LEVEL_UP" -> SoundEvents.PLAYER_LEVELUP;
            case "ARROW" -> SoundEvents.ARROW_HIT_PLAYER;
            case "TOTEM" -> SoundEvents.TOTEM_USE;
            default -> SoundEvents.EXPERIENCE_ORB_PICKUP;
        };
    }

    private static String warningIcon(EHudConfig config) {
        return switch (config.iconStyle) {
            case "VANILLA" -> "[!]";
            case "MINIMAL" -> "|";
            case "TEXT_ONLY" -> "";
            default -> "!";
        };
    }
}
