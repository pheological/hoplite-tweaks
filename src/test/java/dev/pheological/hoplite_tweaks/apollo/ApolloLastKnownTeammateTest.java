package dev.pheological.hoplite_tweaks.apollo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApolloLastKnownTeammateTest {
    private static final UUID PLAYER = UUID.randomUUID();

    @AfterEach
    void clear() {
        ApolloState.clear();
    }

    @Test
    void omissionArchivesTheLastReportedSampleWithoutRefreshingIt() {
        ApolloModels.Teammate sample = sample("minecraft:overworld", 12.0, 1_000L);
        ApolloState.observeProfile(PLAYER, "Example_Player");
        ApolloState.replaceTeammatesAuthoritative(List.of(sample));
        ApolloState.replaceTeammatesAuthoritative(List.of());

        ApolloModels.LastKnownTeammate archived = ApolloState.lastKnownTeammates().iterator().next();
        assertEquals(sample, archived.teammate());
        assertEquals("Example_Player", archived.profileName());

        ApolloState.replaceTeammatesAuthoritative(List.of());
        assertEquals(sample, ApolloState.lastKnownTeammates().iterator().next().teammate());
    }

    @Test
    void dimensionChangesDoNotLookLikeDisconnects() {
        ApolloModels.Teammate overworld = sample("minecraft:overworld", 10.0, 1_000L);
        ApolloModels.Teammate nether = sample("minecraft:the_nether", 20.0, 2_000L);
        ApolloState.replaceTeammatesAuthoritative(List.of(overworld));
        ApolloState.replaceTeammatesAuthoritative(List.of(nether));

        assertEquals("minecraft:the_nether", ApolloState.teammates().iterator().next().world());
        assertTrue(ApolloState.lastKnownTeammates().isEmpty());

        ApolloModels.Teammate returned = sample("minecraft:overworld", 30.0, 3_000L);
        ApolloState.replaceTeammatesAuthoritative(List.of(returned));
        assertTrue(ApolloState.lastKnownTeammates().isEmpty());
    }

    @Test
    void omissionOnlyArchivesAPlayerMissingFromTheTabList() {
        ApolloModels.Teammate sample = sample("minecraft:overworld", 12.0, 1_000L);
        ApolloState.replaceTeammatesAuthoritative(List.of(sample), java.util.Set.of(PLAYER));
        ApolloState.replaceTeammatesAuthoritative(List.of(), java.util.Set.of(PLAYER));
        assertTrue(ApolloState.lastKnownTeammates().isEmpty());

        ApolloState.replaceTeammatesAuthoritative(List.of(), java.util.Set.of());
        assertEquals(sample, ApolloState.lastKnownTeammates().iterator().next().teammate());
    }

    @Test
    void trustedDeathConvertsLatestPositionToADeathMarker() {
        ApolloModels.Teammate sample = sample("minecraft:overworld", 12.0, 1_000L);
        ApolloState.observeProfile(PLAYER, "Ann");
        ApolloState.replaceTeammatesAuthoritative(List.of(sample));

        assertTrue(ApolloState.markDeath(PLAYER, 5_000L));
        ApolloModels.DeathLocation death = ApolloState.deathLocations().iterator().next();
        assertEquals(sample, death.teammate());
        assertEquals("Ann", death.profileName());
        assertEquals(5_000L, death.diedAt());
        assertTrue(ApolloState.lastKnownTeammates().isEmpty());
    }

    @Test
    void corpseAnnouncementExpiresEveryWorldForOnlyTheNamedPlayer() {
        UUID similar = UUID.randomUUID();
        ApolloState.observeProfile(PLAYER, "Ann");
        ApolloState.observeProfile(similar, "JoAnn");
        ApolloState.replaceTeammatesAuthoritative(List.of(
            sample("minecraft:overworld", 10.0, 1_000L),
            new ApolloModels.Teammate(similar, 20, 64, 0, "minecraft:overworld", "JoAnn", 0, 1_000L)
        ));
        ApolloState.replaceTeammatesAuthoritative(List.of());

        assertTrue(ApolloState.expireCorpse("§7✖ Ann's ::: Corpse has died!"));
        assertEquals(1, ApolloState.lastKnownTeammates().size());
        assertEquals(similar, ApolloState.lastKnownTeammates().iterator().next().teammate().playerId());
        assertFalse(ApolloState.expireCorpse("Someone else's Corpse has died!"));
    }

    @Test
    void corpseAnnouncementBeforeDelayedRemovalPreventsAStaleMarker() {
        ApolloState.observeProfile(PLAYER, "Ann");
        ApolloState.replaceTeammatesAuthoritative(List.of(sample("minecraft:overworld", 10.0, 1_000L)));

        assertTrue(ApolloState.expireCorpse("Ann's Corpse has died!"));
        ApolloState.replaceTeammatesAuthoritative(List.of());

        assertTrue(ApolloState.lastKnownTeammates().isEmpty());
        assertTrue(ApolloState.deathLocations().isEmpty());
    }

    @Test
    void clearingTheSessionRemovesActiveAndArchivedEntries() {
        ApolloState.replaceTeammatesAuthoritative(List.of(sample("minecraft:overworld", 1.0, 1_000L)));
        ApolloState.replaceTeammatesAuthoritative(List.of());
        ApolloState.clear();

        assertTrue(ApolloState.teammates().isEmpty());
        assertTrue(ApolloState.lastKnownTeammates().isEmpty());
    }

    private static ApolloModels.Teammate sample(String world, double x, long updatedAt) {
        return new ApolloModels.Teammate(PLAYER, x, 64, 0, world, "Ann", 0, updatedAt);
    }
}
