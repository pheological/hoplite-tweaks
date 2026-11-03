package dev.pheological.hoplite_tweaks.network;

import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeammateNetworkingTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test void existingForeignCodecAndReceiverArePreserved() {
        var registry = registry();
        var type = new CustomPacketPayload.Type<ForeignPayload>(Identifier.parse("test:shared_teammates"));
        var owner = registry.register(type, StreamCodec.unit(new ForeignPayload(type)));
        var ourType = new CustomPacketPayload.Type<ResetTeammatesPayload>(type.id());
        assertFalse(TeammateNetworking.registerIfUnclaimed(registry, ourType, ResetTeammatesPayload.CODEC,
            () -> fail("Must not install our receiver for someone else's payload class")));
        assertSame(owner, ((PayloadTypeRegistryImpl<RegistryFriendlyByteBuf>) registry).get(type.id()));
    }

    @Test void absentChannelGetsFallbackCodecAndExactlyOneReceiver() {
        var registry = registry();
        var type = new CustomPacketPayload.Type<ResetTeammatesPayload>(Identifier.parse("test:fallback_teammates"));
        AtomicInteger receivers = new AtomicInteger();
        assertTrue(TeammateNetworking.registerIfUnclaimed(registry, type, ResetTeammatesPayload.CODEC,
            receivers::incrementAndGet));
        assertFalse(TeammateNetworking.registerIfUnclaimed(registry, type, ResetTeammatesPayload.CODEC,
            receivers::incrementAndGet));
        assertEquals(1, receivers.get());
    }

    @Test void sharedSnapshotCanBeDecodedAgainByItsOwner() {
        var payload = new TeammatesPayload(List.of(new TeammatesPayload.Teammate(
            UUID.randomUUID(), Component.literal("Teammate"), 0xFF123456, Level.OVERWORLD, new Vec3(1, 70, 3))));
        var buffer = packet(TeammatesPayload.TYPE.id());
        try {
            TeammatesPayload.CODEC.encode(buffer, payload);
            int reader = buffer.readerIndex();
            int writer = buffer.writerIndex();
            int refs = buffer.refCnt();
            buffer.markReaderIndex();
            assertEquals(payload, TeammateNetworking.copySharedPayload(buffer, true, true));
            assertEquals(reader, buffer.readerIndex());
            assertEquals(writer, buffer.writerIndex());
            assertEquals(refs, buffer.refCnt());
            buffer.resetReaderIndex();
            assertEquals(reader, buffer.readerIndex());
            assertNull(TeammateNetworking.copySharedPayload(buffer, false, true));
            assertEquals(TeammatesPayload.TYPE.id(), buffer.readIdentifier());
            assertEquals(payload, TeammatesPayload.CODEC.decode(buffer));
        } finally { buffer.release(); }
    }

    @Test void resetUnrelatedAndMalformedPacketsRemainUntouched() {
        var reset = packet(ResetTeammatesPayload.TYPE.id());
        var unrelated = packet(Identifier.parse("other:channel"));
        var malformed = packet(TeammatesPayload.TYPE.id());
        try {
            assertInstanceOf(ResetTeammatesPayload.class, TeammateNetworking.copySharedPayload(reset, true, true));
            assertNull(TeammateNetworking.copySharedPayload(reset, true, false));
            assertNull(TeammateNetworking.copySharedPayload(unrelated, true, true));
            assertNull(TeammateNetworking.copySharedPayload(malformed, true, true));
            assertEquals(0, reset.readerIndex());
            assertEquals(0, unrelated.readerIndex());
            assertEquals(0, malformed.readerIndex());
        } finally { reset.release(); unrelated.release(); malformed.release(); }
    }

    private static PayloadTypeRegistry<RegistryFriendlyByteBuf> registry() {
        //? >=26 {
        /*return PayloadTypeRegistry.clientboundPlay();
        *///?} else {
        return PayloadTypeRegistry.playS2C();
        //?}
    }

    private static RegistryFriendlyByteBuf packet(Identifier id) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        buffer.writeIdentifier(id);
        return buffer;
    }

    private record ForeignPayload(CustomPacketPayload.Type<ForeignPayload> type) implements CustomPacketPayload {}
}
