package dev.oolist.ehud.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.oolist.ehud.client.screen.EHudConfigScreen;

public final class EHudModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return EHudConfigScreen::new;
    }
}
