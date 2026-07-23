package dev.oolist.ehud.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class EHudNetworking {
    private static final EHudServerPolicy POLICY = EHudServerPolicy.load();

    private EHudNetworking() {
    }

    public static void initialize() {
        PayloadTypeRegistry.playS2C().register(ServerPolicyPayload.TYPE, ServerPolicyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(AdminUpdatePayload.TYPE, AdminUpdatePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AdminUpdatePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                if (!isAdministrator(player)) return;
                POLICY.replace(payload.disabledModules());
                broadcastPolicy(context.server());
            });
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                sender.sendPacket(new ServerPolicyPayload(POLICY.encoded(), isAdministrator(handler.player))));
    }

    private static boolean isAdministrator(ServerPlayer player) {
        try {
            // Minecraft 1.21.11 introduced permission objects.
            Object permissionSet = player.getClass().getMethod("permissions").invoke(player);
            Class<?> permissions = Class.forName("net.minecraft.server.permissions.Permissions");
            Object gameMaster = permissions.getField("COMMANDS_GAMEMASTER").get(null);
            for (java.lang.reflect.Method method : permissionSet.getClass().getMethods()) {
                if (method.getName().equals("hasPermission") && method.getParameterCount() == 1) {
                    return (boolean) method.invoke(permissionSet, gameMaster);
                }
            }
            return false;
        } catch (ReflectiveOperationException ignored) {
            try {
                // Minecraft 1.21 through 1.21.10 use numeric operator levels.
                return (boolean) player.getClass().getMethod("hasPermissions", int.class).invoke(player, 2);
            } catch (ReflectiveOperationException unavailable) {
                return false;
            }
        }
    }

    private static void broadcastPolicy(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (ServerPlayNetworking.canSend(player, ServerPolicyPayload.TYPE)) {
                ServerPlayNetworking.send(player, new ServerPolicyPayload(POLICY.encoded(), isAdministrator(player)));
            }
        }
    }
}
