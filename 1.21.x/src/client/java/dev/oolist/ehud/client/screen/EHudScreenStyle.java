package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.EHudConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

final class EHudScreenStyle {
    private EHudScreenStyle() {
    }

    static void header(GuiGraphics graphics, Font font, int width, EHudConfig config, String section) {
        graphics.fill(0, 0, width, 38, 0xE60A1610);
        graphics.fill(0, 36, width / 2, 38, config.accentColor);
        graphics.fill(width / 2, 36, width, 38, config.primaryColor);
        graphics.drawString(font, "E HUD", 14, 10, 0xFFFFFFFF, true);
        graphics.drawString(font, section + "  |  Everything, exactly when it matters", 14, 23,
                0xFF9FD8AE, false);
    }
}
