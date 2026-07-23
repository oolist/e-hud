package dev.oolist.ehud.client.hud;

import net.minecraft.client.gui.GuiGraphics;

final class HudPose {
    private HudPose() { }

    static void push(GuiGraphics graphics, int x, int y, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.pose().translate(-x, -y);
    }

    static void pop(GuiGraphics graphics) { graphics.pose().popMatrix(); }
}
