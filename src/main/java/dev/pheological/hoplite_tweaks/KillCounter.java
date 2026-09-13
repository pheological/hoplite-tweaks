package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.scores.*;
import java.util.*;

public final class KillCounter {
    private static final KillCounterState STATE = new KillCounterState();
    private static final String SWORD = "\uE000";
    private static final FontDescription FONT = new FontDescription.Resource(
        Identifier.fromNamespaceAndPath(HopliteTweaks.MOD_ID, "kills"));

    private KillCounter() {}

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> refresh());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> STATE.clear());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> STATE.clear());
        // Hoplite has delivered server-authored notices through both channels across
        // protocol/client versions. accept() de-duplicates mirrored messages.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> accept(message));
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, receivedAt) ->
            accept(message));
    }

    private static void accept(Component message) {
        refresh();
        STATE.accept(message.getString(), now());
    }

    private static long now() { return System.nanoTime() / 1_000_000; }

    private static void refresh() {
        Minecraft client = Minecraft.getInstance();
        boolean hoplite = HopliteSession.isActive();
        STATE.update(client.level, hoplite, hoplite ? sidebar(client) : null, now(),
            HopliteTweaksConfig.get().trackKillsAfterMiningPhase);
        if (!STATE.inGame() || client.level == null) return;
        if (client.getConnection() != null) {
            client.getConnection().getOnlinePlayers().forEach(info ->
                STATE.observe(info.getProfile().name(), info.getProfile().id()));
        }
        client.level.players().forEach(player -> STATE.observe(player.getGameProfile().name(), player.getUUID()));
    }

    private static List<String> sidebar(Minecraft client) {
        if (client.level == null || client.player == null) return null;
        Scoreboard board = client.level.getScoreboard();
        Objective objective = null;
        PlayerTeam team = board.getPlayersTeam(client.player.getScoreboardName());
        if (team != null) {
            //? >=26.2 {
            /*DisplaySlot slot = team.getColor().map(TeamColor::displaySlot).orElse(null);
            *///?} else {
            DisplaySlot slot = DisplaySlot.teamColorToSlot(team.getColor());
            //?}
            if (slot != null) objective = board.getDisplayObjective(slot);
        }
        if (objective == null) objective = board.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return null;
        List<String> lines = new ArrayList<>();
        lines.add(objective.getDisplayName().getString());
        board.listPlayerScores(objective).stream().filter(entry -> !entry.isHidden())
            .sorted(Comparator.comparingInt(PlayerScoreEntry::value).reversed()
                .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER))
            .limit(15).forEach(entry -> lines.add(PlayerTeam.formatNameForTeam(
                board.getPlayersTeam(entry.owner()), entry.ownerName()).getString()));
        return lines.stream().allMatch(line -> KillCounterState.clean(line).isBlank()) ? null : lines;
    }

    public static void reset() { STATE.resetCounts(); refresh(); }

    public static Component counter(UUID player) {
        var config = HopliteTweaksConfig.get();
        int kills = STATE.count(player);
        if (!config.enabled || !config.killCounter || !STATE.inGame() || kills == 0) return null;
        return label(kills);
    }

    static MutableComponent label(int kills) {
        return Component.empty().setStyle(Style.EMPTY.withColor(0xFFFFFF)
                .withBold(false).withItalic(false).withUnderlined(false)
                .withStrikethrough(false).withObfuscated(false))
            .append(Component.literal(SWORD).withStyle(style -> style.withFont(FONT)))
            .append(Component.literal(" " + kills).withStyle(style -> style.withFont(FontDescription.DEFAULT)));
    }

    static boolean decorated(Component text) {
        return text != null && text.toFlatList().stream().anyMatch(part ->
            part.getString().contains(SWORD) && FONT.equals(part.getStyle().getFont()));
    }

    public static Component append(Component name, Component count) {
        if (name == null || count == null || decorated(name)) return name;
        // A neutral parent keeps the new suffix from inheriting ranks' formatting.
        return Component.empty().append(name.copy()).append(Component.literal(" ").setStyle(Style.EMPTY))
            .append(count.copy());
    }

    public static Component tab(Component name, UUID player) {
        return HopliteTweaksConfig.get().killDisplay.tab() ? append(name, counter(player)) : name;
    }

    public static MutableComponent sidebarRow(MutableComponent row) {
        if (!HopliteTweaksConfig.get().killScoreboard || decorated(row)) return row;
        UUID player = STATE.playerInRow(row.getString());
        Component count = player == null ? null : counter(player);
        return count == null ? row : Component.empty().append(count).append(" ").append(row.copy());
    }
}
