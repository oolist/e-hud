package dev.oolist.ehud;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.oolist.ehud.network.EHudNetworking;

public final class EHud implements ModInitializer {
    public static final String MOD_ID = "ehud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        EHudNetworking.initialize();
        LOGGER.info("E HUD is loading.");
    }
}
