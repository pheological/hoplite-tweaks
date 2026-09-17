package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloModels;
import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.network.TeammatesPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HopliteTeammatesTest {
    @org.junit.jupiter.api.BeforeAll static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
    @AfterEach void clear() { ApolloState.clear(); }

    @Test void appliesAuthoritativeSnapshotAndReset() {
        UUID player = UUID.randomUUID();
        ResourceKey<Level> dimension = ResourceKey.create(
            Registries.DIMENSION, Identifier.fromNamespaceAndPath("minecraft", "overworld"));
        HopliteTeammates.accept(new TeammatesPayload(List.of(new TeammatesPayload.Teammate(
            player, Component.literal("Party Player"), 0xFF4097FF, dimension,
            new Vec3(12.5, 70, -4.25)
        ))));

        ApolloModels.Teammate teammate = ApolloState.teammates().iterator().next();
        assertEquals(player, teammate.playerId());
        assertEquals("Party Player", teammate.displayName());
        assertEquals("minecraft:overworld", teammate.world());
        assertEquals(0xFF4097FF, teammate.color());
        assertEquals(new Vec3(12.5, 70, -4.25), ApolloState.teammatePosition(teammate));

        HopliteTeammates.reset();
        assertTrue(ApolloState.teammates().isEmpty());
    }

    @Test void replacementDropsPlayersMissingFromNextSnapshot() {
        ResourceKey<Level> dimension = ResourceKey.create(
            Registries.DIMENSION, Identifier.fromNamespaceAndPath("minecraft", "the_nether"));
        TeammatesPayload.Teammate first = new TeammatesPayload.Teammate(
            UUID.randomUUID(), Component.literal("First"), 0, dimension, Vec3.ZERO);
        TeammatesPayload.Teammate second = new TeammatesPayload.Teammate(
            UUID.randomUUID(), Component.literal("Second"), 0, dimension, Vec3.ZERO);
        HopliteTeammates.accept(new TeammatesPayload(List.of(first, second)));
        HopliteTeammates.accept(new TeammatesPayload(List.of(second)));
        assertFalse(ApolloState.isTeammate(first.playerId()));
        assertTrue(ApolloState.isTeammate(second.playerId()));
        assertEquals(first.playerId(), ApolloState.lastKnownTeammates().iterator().next()
            .teammate().playerId());
    }

    @Test void packetCodecRoundTripsTheReferenceWireShape() {
        ResourceKey<Level> dimension = ResourceKey.create(
            Registries.DIMENSION, Identifier.fromNamespaceAndPath("minecraft", "overworld"));
        TeammatesPayload payload = new TeammatesPayload(List.of(new TeammatesPayload.Teammate(
            UUID.randomUUID(), Component.literal("King"), 0xFFFFD400, dimension,
            new Vec3(1.25, 72, -9.5)
        )));
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        TeammatesPayload.CODEC.encode(buffer, payload);
        TeammatesPayload decoded = TeammatesPayload.CODEC.decode(buffer);
        assertEquals(payload, decoded);
        assertEquals(0, buffer.readableBytes());
    }
}
