package dev.oolist.ehud.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record AdminUpdatePayload(String disabledModules) implements CustomPacketPayload {
    public static final Type<AdminUpdatePayload> TYPE = CustomPacketPayload.createType("ehud:admin_update");
    public static final StreamCodec<RegistryFriendlyByteBuf, AdminUpdatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AdminUpdatePayload::disabledModules, AdminUpdatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
