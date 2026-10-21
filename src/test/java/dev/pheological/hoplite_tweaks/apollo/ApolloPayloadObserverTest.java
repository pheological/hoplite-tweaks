package dev.pheological.hoplite_tweaks.apollo;

import io.netty.buffer.Unpooled;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApolloPayloadObserverTest {
    @AfterEach
    void clearState() {
        ApolloState.clear();
    }

    @Test
    void cooldownCopyLeavesOriginalAvailableToNativeDecoder() {
        byte[] display = {10, 4, 'M', 'a', 'c', 'e', 18, 2, 8, 5};
        byte[] payload = envelope("lunarclient.apollo.cooldown.v1.DisplayCooldownMessage", display);
        FriendlyByteBuf incoming = packet("lunar:apollo", payload);
        try {
            int reader = incoming.readerIndex();
            int writer = incoming.writerIndex();
            int refs = incoming.refCnt();
            incoming.markReaderIndex();
            byte[] copy = ApolloPayloadObserver.copyApolloData(incoming);
            assertArrayEquals(payload, copy);
            assertEquals(reader, incoming.readerIndex());
            assertEquals(writer, incoming.writerIndex());
            assertEquals(refs, incoming.refCnt());
            ApolloProtocol.accept(copy);
            assertEquals("Mace", ApolloState.cooldowns().iterator().next().name());
            assertEquals(5000, ApolloState.cooldowns().iterator().next().durationMillis());
            assertNativeDecode(incoming, payload);
            incoming.resetReaderIndex();
            assertEquals(reader, incoming.readerIndex());
        } finally {
            incoming.release();
        }
    }

    @Test
    void restrictionMessageRemainsIntactForLunar() {
        byte[] restriction = envelope("lunarclient.apollo.modsetting.v1.OverrideModSettingsMessage",
            new byte[]{10, 8, 'f', 'r', 'e', 'e', 'l', 'o', 'o', 'k'});
        FriendlyByteBuf incoming = packet("lunar:apollo", restriction);
        try {
            ApolloProtocol.accept(ApolloPayloadObserver.copyApolloData(incoming));
            assertTrue(ApolloState.cooldowns().isEmpty());
            assertNativeDecode(incoming, restriction);
        } finally {
            incoming.release();
        }
    }

    @Test
    void unrelatedAndMalformedPacketsRemainUntouched() {
        FriendlyByteBuf other = packet("other:channel", new byte[]{1, 2, 3});
        FriendlyByteBuf malformed = new FriendlyByteBuf(Unpooled.wrappedBuffer(new byte[]{(byte) 0x80}));
        try {
            assertNull(ApolloPayloadObserver.copyApolloData(other));
            assertEquals(0, other.readerIndex());
            assertNull(ApolloPayloadObserver.copyApolloData(malformed));
            assertEquals(0, malformed.readerIndex());
        } finally {
            other.release();
            malformed.release();
        }
    }

    private static void assertNativeDecode(FriendlyByteBuf incoming, byte[] expected) {
        assertEquals("lunar:apollo", incoming.readIdentifier().toString());
        byte[] nativeData = new byte[incoming.readableBytes()];
        incoming.readBytes(nativeData);
        assertArrayEquals(expected, nativeData);
    }

    private static FriendlyByteBuf packet(String channel, byte[] data) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeIdentifier(Identifier.parse(channel));
        buffer.writeBytes(data);
        return buffer;
    }

    private static byte[] envelope(String type, byte[] data) {
        byte[] url = ("type.googleapis.com/" + type).getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(10);
        out.write(url.length);
        out.writeBytes(url);
        out.write(18);
        out.write(data.length);
        out.writeBytes(data);
        return out.toByteArray();
    }
}
