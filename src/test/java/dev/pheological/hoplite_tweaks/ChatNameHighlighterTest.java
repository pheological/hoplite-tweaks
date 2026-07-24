package dev.pheological.hoplite_tweaks;

import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

final class ChatNameHighlighterTest {
    @Test
    void parsesValidUniqueUsernames() {
        assertEquals(
            List.of("longer_name", "player"),
            ChatNameHighlighter.parsePlayers("""
                # comment
                Player
                player
                longer_name
                invalid-name
                """)
        );
    }

    @Test
    void highlightsWholeUsernamesOnly() {
        List<String> names = List.of("player");
        Component original = Component.literal("Player: hello playerish");
        Component highlighted = ChatNameHighlighter.highlight(original, names, 0xFF00AA, true);

        assertEquals(original.getString(), highlighted.getString());
        assertFalse(highlighted == original);
        Component noMatch = Component.literal("playerish");
        assertSame(
            noMatch,
            ChatNameHighlighter.highlight(noMatch, names, 0xFF00AA, true)
        );
    }

    @Test
    void packagesDefaultPlayerList() throws Exception {
        try (InputStream stream = ChatNameHighlighter.class.getResourceAsStream(
            "/assets/hoplite_tweaks/highlighted-players.txt"
        )) {
            assertNotNull(stream);
            assertFalse(ChatNameHighlighter.parsePlayers(
                new String(stream.readAllBytes(), StandardCharsets.UTF_8)
            ).isEmpty());
        }
    }
}
