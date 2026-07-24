package dev.pheological.hoplite_tweaks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NonRankDetectorTest {
    @Test
    void detectsGraySenderBeforeFirstColon() {
        Component message = Component.empty()
            .append(Component.literal("PlayerName").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(": hello").withStyle(ChatFormatting.WHITE));

        assertTrue(NonRankDetector.isNon(message, "PlayerName"));
    }

    @Test
    void rejectsRankedSenderAndNamesOutsideSenderPrefix() {
        Component ranked = Component.empty()
            .append(Component.literal("[VIP] ").withStyle(ChatFormatting.GOLD))
            .append(Component.literal("PlayerName").withStyle(ChatFormatting.AQUA))
            .append(Component.literal(": hello").withStyle(ChatFormatting.WHITE));
        Component mentioned = Component.empty()
            .append(Component.literal("OtherPlayer").withStyle(ChatFormatting.GRAY))
            .append(Component.literal(": hi PlayerName").withStyle(ChatFormatting.WHITE));

        assertFalse(NonRankDetector.isNon(ranked, "PlayerName"));
        assertFalse(NonRankDetector.isNon(mentioned, "PlayerName"));
    }
}
