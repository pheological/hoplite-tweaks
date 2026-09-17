package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.UUID;

/** Composes all Hoplite Tweaks additions to a player's vanilla nametag. */
public final class PlayerNametag {
    private PlayerNametag() {}

    public record Decoration(Component name, Component header) {}

    public static Decoration decorate(Component name, UUID playerId) {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        Component kills = config.killDisplay.nametag() ? KillCounter.counter(playerId) : null;
        Component dripstone = config.dripstoneDisplay.nametag() ? KillCounter.dripstoneBadge(playerId) : null;
        Component ping = PingHeader.counter(playerId);
        boolean killsAbove = config.killPlacement == HopliteTweaksConfig.KillPlacement.ABOVE_NAME;
        boolean dripstoneAbove = config.dripstonePlacement == HopliteTweaksConfig.KillPlacement.ABOVE_NAME;
        return decorate(name, kills, ping, dripstone, killsAbove, dripstoneAbove, config.pingPosition);
    }

    static Decoration decorate(Component name, Component kills, Component ping,
        boolean killsAbove, HopliteTweaksConfig.PingPosition pingPosition) {
        return decorate(name, kills, ping, null, killsAbove, false, pingPosition);
    }

    static Decoration decorate(Component name, Component kills, Component ping, Component dripstone,
        boolean killsAbove, boolean dripstoneAbove, HopliteTweaksConfig.PingPosition pingPosition) {
        boolean pingAbove = pingPosition == HopliteTweaksConfig.PingPosition.ABOVE_NAME;
        Component decorated = name;
        if (ping != null && pingPosition == HopliteTweaksConfig.PingPosition.APPEND_LEFT) {
            decorated = prepend(decorated, ping);
        }
        if (ping != null && pingPosition == HopliteTweaksConfig.PingPosition.APPEND_RIGHT) {
            decorated = append(decorated, ping);
        }
        if (kills != null && !killsAbove) decorated = KillCounter.append(decorated, kills);
        if (dripstone != null && !dripstoneAbove) {
            decorated = KillCounter.appendDripstone(decorated, dripstone);
        }

        Component header = null;
        if (ping != null && pingAbove) header = ping;
        if (kills != null && killsAbove) header = join(header, kills);
        if (dripstone != null && dripstoneAbove) header = join(header, dripstone);
        return new Decoration(decorated, header);
    }

    static Component prepend(Component name, Component prefix) {
        if (name == null || prefix == null) return name;
        return Component.empty().append(prefix.copy())
            .append(Component.literal(" ").setStyle(Style.EMPTY)).append(name.copy());
    }

    static Component append(Component name, Component suffix) {
        if (name == null || suffix == null) return name;
        return Component.empty().append(name.copy())
            .append(Component.literal(" ").setStyle(Style.EMPTY)).append(suffix.copy());
    }

    static Component join(Component left, Component right) {
        if (left == null) return right;
        if (right == null) return left;
        return Component.empty().append(left.copy())
            .append(Component.literal(" ").setStyle(Style.EMPTY)).append(right.copy());
    }
}
