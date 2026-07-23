package dev.oolist.ehud.client.network;

import dev.oolist.ehud.client.config.ConfigManager;
import dev.oolist.ehud.client.config.HudModule;
import dev.oolist.ehud.client.config.EHudConfig;
import dev.oolist.ehud.client.screen.VanillaServerWarningScreen;
import dev.oolist.ehud.network.AdminUpdatePayload;
import dev.oolist.ehud.network.ServerPolicyPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class ClientServerState {
    private static final Set<HudModule> disabled = EnumSet.noneOf(HudModule.class);
    private static boolean available;
    private static boolean administrator;
    private static int warningDelay = -1;
    private static EHudConfig configBeforeScopedProfile;

    private ClientServerState() {
    }

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(ServerPolicyPayload.TYPE, (payload, context) ->
                context.client().execute(() -> apply(payload)));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            applyScopedProfile(client);
            available = ClientPlayNetworking.canSend(AdminUpdatePayload.TYPE);
            administrator = false;
            disabled.clear();
            warningDelay = !available && shouldWarn() ? 20 : -1;
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (configBeforeScopedProfile != null) {
                ConfigManager.replace(configBeforeScopedProfile);
                configBeforeScopedProfile = null;
            }
            available = false; administrator = false; disabled.clear(); warningDelay = -1;
        });
    }

    public static void tick(Minecraft client) {
        if (warningDelay < 0 || client.level == null) return;
        if (available) { warningDelay = -1; return; }
        if (--warningDelay == 0 && client.screen == null) {
            warningDelay = -1;
            client.setScreen(new VanillaServerWarningScreen());
        }
    }

    public static boolean available() { return available; }
    public static boolean administrator() { return administrator; }
    public static boolean disabledByOperator(HudModule module) { return available && disabled.contains(module); }
    public static Set<HudModule> disabledModules() { return disabled.isEmpty() ? EnumSet.noneOf(HudModule.class) : EnumSet.copyOf(disabled); }

    public static String currentProfileKey(Minecraft client) {
        if (client.getCurrentServer() != null) return "server:" + client.getCurrentServer().ip;
        if (client.getSingleplayerServer() != null) {
            try {
                Object data = client.getSingleplayerServer().getClass().getMethod("getWorldData")
                        .invoke(client.getSingleplayerServer());
                Object name = data.getClass().getMethod("getLevelName").invoke(data);
                return "world:" + name;
            } catch (ReflectiveOperationException ignored) {
                return "world:singleplayer";
            }
        }
        return null;
    }

    public static void sendAdminPolicy(Set<HudModule> modules) {
        if (!available || !administrator || !ClientPlayNetworking.canSend(AdminUpdatePayload.TYPE)) return;
        String encoded = modules.stream().map(module -> module.name().toLowerCase(Locale.ROOT))
                .sorted().collect(Collectors.joining(","));
        ClientPlayNetworking.send(new AdminUpdatePayload(encoded));
    }

    private static void apply(ServerPolicyPayload payload) {
        available = true;
        warningDelay = -1;
        administrator = payload.administrator();
        disabled.clear();
        Arrays.stream(payload.disabledModules().split(",")).map(String::trim).filter(value -> !value.isBlank())
                .forEach(value -> {
                    try { disabled.add(HudModule.valueOf(value.toUpperCase(Locale.ROOT))); }
                    catch (IllegalArgumentException ignored) { }
                });
    }

    private static boolean shouldWarn() {
        var config = ConfigManager.get();
        return config.showVanillaServerWarning && !config.doNotShowVanillaServerWarningAgain;
    }

    private static void applyScopedProfile(Minecraft client) {
        EHudConfig global = ConfigManager.get();
        if (!global.perServerProfiles) return;
        String key = currentProfileKey(client);
        String profile = key == null ? null : global.serverProfiles.get(key);
        if (profile == null || profile.isBlank()) return;
        try {
            EHudConfig scoped = ConfigManager.importPreset(ConfigManager.presetByName(profile));
            scoped.serverProfiles = new java.util.LinkedHashMap<>(global.serverProfiles);
            configBeforeScopedProfile = global.copy();
            ConfigManager.replace(scoped);
        } catch (java.io.IOException ignored) { }
    }
}
