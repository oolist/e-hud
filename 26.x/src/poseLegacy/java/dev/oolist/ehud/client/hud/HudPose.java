package dev.oolist.ehud.client.hud;

import net.minecraft.client.gui.GuiGraphics;

final class HudPose {
    private HudPose() { }

    static void push(GuiGraphics graphics, int x, int y, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.pose().translate(-x, -y, 0.0F);
    }

    static void pop(GuiGraphics graphics) { graphics.pose().popPose(); }
}
