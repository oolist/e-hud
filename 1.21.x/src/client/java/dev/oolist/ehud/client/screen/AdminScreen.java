package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.HudModule;
import dev.oolist.ehud.client.network.ClientServerState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.EnumSet;
import java.util.Set;

final class AdminScreen extends Screen {
    private static final int PAGE_SIZE = 8;
    private final Screen parent;
    private final Set<HudModule> disabled = EnumSet.noneOf(HudModule.class);
    private int page;

    AdminScreen(Screen parent) {
        super(Component.literal("E HUD Admin")); this.parent = parent;
        disabled.addAll(ClientServerState.disabledModules());
    }

    @Override protected void init() {
        clearWidgets(); HudModule[] modules = HudModule.values(); int start = page * PAGE_SIZE;
        for (int row = 0; row < PAGE_SIZE && start + row < modules.length; row++) {
            HudModule module = modules[start + row];
            addRenderableWidget(Button.builder(Component.literal((disabled.contains(module) ? "DISABLED  " : "ALLOWED  ")
                    + module.title()), button -> { if (!disabled.remove(module)) disabled.add(module); rebuildWidgets(); })
                    .bounds(width / 2 - 180, 53 + row * 27, 360, 20).build());
        }
        int bottom = height - 27;
        addRenderableWidget(Button.builder(Component.literal("Previous"), b -> { page = Math.max(0, page - 1); rebuildWidgets(); })
                .bounds(width / 2 - 160, bottom, 90, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Apply policy"), b -> {
            ClientServerState.sendAdminPolicy(disabled); onClose();
        }).bounds(width / 2 - 55, bottom, 110, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Next"), b -> { page = Math.min(1, page + 1); rebuildWidgets(); })
                .bounds(width / 2 + 70, bottom, 90, 20).build());
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.fill(0, 0, width, 38, 0xE60A1610); graphics.fill(0, 36, width / 2, 38, 0xFFFF8A21);
        graphics.fill(width / 2, 36, width, 38, 0xFF42F57B);
        graphics.drawString(font, "E HUD ADMIN", 14, 11, 0xFFFFFFFF, true);
        graphics.drawString(font, "Disabled features are locked on every connected E HUD client", 14, 23, 0xFF9FD8AE, false);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
}
