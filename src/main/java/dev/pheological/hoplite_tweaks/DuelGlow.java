package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.Scoreboard;
//? >=26.2 {
/*import net.minecraft.world.scores.TeamColor;
*///?}

import java.util.List;
import java.util.Comparator;
import java.util.regex.Pattern;

import java.util.Locale;

public final class DuelGlow {
    private static final Pattern LOBBY_ID = Pattern.compile("(?i)(?<![a-z0-9_])d(?:10|[1-9])(?![a-z0-9_])");

    private DuelGlow() {
    }

    public static boolean shouldGlow(Entity entity) {
        Minecraft client = Minecraft.getInstance();
        if (!HopliteSession.isActive()
            || !HopliteTweaksConfig.get().enabled
            || !HopliteTweaksConfig.get().duelTeamGlow
            || client.player == null
            || client.level == null
            || !(entity instanceof Player other)
            || other == client.player
            || !isDuel(client)) {
            return false;
        }

        if (ApolloState.isTeammate(other.getUUID())) {
            return true;
        }
        return client.player.getTeam() != null && client.player.isAlliedTo(other);
    }

    private static boolean isDuel(Minecraft client) {
        Scoreboard board = client.level.getScoreboard();
        Objective sidebar = null;
        PlayerTeam team = board.getPlayersTeam(client.player.getScoreboardName());
        if (team != null) {
            //? >=26.2 {
            /*DisplaySlot slot = team.getColor().map(TeamColor::displaySlot).orElse(null);
            *///?} else {
            DisplaySlot slot = DisplaySlot.teamColorToSlot(team.getColor());
            //?}
            if (slot != null) sidebar = board.getDisplayObjective(slot);
        }
        if (sidebar == null) sidebar = board.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null) return false;
        List<String> lines = board.listPlayerScores(sidebar).stream()
            .filter(entry -> !entry.isHidden())
            .sorted(Comparator.comparingInt(PlayerScoreEntry::value).reversed()
                .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER))
            .limit(15)
            .map(entry -> PlayerTeam.formatNameForTeam(
                board.getPlayersTeam(entry.owner()), entry.ownerName()).getString())
            .toList();
        return isDuelMatch(sidebar.getDisplayName().getString(), lines);
    }

    static boolean isDuelMatch(String title, List<String> lines) {
        String normalized = title.toLowerCase(Locale.ROOT);
        if (!normalized.contains("duel") && !normalized.contains("comp")) return false;
        return lines.stream().noneMatch(line -> LOBBY_ID.matcher(
            line.replaceAll("(?i)§[0-9a-fk-orx]", "")).find());
    }
}
