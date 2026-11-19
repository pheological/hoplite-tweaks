package dev.pheological.hoplite_tweaks.apollo;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TeammateIdentityTest {
    @Test void nickedMemberResolvesToVisibleProfileWhileKeepingServerIdentity() {
        UUID server = UUID.randomUUID(), visible = UUID.randomUUID();
        var result = TeammateIdentity.resolve(List.of(member(server, "§b[MVP+] §aDisguised")),
            List.of(profile(visible, "disguised")));
        assertEquals(visible, result.get(server));
        assertFalse(result.containsKey(visible));
    }

    @Test void tabNicknameLinkedByServerUuidCanResolveAnOriginalPacketName() {
        UUID server = UUID.randomUUID(), visible = UUID.randomUUID();
        assertEquals(visible, TeammateIdentity.resolve(List.of(member(server, "OriginalName")),
            List.of(profile(visible, "Alias")), java.util.Map.of(server, "§b[MVP+] Alias")).get(server));
        assertTrue(TeammateIdentity.resolve(List.of(member(server, "OriginalName")),
            List.of(profile(visible, "Alias")), java.util.Map.of(UUID.randomUUID(), "Alias")).isEmpty());
    }

    @Test void exactUuidWinsEvenWhenNamesDifferAndReservesThatPlayer() {
        UUID exact = UUID.randomUUID(), nicked = UUID.randomUUID();
        var result = TeammateIdentity.resolve(List.of(member(nicked, "Alias"), member(exact, "Original")),
            List.of(profile(exact, "Alias")));
        assertEquals(exact, result.get(exact));
        assertFalse(result.containsKey(nicked));
    }

    @Test void duplicateVisibleNamesAndCompetingMembersAreNotGuessed() {
        UUID first = UUID.randomUUID(), second = UUID.randomUUID(), player = UUID.randomUUID();
        assertTrue(TeammateIdentity.resolve(List.of(member(first, "Alias")),
            List.of(profile(player, "Alias"), profile(UUID.randomUUID(), "Alias"))).isEmpty());
        assertTrue(TeammateIdentity.resolve(List.of(member(first, "Alias"), member(second, "Alias")),
            List.of(profile(player, "Alias"))).isEmpty());
    }

    @Test void nameChangesAndUnloadsDoNotLeaveCachedMatches() {
        UUID server = UUID.randomUUID(), player = UUID.randomUUID();
        assertEquals(player, TeammateIdentity.resolve(List.of(member(server, "Alias")),
            List.of(profile(player, "Alias"))).get(server));
        assertTrue(TeammateIdentity.resolve(List.of(member(server, "NewAlias")),
            List.of(profile(player, "Alias"))).isEmpty());
        assertTrue(TeammateIdentity.resolve(List.of(member(server, "Alias")), List.of()).isEmpty());
    }

    @Test void prefixesSubstringsRanksAndMissingNamesCannotSelectAnUnrelatedPlayer() {
        UUID server = UUID.randomUUID(), player = UUID.randomUUID();
        for (String name : List.of("OtherAlias", "[MVP+] Missing", "", "LongerThanSixteenCharactersAlias")) {
            assertTrue(TeammateIdentity.resolve(List.of(member(server, name)),
                List.of(profile(player, "Alias"), profile(UUID.randomUUID(), "MVP"))).isEmpty());
        }
        assertTrue(TeammateIdentity.resolve(List.of(member(server, "King Missing")),
            List.of(profile(player, "King"))).isEmpty());
        assertEquals(player, TeammateIdentity.resolve(List.of(member(server, "King Alias")),
            List.of(profile(player, "Alias"))).get(server));
    }

    @Test void exactUuidDoesNotNeedANameOrPositionAndMissingPositionCanUseNickname() {
        UUID server = UUID.randomUUID(), player = UUID.randomUUID();
        assertEquals(server, TeammateIdentity.resolve(List.of(member(server, "")),
            List.of(profile(server, "Different"))).get(server));
        assertEquals(player, TeammateIdentity.resolve(List.of(member(server, "Alias")),
            List.of(profile(player, "Alias"))).get(server));
    }

    private static ApolloModels.Teammate member(UUID id, String name) {
        return new ApolloModels.Teammate(id, Double.NaN, Double.NaN, Double.NaN, "", name, -1, 0);
    }
    private static TeammateIdentity.Profile profile(UUID id, String name) {
        return new TeammateIdentity.Profile(id, name);
    }
}
