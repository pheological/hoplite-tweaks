package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;

import java.util.Locale;

public final class DuelGlow {
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
        Objective sidebar = client.level.getScoreboard().getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null) {
            return false;
        }
        String title = sidebar.getDisplayName().getString().toLowerCase(Locale.ROOT);
        return title.contains("duel") || title.contains("comp");
    }
}
