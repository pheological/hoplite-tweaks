package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PingHeaderTest {
    @Test void formatsLiteralAffixesAndRejectsUnavailableLatency() {
        assertEquals("(100 ms)", PingHeader.label(100, "(", " ms)").getString());
        assertEquals("[100]", PingHeader.label(100, "[", "]").getString());
        assertEquals("100", PingHeader.label(100, "", "").getString());
        assertEquals("100", PingHeader.label(100, null, null).getString());
        assertNull(PingHeader.label(-1, "(", ")"));
        assertEquals(0x008000, PingHeader.color(0));
        assertEquals(0x55FF55, PingHeader.color(50));
        assertEquals(0xFFFF55, PingHeader.color(100));
        assertEquals(0xFFAA00, PingHeader.color(150));
        assertEquals(0xFF5555, PingHeader.color(200));
        assertEquals(0xAA0000, PingHeader.color(300));
        assertNotEquals(PingHeader.color(51), PingHeader.color(52));
    }

    @Test void composesEveryPingAndKillPlacement() {
        Component name = Component.literal("Player").withColor(0x55FFFF);
        Component ping = PingHeader.label(100, "(", " ms)");
        Component kills = KillCounter.label(2);

        var appended = PlayerNametag.decorate(name, kills, ping, false,
            HopliteTweaksConfig.PingPosition.APPEND_RIGHT);
        assertEquals("Player (100 ms) \uE100 2", appended.name().getString());
        assertNull(appended.header());
        assertEquals(0x55FFFF, appended.name().toFlatList().stream()
            .filter(part -> part.getString().equals("Player")).findFirst().orElseThrow()
            .getStyle().getColor().getValue());

        var left = PlayerNametag.decorate(name, kills, ping, false,
            HopliteTweaksConfig.PingPosition.APPEND_LEFT);
        assertEquals("(100 ms) Player \uE100 2", left.name().getString());

        var bothAbove = PlayerNametag.decorate(name, kills, ping, true,
            HopliteTweaksConfig.PingPosition.ABOVE_NAME);
        assertEquals("Player", bothAbove.name().getString());
        assertEquals("(100 ms) \uE100 2", bothAbove.header().getString());

        var pingAbove = PlayerNametag.decorate(name, kills, ping, false,
            HopliteTweaksConfig.PingPosition.ABOVE_NAME);
        assertEquals("Player \uE100 2", pingAbove.name().getString());
        assertEquals("(100 ms)", pingAbove.header().getString());

        var killsAbove = PlayerNametag.decorate(name, kills, ping, true,
            HopliteTweaksConfig.PingPosition.APPEND_RIGHT);
        assertEquals("Player (100 ms)", killsAbove.name().getString());
        assertEquals("\uE100 2", killsAbove.header().getString());
    }

    @Test void composesDripstoneAfterPingAndKillsBesideOrAboveName() {
        Component name = Component.literal("Player");
        Component ping = PingHeader.label(100, "(", " ms)");
        Component kills = KillCounter.label(2);
        Component dripstone = KillCounter.dripstoneLabel();

        var beside = PlayerNametag.decorate(name, kills, ping, dripstone, false, false,
            HopliteTweaksConfig.PingPosition.APPEND_RIGHT);
        assertEquals("Player (100 ms) \uE100 2 \uE101", beside.name().getString());
        assertNull(beside.header());

        var above = PlayerNametag.decorate(name, kills, ping, dripstone, true, true,
            HopliteTweaksConfig.PingPosition.ABOVE_NAME);
        assertEquals("Player", above.name().getString());
        assertEquals("(100 ms) \uE100 2 \uE101", above.header().getString());

        var split = PlayerNametag.decorate(name, kills, ping, dripstone, true, false,
            HopliteTweaksConfig.PingPosition.APPEND_LEFT);
        assertEquals("(100 ms) Player \uE101", split.name().getString());
        assertEquals("\uE100 2", split.header().getString());
    }
}
