package dev.oolist.ehud.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerPolicyPayload(String disabledModules, boolean administrator) implements CustomPacketPayload {
    public static final Type<ServerPolicyPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("ehud", "server_policy"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerPolicyPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerPolicyPayload::disabledModules,
            ByteBufCodecs.BOOL, ServerPolicyPayload::administrator,
            ServerPolicyPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
