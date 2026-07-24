package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloModels;

import java.util.Locale;

final class TeammateRole {
    private TeammateRole() {
    }

    static int colorFor(
        ApolloModels.Teammate teammate,
        int kingColor,
        int partyColor,
        int teammateColor
    ) {
        String name = teammate.displayName().toLowerCase(Locale.ROOT);
        int serverColor = teammate.color();
        int red = serverColor >>> 16 & 0xFF;
        int green = serverColor >>> 8 & 0xFF;
        int blue = serverColor & 0xFF;

        if (name.contains("king")
            || name.contains("♛")
            || name.contains("♔")
            || name.contains("crown")
            || red >= 180 && green >= 120 && blue <= 140) {
            return kingColor;
        }
        if (name.contains("party")
            || blue >= 140 && blue > red * 1.18F && blue > green * 1.08F) {
            return partyColor;
        }
        return teammateColor;
    }
}
