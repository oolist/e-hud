package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public final class VanillaServerWarningScreen extends Screen {
    public VanillaServerWarningScreen() {
        super(Component.literal("E HUD server notice"));
    }

    @Override protected void init() {
        int y = height / 2 + 42;
        addRenderableWidget(Button.builder(Component.literal("Continue"), button -> minecraft.setScreen(null))
                .bounds(width / 2 - 154, y, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Leave server"), button ->
                minecraft.disconnect(new TitleScreen(), false))
                .bounds(width / 2 - 48, y, 96, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Continue & don't show again"), button -> {
            ConfigManager.get().doNotShowVanillaServerWarningAgain = true;
            ConfigManager.save(); minecraft.setScreen(null);
        }).bounds(width / 2 + 58, y, 154, 20).build());
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderTransparentBackground(graphics);
        int left = width / 2 - 220; int right = width / 2 + 220; int top = height / 2 - 86;
        graphics.fill(left, top, right, height / 2 + 76, 0xF00A1610);
        graphics.fill(left, top, width / 2, top + 3, 0xFFFF8A21);
        graphics.fill(width / 2, top, right, top + 3, 0xFF42F57B);
        graphics.drawCenteredString(font, "E HUD is not installed on this server", width / 2, top + 18, 0xFFFFB13B);
        graphics.drawCenteredString(font, "You can still use normal client-side inspection.", width / 2, top + 42, 0xFFFFFFFF);
        graphics.drawCenteredString(font, "Server-only data, operator policies, and teammate features will be unavailable.",
                width / 2, top + 56, 0xFFB7C8BC);
        graphics.drawCenteredString(font, "Those controls will appear dimmed and inaccessible while connected.",
                width / 2, top + 70, 0xFF94A89A);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override public boolean isPauseScreen() { return true; }
}
