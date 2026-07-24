package dev.pheological.hoplite_tweaks;

import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Executes Hoplite's clickable skin-application prompt when requested.
 */
public final class AutoApplySkins {
    private static final long DUPLICATE_WINDOW_MILLIS = 1_000;
    private static String lastCommand = "";
    private static long lastAppliedAt;

    private AutoApplySkins() {
    }

    public static void initialize() {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                handle(message);
            }
        });
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, receivedAt) ->
            handle(message)
        );
    }

    private static void handle(Component message) {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        if (!config.enabled
            || !config.autoApplySkins
            || !HopliteSession.isActive()
            || !isApplyPrompt(message)) {
            return;
        }

        String command = message.toFlatList().stream()
            .map(part -> part.getStyle().getClickEvent())
            .filter(ClickEvent.RunCommand.class::isInstance)
            .map(ClickEvent.RunCommand.class::cast)
            .map(ClickEvent.RunCommand::command)
            .findFirst()
            .orElse("");
        if (command.isBlank()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (command.equals(lastCommand) && now - lastAppliedAt < DUPLICATE_WINDOW_MILLIS) {
            return;
        }
        lastCommand = command;
        lastAppliedAt = now;

        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.getConnection() != null && HopliteSession.isActive()) {
                client.getConnection().sendCommand(
                    command.startsWith("/") ? command.substring(1) : command
                );
            }
        });
    }

    static boolean isApplyPrompt(Component message) {
        return message != null
            && message.getString().toLowerCase(Locale.ROOT).contains("click here to apply");
    }
}
