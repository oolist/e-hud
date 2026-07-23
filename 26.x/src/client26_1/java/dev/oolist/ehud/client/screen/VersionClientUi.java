package dev.oolist.ehud.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class VersionClientUi {
    private VersionClientUi() { }

    public static Screen currentScreen(Minecraft client) {
        return client.screen;
    }

    public static void setScreen(Minecraft client, Screen screen) {
        client.setScreen(screen);
    }

    public static boolean hudHidden(Minecraft client) {
        return client.options.hideGui;
    }
}
