package dev.oolist.ehud.client;

import dev.oolist.ehud.EHud;
import dev.oolist.ehud.client.config.ConfigManager;
import dev.oolist.ehud.client.hud.EHudRenderer;
import dev.oolist.ehud.client.screen.EHudConfigScreen;
import dev.oolist.ehud.client.network.ClientServerState;
import dev.oolist.ehud.client.screen.AnimalHudConflictScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;

public final class EHudClient implements ClientModInitializer {
    private static KeyMapping openConfig;
    private static KeyMapping inspect;
    private static KeyMapping pin;

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        ClientServerState.initialize();
        registerKeys();
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> EHudRenderer.render(graphics));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            AnimalHudConflictScreen.check(client);
            ClientServerState.tick(client);
            while (openConfig.consumeClick()) {
                client.setScreen(new EHudConfigScreen(client.screen));
            }
            while (pin.consumeClick()) {
                EHudRenderer.togglePin();
            }
        });
        registerOptionsFallback();
        EHud.LOGGER.info("E HUD client initialized with profile {}.", ConfigManager.get().activeProfile);
    }

    public static boolean inspectHeld() {
        return inspect != null && inspect.isDown();
    }

    private static void registerKeys() {
        KeyMapping[] bindings = VersionKeyBindings.create();
        openConfig = KeyBindingHelper.registerKeyBinding(bindings[0]);
        inspect = KeyBindingHelper.registerKeyBinding(bindings[1]);
        pin = KeyBindingHelper.registerKeyBinding(bindings[2]);
    }

    private static void registerOptionsFallback() {
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            return;
        }
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof OptionsScreen) {
                Button button = Button.builder(Component.literal("E HUD Settings"),
                                pressed -> client.setScreen(new EHudConfigScreen(screen)))
                        .bounds(scaledWidth - 112, 8, 104, 20)
                        .build();
                Screens.getButtons(screen).add(button);
            }
        });
    }
}
