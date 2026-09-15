package dev.pheological.hoplite_tweaks;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class AntiSlurFilterTest {
    @Test
    void matchesBlockedSubstringsAndPreservesExceptions() {
        AntiSlurFilter.RuleSet rules = AntiSlurFilter.parseRules("""
            # maintained remotely
            retard
            dox
            !paradox
            """);

        assertTrue(AntiSlurFilter.matches("That RETARDED message should stop", rules));
        assertTrue(AntiSlurFilter.matches("They are doxxing someone", rules));
        assertFalse(AntiSlurFilter.matches("That is a paradox", rules));
        assertTrue(AntiSlurFilter.matches("A paradox and a separate dox", rules));
    }

    @Test
    void matchesBlockedMultiWordPhrasesAsSubstrings() {
        AntiSlurFilter.RuleSet rules = AntiSlurFilter.parseRules("blocked phrase");

        assertTrue(AntiSlurFilter.matches("A blocked phrases variant should stop", rules));
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
