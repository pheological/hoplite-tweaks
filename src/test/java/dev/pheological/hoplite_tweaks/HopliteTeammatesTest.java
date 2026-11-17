package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloModels;
import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
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

    @Test void appliesApolloSnapshotAndReset() {
        var member = new ApolloModels.Teammate(UUID.randomUUID(), 12.5, 70, -4.25,
            "overworld", "Party Player", 0xFF4097FF, 1);
        HopliteTeammates.accept(List.of(member));
        assertEquals(member, ApolloState.teammates().iterator().next());
        HopliteTeammates.reset();
        assertTrue(ApolloState.teammates().isEmpty());
    }

    @Test void replacementDropsPlayersMissingFromNextSnapshot() {
        var first = new ApolloModels.Teammate(UUID.randomUUID(), 0, 64, 0,
            "overworld", "First", 0, 1);
        var second = new ApolloModels.Teammate(UUID.randomUUID(), 0, 64, 0,
            "overworld", "Second", 0, 1);
        HopliteTeammates.accept(List.of(first, second));
        HopliteTeammates.accept(List.of(second));
        assertFalse(ApolloState.isTeammate(first.playerId()));
        assertTrue(ApolloState.isTeammate(second.playerId()));
        assertEquals(first.playerId(), ApolloState.lastKnownTeammates().iterator().next()
            .teammate().playerId());
    }
}
