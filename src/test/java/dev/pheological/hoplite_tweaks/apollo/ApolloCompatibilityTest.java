package dev.pheological.hoplite_tweaks.apollo;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ApolloCompatibilityTest {
    @TempDir Path clientFiles;

    @Test
    void ordinaryFabricRetainsCooldownChannel() throws Exception {
        try (var loader = new URLClassLoader(new URL[]{clientFiles.toUri().toURL()}, null)) {
            assertTrue(ApolloCompatibility.shouldRegisterChannel(loader, false));
            assertFalse(ApolloCompatibility.observeNativeChannel());
        }
    }

    @Test
    void anotherModKeepsItsCodecAndEnablesPassiveObservation() throws Exception {
        try (var loader = new URLClassLoader(new URL[]{clientFiles.toUri().toURL()}, null)) {
            assertFalse(ApolloCompatibility.shouldRegisterChannel(loader, true));
            assertTrue(ApolloCompatibility.observeNativeChannel());
        }
    }

    @Test
    void lunarClientKeepsOwnershipOfServerRestrictionChannel() throws Exception {
        Path marker = clientFiles.resolve("assets/lunar/apollo/button_circle.png");
        Files.createDirectories(marker.getParent());
        Files.write(marker, new byte[0]);
        try (var loader = new URLClassLoader(new URL[]{clientFiles.toUri().toURL()}, null)) {
            assertFalse(ApolloCompatibility.shouldRegisterChannel(loader, false));
            assertTrue(ApolloCompatibility.observeNativeChannel());
        }
    }
    @Test
    void foreignApolloPayloadClassKeepsItsCodecAndReceiver() throws Exception {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        //? >=26 {
        /*var registry = net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.clientboundPlay();
        *///?} else {
        var registry = net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C();
        //?}
        var type = new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ForeignPayload>(
            dev.pheological.hoplite_tweaks.network.ApolloPayload.TYPE.id());
        var owner = registry.register(type, net.minecraft.network.codec.StreamCodec.unit(new ForeignPayload(type)));
        try (var loader = new URLClassLoader(new URL[]{clientFiles.toUri().toURL()}, null)) {
            assertFalse(ApolloCompatibility.registerChannel(loader, registry,
                () -> fail("Cannot install our receiver for a foreign decoded payload class")));
            assertTrue(ApolloCompatibility.observeNativeChannel());
            assertSame(owner, ((net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl<?>) registry).get(type.id()));
        }
    }

    private record ForeignPayload(net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<ForeignPayload> type)
            implements net.minecraft.network.protocol.common.custom.CustomPacketPayload {}

}
