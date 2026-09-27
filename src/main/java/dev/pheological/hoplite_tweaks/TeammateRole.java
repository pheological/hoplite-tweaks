package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.apollo.ApolloModels;

final class TeammateRole {
    private TeammateRole() {
    }

    static int colorFor(
        ApolloModels.Teammate teammate,
        boolean king,
        int kingColor,
        int partyColor,
        int teammateColor
    ) {
        int serverColor = teammate.color();
        int red = serverColor >>> 16 & 0xFF;
        int green = serverColor >>> 8 & 0xFF;
        int blue = serverColor & 0xFF;

        if (king) {
            return kingColor;
        }
        if (teammate.displayName().toLowerCase(java.util.Locale.ROOT).contains("party")
            || blue >= 140 && blue > red * 1.18F && blue > green * 1.08F) {
            return partyColor;
        }
        return teammateColor;
    }
}
