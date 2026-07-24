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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ChatNameHighlighterTest {
    @Test
    void parsesValidUniqueUsernames() {
        assertEquals(
            List.of(
                new ChatNameHighlighter.PlayerHighlight("longer_name", 0x00FF55, false),
                new ChatNameHighlighter.PlayerHighlight("player", 0xFFD400, true)
            ),
            ChatNameHighlighter.parsePlayers("""
                # comment
                Player #FFFFFF normal
                player #FFD400 bold
                longer_name 00FF55 plain
                invalid-name #FF0000 bold
                badcolor #nope bold
                """)
        );
    }

    @Test
    void highlightsWholeUsernamesOnly() {
        List<ChatNameHighlighter.PlayerHighlight> names = List.of(
            new ChatNameHighlighter.PlayerHighlight("player", 0xFF00AA, true)
        );
        Component original = Component.literal("Player: hello playerish");
        Component highlighted = ChatNameHighlighter.highlight(original, names);

        assertEquals(original.getString(), highlighted.getString());
        assertFalse(highlighted == original);
        Component highlightedName = highlighted.toFlatList().getFirst();
        assertEquals(0xFF00AA, highlightedName.getStyle().getColor().getValue());
        assertTrue(highlightedName.getStyle().isBold());
        Component noMatch = Component.literal("playerish");
        assertSame(
            noMatch,
            ChatNameHighlighter.highlight(noMatch, names)
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
