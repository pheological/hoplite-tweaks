package dev.pheological.hoplite_tweaks;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AntiSlurFilterTest {
    @Test
    void parsesCommentsPhrasesAndExceptions() {
        AntiSlurFilter.RuleSet rules = AntiSlurFilter.parseRules("""
            # maintained remotely
            blockedword
            blocked phrase
            !allowed blockedword
            """);

        assertTrue(AntiSlurFilter.matches("That BLOCKEDWORD should stop", rules));
        assertTrue(AntiSlurFilter.matches("A blocked phrase should stop", rules));
        assertFalse(AntiSlurFilter.matches("allowed blockedword", rules));
        assertFalse(AntiSlurFilter.matches("blockedwordish", rules));
    }

    @Test
    void normalizesCommonCharacterSubstitutions() {
        AntiSlurFilter.RuleSet rules = AntiSlurFilter.parseRules("example");
        assertTrue(AntiSlurFilter.matches("3x4mpl3", rules));
    }

    @Test
    void packagesANonEmptyFallbackList() throws Exception {
        try (InputStream stream = AntiSlurFilter.class.getResourceAsStream(
            "/assets/hoplite_tweaks/blocked-words.txt"
        )) {
            assertNotNull(stream);
            AntiSlurFilter.RuleSet rules = AntiSlurFilter.parseRules(
                new String(stream.readAllBytes(), StandardCharsets.UTF_8)
            );
            assertFalse(rules.blocked().isEmpty());
        }
    }
}
