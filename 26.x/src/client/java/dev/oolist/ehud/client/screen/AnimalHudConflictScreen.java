package dev.oolist.ehud.client.screen;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public final class AnimalHudConflictScreen extends Screen {
    private static boolean shown;

    public AnimalHudConflictScreen() {
        super(Component.literal("Incompatible mods"));
    }

    public static void check(Minecraft client) {
        if (!shown && VersionClientUi.currentScreen(client) instanceof TitleScreen && hasAnimalHud()) {
            shown = true;
            VersionClientUi.setScreen(client, new AnimalHudConflictScreen());
        }
    }

    @Override protected void init() {
        int y = height / 2 + 38;
        addRenderableWidget(Button.builder(Component.literal("Exit and remove Animal HUD"), button -> minecraft.stop())
                .bounds(width / 2 - 152, y, 196, 20).build());
        addRenderableWidget(Button.builder(Component.literal("No - close game"), button -> minecraft.stop())
                .bounds(width / 2 + 52, y, 100, 20).build());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        EHudScreenStyle.background(graphics, width, height);
        int left = width / 2 - 220; int right = width / 2 + 220; int top = height / 2 - 82;
        graphics.fill(left, top, right, height / 2 + 73, 0xF00A1610);
        graphics.fill(left, top, right, top + 3, 0xFFFF4D4D);
        graphics.centeredText(font, "Animal HUD is not compatible with E HUD", width / 2, top + 18, 0xFFFF4D4D);
        graphics.centeredText(font, "Both mods draw and control overlapping animal information.", width / 2,
                top + 44, 0xFFFFFFFF);
        graphics.centeredText(font, "Fabric cannot safely unload a mod while the game is running.", width / 2,
                top + 58, 0xFFB7C8BC);
        graphics.centeredText(font, "Remove Animal HUD from the mods folder, then restart Minecraft.", width / 2,
                top + 72, 0xFF9FD8AE);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public void onClose() { }

    private static boolean hasAnimalHud() {
        return FabricLoader.getInstance().getAllMods().stream().anyMatch(mod -> {
            String id = mod.getMetadata().getId().toLowerCase();
            String name = mod.getMetadata().getName().toLowerCase();
            return !id.equals("ehud") && (id.equals("animalhud") || id.equals("animal_hud") || name.equals("animal hud"));
        });
    }
}
