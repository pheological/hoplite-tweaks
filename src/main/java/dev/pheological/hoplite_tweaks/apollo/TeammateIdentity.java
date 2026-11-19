package dev.pheological.hoplite_tweaks.apollo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.Minecraft;

/** Matches server teammate identities to visible nick profiles without rewriting server UUIDs. */
public final class TeammateIdentity {
    private static final Pattern FORMATTING = Pattern.compile("(?i)§[0-9a-fk-orx]");
    private static final Pattern USERNAME = Pattern.compile("(?<![A-Za-z0-9_])[A-Za-z0-9_]{1,16}(?![A-Za-z0-9_])");

    private TeammateIdentity() {}

    public static Map<UUID, AbstractClientPlayer> loadedPlayers(
            Collection<ApolloModels.Teammate> teammates, List<? extends AbstractClientPlayer> players) {
        Map<UUID, AbstractClientPlayer> byId = new HashMap<>();
        players.forEach(player -> byId.put(player.getUUID(), player));
        Map<UUID, String> tabNames = new HashMap<>();
        var connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            for (var member : teammates) {
                var info = connection.getPlayerInfo(member.playerId());
                if (info != null && info.getTabListDisplayName() != null) {
                    tabNames.put(member.playerId(), info.getTabListDisplayName().getString());
                }
            }
        }
        Map<UUID, AbstractClientPlayer> result = new HashMap<>();
        resolve(teammates, players.stream().map(player ->
            new Profile(player.getUUID(), player.getGameProfile().name())).toList(), tabNames)
            .forEach((teammate, player) -> result.put(teammate, byId.get(player)));
        return result;
    }

    static Map<UUID, UUID> resolve(Collection<ApolloModels.Teammate> teammates, Collection<Profile> players) {
        return resolve(teammates, players, Map.of());
    }

    static Map<UUID, UUID> resolve(Collection<ApolloModels.Teammate> teammates, Collection<Profile> players,
            Map<UUID, String> tabNames) {
        Map<UUID, UUID> result = new HashMap<>();
        Set<UUID> claimed = new HashSet<>();
        Set<UUID> loaded = new HashSet<>();
        players.forEach(player -> loaded.add(player.id()));
        // Real UUIDs always win, including over another member's nickname fallback.
        for (var teammate : teammates) {
            if (loaded.contains(teammate.playerId())) {
                result.put(teammate.playerId(), teammate.playerId());
                claimed.add(teammate.playerId());
            }
        }
        Map<UUID, UUID> proposed = new HashMap<>();
        Map<UUID, Integer> uses = new HashMap<>();
        for (var teammate : teammates) {
            if (result.containsKey(teammate.playerId())) continue;
            Set<String> names = names(teammate.displayName());
            names.addAll(names(tabNames.get(teammate.playerId())));
            List<Profile> candidates = players.stream()
                .filter(player -> !claimed.contains(player.id()))
                .filter(player -> player.name() != null && !player.name().isBlank()
                    && names.contains(player.name().toLowerCase(Locale.ROOT)))
                .toList();
            if (candidates.size() != 1) continue;
            UUID candidate = candidates.getFirst().id();
            proposed.put(teammate.playerId(), candidate);
            uses.merge(candidate, 1, Integer::sum);
        }
        // Neither two players for one member nor two members for one player is a safe match.
        proposed.forEach((member, player) -> {
            if (uses.get(player) == 1) result.put(member, player);
        });
        return result;
    }

    private static Set<String> names(String displayName) {
        Set<String> result = new HashSet<>();
        if (displayName == null) return result;
        String plain = FORMATTING.matcher(displayName).replaceAll("")
            .replaceAll("\\[[^\\]]*\\]", " ");
        var matcher = USERNAME.matcher(plain);
        var tokens = new java.util.ArrayList<String>();
        while (matcher.find()) tokens.add(matcher.group().toLowerCase(Locale.ROOT));
        if (tokens.size() == 1) result.add(tokens.getFirst());
        else if (tokens.size() == 2 && Set.of("king", "party", "teammate").contains(tokens.getFirst())) {
            result.add(tokens.get(1));
        }
        return result;
    }

    record Profile(UUID id, String name) {}
}
