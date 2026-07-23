package dev.oolist.ehud.client.screen;

import dev.oolist.ehud.client.config.ConfigSession;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import dev.oolist.ehud.client.network.ClientServerState;

final class CompatibilityScreen extends Screen {
    private final Screen parent;
    private final ConfigSession session;

    CompatibilityScreen(Screen parent, ConfigSession session) {
        super(Component.literal("E HUD Compatibility")); this.parent = parent; this.session = session;
    }

    @Override protected void init() {
        clearWidgets(); var c = session.working(); int x = width / 2 - 160; int y = 62;
        addRenderableWidget(Button.builder(Component.literal("Vanilla-server warning: " + onOff(c.showVanillaServerWarning)), b -> {
            c.showVanillaServerWarning = !c.showVanillaServerWarning; changed();
        }).bounds(x, y, 320, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Per-server profiles: " + onOff(c.perServerProfiles)), b -> {
            c.perServerProfiles = !c.perServerProfiles; changed();
        }).bounds(x, y + 28, 320, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Respect operator policy: " + onOff(c.respectServerPolicy)), b -> {
            c.respectServerPolicy = !c.respectServerPolicy; changed();
        }).bounds(x, y + 56, 320, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Debug mode: " + onOff(c.debugMode)), b -> {
            c.debugMode = !c.debugMode; changed();
        }).bounds(x, y + 84, 320, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(width / 2 - 60, height - 55, 120, 20).build());
        Button admin = Button.builder(Component.literal("E HUD Admin"), b -> minecraft.setScreen(new AdminScreen(this)))
                .bounds(width / 2 - 80, y + 123, 160, 20).build();
        admin.active = ClientServerState.available() && ClientServerState.administrator();
        addRenderableWidget(admin);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        EHudScreenStyle.header(graphics, font, width, session.working(), "COMPATIBILITY & SERVER");
        super.render(graphics, mouseX, mouseY, delta);
        int y = 190;
        graphics.drawCenteredString(font, "Mod Menu: " + installed("modmenu"), width / 2, y, color(installed("modmenu")));
        graphics.drawCenteredString(font, "Animal HUD: " + animalHudStatus(), width / 2, y + 15,
                hasAnimalHud() ? 0xFFFF4D4D : 0xFF42F57B);
        graphics.drawCenteredString(font, "Current server: " + (ClientServerState.available() ? "E HUD available" : "client-side only"),
                width / 2, y + 30, ClientServerState.available() ? 0xFF42F57B : 0xFFFFB13B);
        graphics.drawCenteredString(font, "Remote server features are capability-checked when joining.", width / 2, y + 48, 0xFF9FD8AE);
        graphics.drawCenteredString(font, "Unavailable or operator-disabled controls become locked and clearly labelled.", width / 2, y + 56, 0xFF94A89A);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
    private void changed() { session.changed(); rebuildWidgets(); }
    private static String onOff(boolean value) { return value ? "ON" : "OFF"; }
    private static String installed(String id) { return FabricLoader.getInstance().isModLoaded(id) ? "Installed" : "Not installed"; }
    private static int color(String value) { return "Installed".equals(value) ? 0xFF42F57B : 0xFFB7C8BC; }
    private static boolean hasAnimalHud() {
        return FabricLoader.getInstance().getAllMods().stream().anyMatch(mod -> {
            String id = mod.getMetadata().getId().toLowerCase();
            String name = mod.getMetadata().getName().toLowerCase();
            return !id.equals("ehud") && (id.equals("animalhud") || id.equals("animal_hud") || name.equals("animal hud"));
        });
    }
    private static String animalHudStatus() { return hasAnimalHud() ? "INCOMPATIBLE" : "Not detected"; }
}
