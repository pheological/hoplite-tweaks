package dev.pheological.hoplite_tweaks.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ApolloPayload(byte[] data) implements CustomPacketPayload {
    public static final Type<ApolloPayload> TYPE =
        new Type<>(Identifier.fromNamespaceAndPath("lunar", "apollo"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ApolloPayload> CODEC =
        CustomPacketPayload.codec(ApolloPayload::write, ApolloPayload::read);

    private static void write(ApolloPayload payload, RegistryFriendlyByteBuf buffer) {
        buffer.writeBytes(payload.data);
    }

    private static ApolloPayload read(RegistryFriendlyByteBuf buffer) {
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        return new ApolloPayload(data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
