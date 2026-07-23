package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.EHudConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class EHudScreenStyle {
    private EHudScreenStyle() {
    }

    static void background(GuiGraphicsExtractor graphics, int width, int height) {
        // Minecraft 26.x permits only one GUI blur request per frame. Other mods or
        // the parent screen may already own it, so E HUD draws its own background.
        graphics.fill(0, 0, width, height, 0xF20B100D);
    }

    static void header(GuiGraphicsExtractor graphics, Font font, int width, EHudConfig config, String section) {
        graphics.fill(0, 0, width, 38, 0xE60A1610);
        graphics.fill(0, 36, width / 2, 38, config.accentColor);
        graphics.fill(width / 2, 36, width, 38, config.primaryColor);
        graphics.text(font, "E HUD", 14, 10, 0xFFFFFFFF, true);
        graphics.text(font, section + "  |  Everything, exactly when it matters", 14, 23,
                0xFF9FD8AE, false);
    }
}
