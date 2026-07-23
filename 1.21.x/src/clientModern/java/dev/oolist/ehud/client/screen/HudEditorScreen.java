package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigSession;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

final class HudEditorScreen extends AbstractHudEditorScreen {
    HudEditorScreen(Screen parent, ConfigSession session) { super(parent, session); }

    @Override public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return beginDrag(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);
    }

    @Override public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return continueDrag(event.x(), event.y()) || super.mouseDragged(event, dragX, dragY);
    }

    @Override public boolean mouseReleased(MouseButtonEvent event) {
        return finishDrag() || super.mouseReleased(event);
    }
}
