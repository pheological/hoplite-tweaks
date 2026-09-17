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
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import dev.pheological.hoplite_tweaks.SupplyBeams;
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
            .category(trackerCategory(config, defaults))
            .category(teamCategory(config, defaults))
            .category(cooldownCategory(config, defaults))
            .category(supplyCategory(config, defaults))
            .build()
            .generateScreen(parent);
    }

    private static OptionGroup killTrackerGroup(HopliteTweaksConfig config, HopliteTweaksConfig defaults) {
        var placement = Option.<HopliteTweaksConfig.KillPlacement>createBuilder()
            .name(text("Nametag position"))
            .description(description("Place kills beside the name or on a separate line above it."))
            .binding(defaults.killPlacement, () -> config.killPlacement, value -> config.killPlacement = value)
            .controller(option -> EnumControllerBuilder.create(option).enumClass(HopliteTweaksConfig.KillPlacement.class))
            .available(config.killDisplay.nametag()).build();
        return OptionGroup.createBuilder()
                .name(text("Kill Tracker"))
                .description(description("Counts observed Hoplite eliminations while Game Stats is visible. Zero kills are hidden."))
                .option(toggle("Kill Counter", "Display kills observed during this match. Tracking continues while hidden.",
                    defaults.killCounter, () -> config.killCounter, value -> config.killCounter = value))
                .option(toggle("Track only after mining phase", "Ignore eliminations while the scoreboard shows 'Mining Phase in:'.",
                    defaults.trackKillsAfterMiningPhase, () -> config.trackKillsAfterMiningPhase,
                    value -> config.trackKillsAfterMiningPhase = value))
                .option(Option.<HopliteTweaksConfig.KillDisplay>createBuilder()
                    .name(text("Show kills in"))
                    .binding(defaults.killDisplay, () -> config.killDisplay, value -> config.killDisplay = value)
                    .controller(option -> EnumControllerBuilder.create(option).enumClass(HopliteTweaksConfig.KillDisplay.class))
                    .listener((option, value) -> placement.setAvailable(value.nametag())).build())
                .option(placement)
                .option(toggle("Show kills in scoreboard", "Add kills to the left of recognized player rows. Independent of tab and nametag selection.",
                    defaults.killScoreboard, () -> config.killScoreboard, value -> config.killScoreboard = value))
                .option(ButtonOption.createBuilder().name(text("Reset kill counts")).text(text("Reset"))
                    .action(screen -> dev.pheological.hoplite_tweaks.KillCounter.reset()).build())
                .build();
    }

    private static OptionGroup pingHeaderGroup(HopliteTweaksConfig config, HopliteTweaksConfig defaults) {
        return OptionGroup.createBuilder()
                .name(text("Ping Header"))
                .description(description("Shows each player's current latency in their in-world nametag on multiplayer servers."))
                .option(toggle("Show player ping", "Display ping to either side of player names or on the header line above.",
                    defaults.pingHeader, () -> config.pingHeader, value -> config.pingHeader = value))
                .option(Option.<HopliteTweaksConfig.PingPosition>createBuilder()
                    .name(text("Ping position"))
                    .description(description("Place ping to the right or left of the name, or on the header line above."))
                    .binding(defaults.pingPosition, () -> config.pingPosition, value -> config.pingPosition = value)
                    .controller(option -> EnumControllerBuilder.create(option).enumClass(HopliteTweaksConfig.PingPosition.class))
                    .build())
                .option(string("Text before ping", "Literal text placed before the number.",
                    defaults.pingLeftText, () -> config.pingLeftText, value -> config.pingLeftText = value))
                .option(string("Text after ping", "Literal text placed after the number.",
                    defaults.pingRightText, () -> config.pingRightText, value -> config.pingRightText = value))
                .build();
    }

    private static OptionGroup dripstoneKillGroup(HopliteTweaksConfig config, HopliteTweaksConfig defaults) {
        var placement = Option.<HopliteTweaksConfig.KillPlacement>createBuilder()
            .name(text("Nametag position"))
            .description(description("Place the dripstone badge beside the name or on the header line above."))
            .binding(defaults.dripstonePlacement, () -> config.dripstonePlacement,
                value -> config.dripstonePlacement = value)
            .controller(option -> EnumControllerBuilder.create(option)
                .enumClass(HopliteTweaksConfig.KillPlacement.class))
            .available(config.dripstoneDisplay.nametag()).build();
        return OptionGroup.createBuilder()
            .name(text("Trap Tracker"))
            .description(description("Marks players who earned a kill with pointed dripstone this match."))
            .option(toggle("Show dripstone badge", "Display one vanilla pointed-dripstone icon for qualifying attackers.",
                defaults.dripstoneBadge, () -> config.dripstoneBadge, value -> config.dripstoneBadge = value))
            .option(Option.<HopliteTweaksConfig.KillDisplay>createBuilder()
                .name(text("Show badge in"))
                .binding(defaults.dripstoneDisplay, () -> config.dripstoneDisplay,
                    value -> config.dripstoneDisplay = value)
                .controller(option -> EnumControllerBuilder.create(option)
                    .enumClass(HopliteTweaksConfig.KillDisplay.class))
                .listener((option, value) -> placement.setAvailable(value.nametag())).build())
            .option(placement)
            .build();
    }

    private static ConfigCategory trackerCategory(HopliteTweaksConfig config, HopliteTweaksConfig defaults) {
        return ConfigCategory.createBuilder()
            .name(text("Tracker"))
            .tooltip(text("Kill counts, trap badges, and player ping labels."))
            .group(killTrackerGroup(config, defaults))
            .group(dripstoneKillGroup(config, defaults))
            .group(pingHeaderGroup(config, defaults))
            .build();
    }

    private static ConfigCategory supplyCategory(HopliteTweaksConfig config, HopliteTweaksConfig defaults) {
        return ConfigCategory.createBuilder()
            .name(text("Supply Beams"))
            .tooltip(text("Mark announced supply drops. Beams expire after five minutes."))
            .option(toggle("Supply Crate Beams", "Show beams at tracked drops. Hidden beams still expire and clear on arrival.",
                defaults.supplyCrateBeams, () -> config.supplyCrateBeams, value -> config.supplyCrateBeams = value))
            .option(toggle("Show beam distance", "Show the horizontal distance when looking at a supply crate beam.",
                defaults.showSupplyBeamDistance, () -> config.showSupplyBeamDistance,
                value -> config.showSupplyBeamDistance = value))
            .option(slider("Arrival radius (blocks)", "Permanently clear a drop when this close horizontally. Zero disables arrival clearing.",
                defaults.supplyBeamArrivalRadius, () -> config.supplyBeamArrivalRadius,
                value -> config.supplyBeamArrivalRadius = value, 0, 200, 1))
            .option(color("Beam color", "Color of the supply crate beams.", defaults.supplyBeamColor,
                () -> config.supplyBeamColor, value -> config.supplyBeamColor = value))
            .option(Option.<Integer>createBuilder()
                .name(text("Beam width (%)"))
                .description(description("Apparent width on screen. Stays consistent as you move closer or farther away."))
                .binding(defaults.supplyBeamThicknessPercent, () -> config.supplyBeamThicknessPercent,
                    value -> config.supplyBeamThicknessPercent = value)
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(25, 500).step(25)
                    .valueFormatter(value -> text(value + "%")))
                .build())
            .option(slider("Height (blocks)", "Height above the estimated or loaded ground surface.",
                defaults.supplyBeamHeight, () -> config.supplyBeamHeight,
                value -> config.supplyBeamHeight = value, 32, 512, 16))
            .option(slider("Opacity (%)", "How opaque the beams appear.",
                defaults.supplyBeamOpacityPercent, () -> config.supplyBeamOpacityPercent,
                value -> config.supplyBeamOpacityPercent = value, 10, 100, 5))
            .option(ButtonOption.createBuilder()
                .name(text("Toggle keybind"))
                .text(text("Open Controls"))
                .description(description("Assign Toggle Supply Crate Beams under Hoplite Tweaks. Unassigned by default."))
                .action(screen -> {
                    Minecraft client = Minecraft.getInstance();
                    //? >=26.2 {
                    /*client.gui.setScreen(new KeyBindsScreen(screen, client.options));
                    *///?} else {
                    client.setScreen(new KeyBindsScreen(screen, client.options));
                    //?}
                })
                .build())
            .option(ButtonOption.createBuilder()
                .name(text("Clear tracked drops"))
                .text(text("Clear"))
                .action(screen -> SupplyBeams.clearTrackedDrops())
                .build())
            .option(ButtonOption.createBuilder()
                .name(text("Test supply beam"))
                .text(text("Spawn"))
                .description(description("Spawn a beam 100 blocks in the direction you are looking. It disappears after 20 seconds."))
                .action(screen -> SupplyBeams.spawnTestBeam())
                .build())
            .build();
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
                .option(slider("Minimum distance",
                    "Hides the entire teammate marker within this radius. Set to 0 to always show it.",
                    defaults.markerMinDistance, () -> config.markerMinDistance,
                    value -> config.markerMinDistance = value, 0, 1000, 25))
                .build())
            .group(OptionGroup.createBuilder()
                .name(text("Disconnected teammates"))
                .description(description("Retains a teammate's last reported position after they disconnect, until their corpse expires."))
                .option(toggle("Show disconnected locations",
                    "Draws a gray marker when a teammate disappears from both Apollo and the tab list.",
                    defaults.showLastKnownLocations, () -> config.showLastKnownLocations,
                    value -> config.showLastKnownLocations = value))
                .option(color("Disconnected marker color", "Color used for disconnected teammate markers.",
                    defaults.lastKnownMarkerColor, () -> config.lastKnownMarkerColor,
                    value -> config.lastKnownMarkerColor = value))
                .build())
            .group(OptionGroup.createBuilder()
                .name(text("Death locations"))
                .description(description("Marks a teammate's last reported position when a trusted server death message is detected."))
                .option(toggle("Show death locations", "Draws a fading red marker where a teammate died.",
                    defaults.showDeathLocations, () -> config.showDeathLocations,
                    value -> config.showDeathLocations = value))
                .option(slider("Fade duration", "Seconds before a death marker fully fades away.",
                    defaults.deathMarkerDurationSeconds, () -> config.deathMarkerDurationSeconds,
                    value -> config.deathMarkerDurationSeconds = value, 5, 300, 5))
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
                    "Hides distance for teammates Minecraft is already rendering.",
                    defaults.hideDistanceWhenTeammateInRenderDistance,
                    () -> config.hideDistanceWhenTeammateInRenderDistance,
                    value -> config.hideDistanceWhenTeammateInRenderDistance = value))
                .option(toggle("Reveal text only when looking",
                    "Hides teammate names and distances until you look close enough toward that teammate.",
                    defaults.revealMarkerTextOnLook, () -> config.revealMarkerTextOnLook,
                    value -> config.revealMarkerTextOnLook = value))
                .option(slider("Look angle", "Shows marker text when the teammate is within this angle of your crosshair.",
                    defaults.markerTextViewAngle, () -> config.markerTextViewAngle,
                    value -> config.markerTextViewAngle = value, 0, 90, 1))
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
                    "Master switch. Hoplite features remain server-gated; Ping Header works on every multiplayer server.",
                    defaults.enabled, () -> config.enabled, value -> config.enabled = value))
                .build())
            .group(OptionGroup.createBuilder()
                .name(text("Visuals"))
                .option(toggle("No lava fog",
                    "Removes the dense fog while your camera is submerged in lava.",
                    defaults.noLavaFog, () -> config.noLavaFog, value -> config.noLavaFog = value))
                .build())
            .group(OptionGroup.createBuilder()
                .name(text("Duels"))
                .option(toggle("Team Glow", "Adds the glowing outline to teammates in Hoplite duels.",
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
                .option(toggle("Auto damage summary",
                    "Runs /damagesummary once when a Round 2 title appears.",
                    defaults.autoDamageSummary, () -> config.autoDamageSummary,
                    value -> config.autoDamageSummary = value))
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

    private static Option<String> string(
        String name, String description, String defaultValue, java.util.function.Supplier<String> getter,
        java.util.function.Consumer<String> setter
    ) {
        return Option.<String>createBuilder()
            .name(text(name))
            .description(description(description))
            .binding(defaultValue, getter, setter)
            .controller(StringControllerBuilder::create)
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
