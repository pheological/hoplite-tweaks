package dev.pheological.hoplite_tweaks.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Hoplite's Diana/Artemis signal that the current teammate snapshot is invalid. */
public record ResetTeammatesPayload() implements CustomPacketPayload {
    public static final Type<ResetTeammatesPayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath("hoplite-addons", "reset_teammates"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResetTeammatesPayload> CODEC =
        StreamCodec.unit(new ResetTeammatesPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
