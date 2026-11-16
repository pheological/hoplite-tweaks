package dev.pheological.hoplite_tweaks.apollo;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TeammateMotionTest {
    private static final UUID PLAYER = UUID.randomUUID();

    @Test
    void sparseUpdatesAreSmoothedAcrossTheObservedPacketInterval() {
        TeammateMotion first = new TeammateMotion(sample(0, "world"), null, 0);
        TeammateMotion next = new TeammateMotion(sample(20, "world"), first, 5_000);
        assertEquals(0, next.position(5_000).x, 0.001);
        assertEquals(5, next.position(6_250).x, 0.001);
        assertEquals(10, next.position(7_500).x, 0.001);
        assertEquals(20, next.position(10_000).x, 0.001);
        assertEquals(20, next.position(60_000).x, 0.001);
    }

    @Test
    void stationaryUpdateDoesNotDriftPastTheServerPosition() {
        TeammateMotion first = new TeammateMotion(sample(0, "world"), null, 0);
        TeammateMotion moving = new TeammateMotion(sample(20, "world"), first, 5_000);
        TeammateMotion stopped = new TeammateMotion(sample(20, "world"), moving, 10_000);
        assertEquals(20, stopped.position(10_000).x, 0.001);
        assertEquals(20, stopped.position(10_150).x, 0.001);
        assertEquals(20, stopped.position(60_000).x, 0.001);
    }

    @Test
    void discontinuitiesSnapAndDiscardVelocity() {
        TeammateMotion first = new TeammateMotion(sample(0, "world"), null, 0);
        assertEquals(200, new TeammateMotion(sample(200, "world"), first, 5_000).position(6_000).x);
        assertEquals(20, new TeammateMotion(sample(20, "nether"), first, 5_000).position(6_000).x);
        assertEquals(20, new TeammateMotion(sample(20, "world"), first, 0).position(6_000).x);
        assertEquals(20, new TeammateMotion(sample(20, "world"), first, 20_000).position(21_000).x);
    }

    @Test
    void removalAndSessionResetDiscardMotion() {
        ApolloModels.Teammate member = sample(20, "world");
        try {
            ApolloState.replaceTeammates(List.of(member));
            ApolloState.replaceTeammates(List.of());
            assertEquals(0, ApolloState.teammatePosition(sample(0, "world")).x);
            ApolloState.replaceTeammates(List.of(member));
            ApolloState.clear();
            assertEquals(0, ApolloState.teammatePosition(sample(0, "world")).x);
        } finally {
            ApolloState.clear();
        }
    }

    private static ApolloModels.Teammate sample(double x, String world) {
        return new ApolloModels.Teammate(PLAYER, x, 64, 0, world, "King", 0xFFFFD400, 0);
    }
}
