package dev.oolist.ehud.client.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;

final class HudPose {
    private HudPose() { }

    static void push(GuiGraphicsExtractor graphics, int x, int y, float scale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.pose().translate(-x, -y);
    }

    static void pop(GuiGraphicsExtractor graphics) { graphics.pose().popMatrix(); }
}
