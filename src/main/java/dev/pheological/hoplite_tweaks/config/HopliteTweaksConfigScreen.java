package dev.pheological.hoplite_tweaks.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Color;

public final class HopliteTweaksConfigScreen {
    private HopliteTweaksConfigScreen() {
    }

    public static Screen create(Screen parent) {
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        HopliteTweaksConfig defaults = new HopliteTweaksConfig();
        return YetAnotherConfigLib.createBuilder()
            .title(text("Hoplite Tweaks"))
            .save(HopliteTweaksConfig::save)
            .category(generalCategory(config, defaults))
            .category(teamCategory(config, defaults))
            .category(cooldownCategory(config, defaults))
            .build()
            .generateScreen(parent);
    }

    private static ConfigCategory teamCategory(HopliteTweaksConfig config, HopliteTweaksConfig defaults) {
        return ConfigCategory.createBuilder()
            .name(text("Team View"))
            .tooltip(text("Teammate markers, labels, role colors, and duel glow."))
            .group(OptionGroup.createBuilder()
                .name(text("World marker"))
                .option(toggle("Show teammate markers", "Draw a marker above each Apollo teammate.",
                    defaults.teammateMarkers, () -> config.teammateMarkers, value -> config.teammateMarkers = value))
                .option(toggle("Hide marker when teammate is in render distance",
                    "Hides the triangle, chevron, or other shape when Minecraft is already rendering the teammate.",
                    defaults.hideMarkerWhenTeammateInRenderDistance,
                    () -> config.hideMarkerWhenTeammateInRenderDistance,
                    value -> config.hideMarkerWhenTeammateInRenderDistance = value))
                .option(Option.<HopliteTweaksConfig.MarkerShape>createBuilder()
                    .name(text("Marker shape"))
                    .description(description("Choose the clean marker silhouette shown above teammates."))
                    .binding(defaults.markerShape, () -> config.markerShape, value -> config.markerShape = value)
                    .controller(option -> EnumControllerBuilder.create(option)
                        .enumClass(HopliteTweaksConfig.MarkerShape.class))
                    .build())
                .option(slider("Marker size", "Controls the marker size.", defaults.markerScalePercent,
                    () -> config.markerScalePercent, value -> config.markerScalePercent = value, 50, 200, 5))
                .option(slider("Height offset", "Moves the marker vertically above the player's head.",
                    defaults.markerHeightPercent, () -> config.markerHeightPercent,
                    value -> config.markerHeightPercent = value, 0, 200, 5))
                .option(slider("Maximum distance", "Set to 0 for unlimited range.", defaults.markerMaxDistance,
                    () -> config.markerMaxDistance, value -> config.markerMaxDistance = value, 0, 1000, 25))
                .option(slider("Hide nearby markers", "Hides markers closer than this many blocks.",
                    defaults.markerMinDistance, () -> config.markerMinDistance,
                    value -> config.markerMinDistance = value, 0, 32, 1))
                .build())
            .group(OptionGroup.createBuilder()
                .name(text("Name and distance"))
                .option(toggle("Show teammate name", "Displays the teammate's name above the marker.",
                    defaults.showTeammateName, () -> config.showTeammateName,
                    value -> config.showTeammateName = value))
                .option(toggle("Show distance", "Displays the distance to the teammate.",
                    defaults.showTeammateDistance, () -> config.showTeammateDistance,
                    value -> config.showTeammateDistance = value))
                .option(toggle("Hide distance when teammate is in render distance",
                    "Hides distance for teammates Minecraft is already rendering. Health remains visible.",
                    defaults.hideDistanceWhenTeammateInRenderDistance,
                    () -> config.hideDistanceWhenTeammateInRenderDistance,
                    value -> config.hideDistanceWhenTeammateInRenderDistance = value))
                .option(toggle("Text background", "Adds a translucent background behind marker text.",
                    defaults.markerTextBackground, () -> config.markerTextBackground,
                    value -> config.markerTextBackground = value))
                .option(slider("Text size", "Controls marker name and distance text size.",
                    defaults.markerTextScalePercent, () -> config.markerTextScalePercent,
                    value -> config.markerTextScalePercent = value, 50, 200, 5))
                .option(color("Name color", "Color used for teammate names.", defaults.markerNameColor,
                    () -> config.markerNameColor, value -> config.markerNameColor = value))
                .option(color("Distance color", "Color used for teammate distances.", defaults.markerDistanceColor,
                    () -> config.markerDistanceColor, value -> config.markerDistanceColor = value))
                .build())
            .group(OptionGroup.createBuilder()
                .name(text("Role colors"))
                .description(description("Marker colors are selected automatically from each teammate's role."))
                .option(color("King", "Marker color for the king.", defaults.kingMarkerColor,
                    () -> config.kingMarkerColor, value -> config.kingMarkerColor = value))
                .option(color("Party member", "Marker color for party members.", defaults.partyMarkerColor,
                    () -> config.partyMarkerColor, value -> config.partyMarkerColor = value))
                .option(color("Teammate", "Marker color for regular teammates.", defaults.teammateMarkerColor,
                    () -> config.teammateMarkerColor, value -> config.teammateMarkerColor = value))
                .build())
            .build();
    }

    private static ConfigCategory cooldownCategory(HopliteTweaksConfig config, HopliteTweaksConfig defaults) {
        return ConfigCategory.createBuilder()
            .name(text("Cooldowns"))
            .tooltip(text("Configure the Coolite-style cooldown HUD."))
            .group(OptionGroup.createBuilder()
                .name(text("Cooldown HUD"))
                .option(toggle("Enable cooldown HUD", "Displays active item and ability cooldowns.",
                    defaults.cooldownHud, () -> config.cooldownHud, value -> config.cooldownHud = value))
                .option(toggle("Show in hotbar",
                    "Shows a visual cooldown sweep over matching hotbar items without blocking item use.",
                    defaults.showCooldownsInHotbar, () -> config.showCooldownsInHotbar,
                    value -> config.showCooldownsInHotbar = value))
                .option(toggle("Show top bars",
                    "Shows client-side top bars using matching hotbar item models.",
                    defaults.showCooldownsAtTop, () -> config.showCooldownsAtTop,
                    value -> config.showCooldownsAtTop = value))
                .option(toggle("Compact cards", "Uses smaller cooldown cards.",
                    defaults.compactCooldowns, () -> config.compactCooldowns,
                    value -> config.compactCooldowns = value))
                .option(slider("HUD scale", "Controls the cooldown HUD size.", defaults.hudScalePercent,
                    () -> config.hudScalePercent, value -> config.hudScalePercent = value, 50, 200, 5))
                .option(slider("Horizontal position", "Moves the HUD across the screen.", defaults.hudXPercent,
                    () -> config.hudXPercent, value -> config.hudXPercent = value, 0, 100, 1))
                .option(slider("Vertical position", "Moves the HUD down the screen.", defaults.hudYPercent,
                    () -> config.hudYPercent, value -> config.hudYPercent = value, 0, 100, 1))
                .option(ButtonOption.createBuilder()
                    .name(text("HUD editor"))
                    .text(text("Open drag editor"))
                    .description(description("Drag a live cooldown preview to place it anywhere on screen."))
                    .action(screen -> {
                        HopliteTweaksConfig.save();
                        //? >=26.2 {
                        /*net.minecraft.client.Minecraft.getInstance().gui.setScreen(
                            new CooldownHudEditorScreen(screen)
                        );
                        *///?} else {
                        net.minecraft.client.Minecraft.getInstance().setScreen(
                            new CooldownHudEditorScreen(screen)
                        );
                        //?}
                    })
                    .build())
                .build())
            .build();
    }

    private static ConfigCategory generalCategory(HopliteTweaksConfig config, HopliteTweaksConfig defaults) {
        return ConfigCategory.createBuilder()
            .name(text("General"))
            .tooltip(text("Global Hoplite Tweaks settings."))
            .group(OptionGroup.createBuilder()
                .name(text("Core"))
                .option(toggle("Enable Hoplite Tweaks",
                    "Master switch. Features still only activate when the server address contains “hoplite”.",
                    defaults.enabled, () -> config.enabled, value -> config.enabled = value))
                .option(toggle("Team glow", "Adds the glowing outline to teammates in Hoplite duels.",
                    defaults.duelTeamGlow, () -> config.duelTeamGlow, value -> config.duelTeamGlow = value))
                .build())
            .group(OptionGroup.createBuilder()
                .name(text("Chat and reminders"))
                .option(toggle("Party and mention ping",
                    "Plays a notification sound for blue party messages and messages mentioning your name.",
                    defaults.partyMessagePing, () -> config.partyMessagePing,
                    value -> config.partyMessagePing = value))
                .option(toggle("Auto Party Chat",
                    "Runs /party chat after a light-green joined message.",
                    defaults.autoPartyChat, () -> config.autoPartyChat, value -> config.autoPartyChat = value))
                .option(toggle("Anti-slur",
                    "Stops blocked words and phrases from being sent. The moderation list updates automatically.",
                    defaults.antiSlur, () -> config.antiSlur, value -> config.antiSlur = value))
                .option(toggle("Message delay",
                    "Queues messages and sends them 3 seconds apart. (only applies to nons)",
                    defaults.messageDelay, () -> config.messageDelay, value -> config.messageDelay = value))
                .option(toggle("Weekly crate reminder",
                    "Five seconds after joining Hoplite, reminds you once per Pacific calendar week after 1:00 AM.",
                    defaults.weeklyCrateReminder, () -> config.weeklyCrateReminder,
                    value -> config.weeklyCrateReminder = value))
                .build())
            .group(OptionGroup.createBuilder()
                .name(text("Gameplay"))
                .option(toggle("Auto Pet",
                    "Automatically pets the battle-bus dragon when the drop prompt appears.",
                    defaults.autoPet, () -> config.autoPet, value -> config.autoPet = value))
                .option(toggle("Auto apply skins",
                    "Automatically clicks Hoplite's “click here to apply” skin prompt.",
                    defaults.autoApplySkins, () -> config.autoApplySkins,
                    value -> config.autoApplySkins = value))
                .option(toggle("Double tap to drop sword",
                    "Requires two drop-key presses within 600 ms to drop a sword from the hotbar.",
                    defaults.doubleTapSwordDrop, () -> config.doubleTapSwordDrop,
                    value -> config.doubleTapSwordDrop = value))
                .option(toggle("Double tap to drop legendary",
                    "Requires two drop-key presses within 600 ms to drop a recognized legendary from the hotbar.",
                    defaults.doubleTapLegendaryDrop, () -> config.doubleTapLegendaryDrop,
                    value -> config.doubleTapLegendaryDrop = value))
                .build())
            .build();
    }

    private static Option<Boolean> toggle(
        String name, String description, boolean defaultValue, java.util.function.Supplier<Boolean> getter,
        java.util.function.Consumer<Boolean> setter
    ) {
        return Option.<Boolean>createBuilder()
            .name(text(name))
            .description(description(description))
            .binding(defaultValue, getter, setter)
            .controller(TickBoxControllerBuilder::create)
            .build();
    }

    private static Option<Integer> slider(
        String name, String description, int defaultValue, java.util.function.Supplier<Integer> getter,
        java.util.function.Consumer<Integer> setter, int minimum, int maximum, int step
    ) {
        return Option.<Integer>createBuilder()
            .name(text(name))
            .description(description(description))
            .binding(defaultValue, getter, setter)
            .controller(option -> IntegerSliderControllerBuilder.create(option)
                .range(minimum, maximum)
                .step(step))
            .build();
    }

    private static Option<Color> color(
        String name, String description, int defaultValue, java.util.function.IntSupplier getter,
        java.util.function.IntConsumer setter
    ) {
        return Option.<Color>createBuilder()
            .name(text(name))
            .description(description(description))
            .binding(new Color(defaultValue, true), () -> new Color(getter.getAsInt(), true),
                value -> setter.accept(value.getRGB()))
            .controller(ColorControllerBuilder::create)
            .build();
    }

    private static OptionDescription description(String value) {
        return OptionDescription.of(text(value));
    }

    private static Component text(String value) {
        return Component.literal(value);
    }
}
