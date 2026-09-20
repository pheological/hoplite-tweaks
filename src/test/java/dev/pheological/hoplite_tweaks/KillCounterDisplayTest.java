package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig.KillDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class KillCounterDisplayTest {
    @org.junit.jupiter.api.BeforeAll
    static void initializeTestConfigDirectory(@org.junit.jupiter.api.io.TempDir java.nio.file.Path directory) throws Exception {
        // Unit tests run without a launched Fabric game; supply its config path.
        var loader = net.fabricmc.loader.api.FabricLoader.getInstance();
        var configDir = loader.getClass().getDeclaredField("configDir");
        configDir.setAccessible(true);
        configDir.set(loader, directory);
    }

    @Test void preservesStyledNameAndIsolatesCounterFontAndStyle() {
        var name = Component.literal("[VIP] Player [Ping: 42]").withColor(0xFF55FF)
            .withStyle(style -> style.withBold(true));
        Component output = KillCounter.append(name, KillCounter.label(3));
        assertEquals("[VIP] Player [Ping: 42]", name.getString());
        assertEquals("[VIP] Player [Ping: 42] \uE100 3", output.getString());
        var segments = output.toFlatList();
        assertEquals(0xFF55FF, segments.getFirst().getStyle().getColor().getValue());
        assertTrue(segments.getFirst().getStyle().isBold());
        var count = segments.getLast();
        assertEquals(0xFFFFFF, count.getStyle().getColor().getValue());
        assertFalse(count.getStyle().isBold());
        assertEquals(FontDescription.DEFAULT, count.getStyle().getFont());
        assertNotEquals(FontDescription.DEFAULT, segments.get(segments.size() - 2).getStyle().getFont());
        assertSame(output, KillCounter.append(output, KillCounter.label(3)));
        assertSame(name, KillCounter.append(name, null));
    }

    @Test void displayModesAreIndependent() {
        assertTrue(KillDisplay.BOTH.tab());
        assertTrue(KillDisplay.BOTH.nametag());
        assertTrue(KillDisplay.TAB_LIST.tab());
        assertFalse(KillDisplay.TAB_LIST.nametag());
        assertFalse(KillDisplay.NAMETAG.tab());
        assertTrue(KillDisplay.NAMETAG.nametag());
    }

    @Test void unrelatedPrivateUseIconsDoNotSuppressOurCounter() {
        Component name = Component.literal("\uE000 Player");
        assertEquals("\uE000 Player \uE100 2", KillCounter.append(name, KillCounter.label(2)).getString());
    }

    @Test void preservesExistingLegendWatchSuffixAndItsFont() {
        FontDescription legendFont = new FontDescription.Resource(
            Identifier.fromNamespaceAndPath("legendwatch", "icons"));
        Component legendIcon = Component.literal("\uE00C")
            .withStyle(style -> style.withFont(legendFont));
        Component decoratedName = Component.empty().append("Player ").append(legendIcon);

        Component output = KillCounter.append(decoratedName, KillCounter.label(2));

        assertEquals("Player \uE00C \uE100 2", output.getString());
        assertTrue(output.toFlatList().stream().anyMatch(part ->
            part.getString().equals("\uE00C") && legendFont.equals(part.getStyle().getFont())));
    }

    @Test void dripstoneBadgeUsesAnIndependentFontAndCannotDuplicate() {
        Component badge = KillCounter.dripstoneLabel();
        assertEquals("\uE101", badge.getString());
        assertNotEquals(FontDescription.DEFAULT, badge.getStyle().getFont());
        Component once = KillCounter.appendDripstone(Component.literal("Player"), badge);
        assertEquals("Player \uE101", once.getString());
        assertSame(once, KillCounter.appendDripstone(once, badge));
    }

    @Test void dripstoneFontUsesVanillaFlatItemTexture() throws Exception {
        try (var stream = getClass().getResourceAsStream(
            "/assets/hoplite_tweaks/font/dripstone.json")) {
            assertNotNull(stream);
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains("minecraft:item/pointed_dripstone.png"));
            assertTrue(json.contains("\\uE101"));
        }
    }

    @Test void trackingSurvivesHiddenModesAndSidebarDoesNotMutateRows() throws Exception {
        var field = KillCounter.class.getDeclaredField("STATE");
        field.setAccessible(true);
        var state = (KillCounterState) field.get(null);
        var config = HopliteTweaksConfig.get();
        boolean enabled = config.enabled, counter = config.killCounter, sidebar = config.killScoreboard;
        KillDisplay display = config.killDisplay;
        var victim = UUID.randomUUID();
        var attacker = UUID.randomUUID();
        try {
            config.enabled = true;
            config.killCounter = false;
            config.killScoreboard = true;
            state.update(new Object(), true, List.of("Game Stats"), 0, false);
            state.observe("Victim", victim);
            state.observe("Attacker", attacker);
            assertTrue(state.accept("Victim was dazzled by Attacker", 1));
            assertNull(KillCounter.counter(attacker));
            config.killCounter = true;
            assertNotNull(KillCounter.counter(attacker));
            assertNull(KillCounter.counter(victim));
            var row = Component.literal("[VIP] Attacker").withColor(0x55FFFF);
            config.killDisplay = KillDisplay.NAMETAG;
            assertSame(row, KillCounter.tab(row, attacker));
            var decorated = KillCounter.sidebarRow(row);
            assertEquals("\uE100 1 [VIP] Attacker", decorated.getString());
            assertEquals("[VIP] Attacker", row.getString());
            assertEquals(0x55FFFF, decorated.toFlatList().getLast().getStyle().getColor().getValue());
            assertSame(decorated, KillCounter.sidebarRow(decorated));
            assertEquals(decorated, KillCounter.sidebarRow(row));
            for (String text : List.of("Game Stats", "Kills: 1", "Victim and Attacker")) {
                var untouched = Component.literal(text);
                assertSame(untouched, KillCounter.sidebarRow(untouched));
            }
            config.killScoreboard = false;
            config.killDisplay = KillDisplay.TAB_LIST;
            assertSame(row, KillCounter.sidebarRow(row));
            assertEquals("[VIP] Attacker \uE100 1", KillCounter.tab(row, attacker).getString());
            config.enabled = false;
            assertSame(row, KillCounter.tab(row, attacker));
            config.enabled = true;
            state.update(new Object(), true, null, 2, false);
            assertNull(KillCounter.counter(attacker));
        } finally {
            state.clear();
            config.enabled = enabled;
            config.killCounter = counter;
            config.killScoreboard = sidebar;
            config.killDisplay = display;
        }
    }
}
