package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigSession;
import net.minecraft.client.gui.screens.Screen;

final class HudEditorScreen extends AbstractHudEditorScreen {
    HudEditorScreen(Screen parent, ConfigSession session) { super(parent, session); }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return beginDrag(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return continueDrag(mouseX, mouseY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return finishDrag() || super.mouseReleased(mouseX, mouseY, button);
    }
}
