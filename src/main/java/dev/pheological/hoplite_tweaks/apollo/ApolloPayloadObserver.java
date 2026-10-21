package dev.pheological.hoplite_tweaks.apollo;

import net.minecraft.network.FriendlyByteBuf;

/** Reads a copy of an incoming payload without consuming bytes needed by Lunar's decoder. */
public final class ApolloPayloadObserver {
    private ApolloPayloadObserver() {
    }

    public static byte[] copyApolloData(FriendlyByteBuf incoming) {
        try {
            FriendlyByteBuf view = new FriendlyByteBuf(incoming.duplicate());
            if (!view.readIdentifier().toString().equals("lunar:apollo")) return null;
            byte[] data = new byte[view.readableBytes()];
            view.readBytes(data);
            return data;
        } catch (RuntimeException ignored) {
            // Vanilla/Lunar retain responsibility for rejecting malformed packets.
            return null;
        }
    }
}
