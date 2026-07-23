package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigSession;
import dev.oolist.ehud.client.hud.EHudRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

abstract class AbstractHudEditorScreen extends Screen {
    private final Screen parent;
    private final ConfigSession session;
    protected boolean dragging;
    protected double lastX;
    protected double lastY;

    AbstractHudEditorScreen(Screen parent, ConfigSession session) {
        super(Component.literal("E HUD Position Editor"));
        this.parent = parent; this.session = session;
    }

    @Override
    protected void init() {
        clearWidgets();
        var c = session.working();
        addRenderableWidget(Button.builder(Component.literal("Anchor: " + c.anchor), button -> {
            c.anchor = switch (c.anchor) { case "HOTBAR_TOP" -> "TOP_CENTER"; case "TOP_CENTER" -> "TOP_LEFT";
                case "TOP_LEFT" -> "TOP_RIGHT"; default -> "HOTBAR_TOP"; }; changed();
        }).bounds(width / 2 - 160, 48, 154, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset position"), button -> {
            c.hudXOffset = 0; c.hudYOffset = 44; changed();
        }).bounds(width / 2 + 6, 48, 154, 20).build());
        addRenderableWidget(Button.builder(Component.literal("X -"), button -> { c.hudXOffset -= 4; changed(); })
                .bounds(8, height - 55, 58, 20).build());
        addRenderableWidget(Button.builder(Component.literal("X +"), button -> { c.hudXOffset += 4; changed(); })
                .bounds(70, height - 55, 58, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Y -"), button -> { c.hudYOffset = Math.max(0, c.hudYOffset - 4); changed(); })
                .bounds(width - 128, height - 55, 58, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Y +"), button -> { c.hudYOffset += 4; changed(); })
                .bounds(width - 66, height - 55, 58, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(width / 2 - 50, height - 27, 100, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EHudScreenStyle.background(graphics, width, height);
        EHudScreenStyle.header(graphics, font, width, session.working(), "LIVE DRAG EDITOR");
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int center = width / 2 + session.working().hudXOffset;
        int bottom = height - session.working().hudYOffset;
        EHudRenderer.renderPreview(graphics, center, bottom, session.working());
        graphics.centeredText(font, "Drag the preview anywhere | X " + session.working().hudXOffset
                + " | Y " + session.working().hudYOffset, width / 2, 76, 0xFF9FD8AE);
    }

    protected boolean beginDrag(double mouseX, double mouseY, int button) {
        int center = width / 2 + session.working().hudXOffset;
        int bottom = height - session.working().hudYOffset;
        if (button == 0 && mouseX >= center - 130 && mouseX <= center + 130
                && mouseY >= bottom - 95 && mouseY <= bottom) {
            dragging = true; lastX = mouseX; lastY = mouseY; return true;
        }
        return false;
    }

    protected boolean continueDrag(double mouseX, double mouseY) {
        if (dragging) {
            session.working().hudXOffset += (int) Math.round(mouseX - lastX);
            session.working().hudYOffset -= (int) Math.round(mouseY - lastY);
            lastX = mouseX; lastY = mouseY; return true;
        }
        return false;
    }

    protected boolean finishDrag() {
        if (dragging) { dragging = false; session.changed(); return true; }
        return false;
    }

    @Override public void onClose() { VersionClientUi.setScreen(minecraft, parent); }
    private void changed() { session.changed(); rebuildWidgets(); }
}
