package dev.pheological.hoplite_tweaks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.pheological.hoplite_tweaks.HopliteTweaks;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HopliteTweaksConfig {
    private static final int CURRENT_CONFIG_VERSION = 3;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("hoplite-tweaks.json");
    private static HopliteTweaksConfig instance = new HopliteTweaksConfig();

    public int configVersion = CURRENT_CONFIG_VERSION;
    public boolean enabled = true;
    public boolean teammateMarkers = true;
    public boolean showTeammateName = true;
    public boolean duelTeamGlow = true;
    public boolean cooldownHud = true;
    public boolean showCooldownsInHotbar = true;
    public boolean showCooldownsAtTop = false;
    public boolean showTeammateDistance = true;
    public boolean hideDistanceWhenTeammateInRenderDistance = true;
    public boolean hideMarkerWhenTeammateInRenderDistance = true;
    public boolean compactCooldowns = false;
    public boolean partyMessagePing = true;
    public boolean autoPartyChat = false;
    public boolean weeklyCrateReminder = true;
    public boolean autoPet = true;
    public boolean autoApplySkins = true;
    public boolean doubleTapSwordDrop = true;
    public boolean doubleTapLegendaryDrop = true;
    public boolean antiSlur = true;
    public boolean messageDelay = false;
    public int markerScalePercent = 100;
    public int markerHeightPercent = 35;
    public int markerMinDistance = 0;
    public int markerTextScalePercent = 100;
    public int kingMarkerColor = 0xFFFFD400;
    public int partyMarkerColor = 0xFF168CFF;
    public int teammateMarkerColor = 0xFF00FF55;
    public int markerNameColor = 0xFFFFFFFF;
    public int markerDistanceColor = 0xFFFFFFFF;
    public MarkerShape markerShape = MarkerShape.INVERTED_TRIANGLE;
    public boolean markerTextBackground = true;
    public int hudXPercent = 100;
    public int hudYPercent = 100;
    public int hudScalePercent = 100;
    public String lastCrateReminderWeek = "";

    public static HopliteTweaksConfig get() {
        return instance;
    }

    public static void load() {
        int loadedVersion = CURRENT_CONFIG_VERSION;
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                HopliteTweaksConfig loaded = GSON.fromJson(json, HopliteTweaksConfig.class);
                if (loaded != null) {
                    instance = loaded;
                    loadedVersion = json.has("configVersion")
                        ? json.get("configVersion").getAsInt()
                        : 0;
                }
            } catch (Exception exception) {
                HopliteTweaks.LOGGER.warn("Could not read {}; defaults will be used", PATH, exception);
            }
        }
        // Carry the original default forward without overwriting custom colors.
        if (instance.teammateMarkerColor == 0xFF54E37A) {
            instance.teammateMarkerColor = 0xFF00FF55;
        }
        if (instance.hudXPercent == 3 && instance.hudYPercent == 32) {
            instance.hudXPercent = 100;
            instance.hudYPercent = 100;
        }
        if (loadedVersion < 1) {
            instance.autoApplySkins = true;
            instance.doubleTapSwordDrop = true;
            instance.doubleTapLegendaryDrop = true;
        }
        if (loadedVersion < 2) {
            instance.hideDistanceWhenTeammateInRenderDistance = true;
            instance.hideMarkerWhenTeammateInRenderDistance = true;
        }
        if (loadedVersion < 3) {
            instance.markerMinDistance = 0;
        }
        boolean migrated = loadedVersion < CURRENT_CONFIG_VERSION;
        instance.configVersion = CURRENT_CONFIG_VERSION;
        instance.clamp();
        if (migrated) {
            save();
        }
    }

    public static void save() {
        instance.clamp();
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(instance, writer);
            }
        } catch (Exception exception) {
            HopliteTweaks.LOGGER.warn("Could not save {}", PATH, exception);
        }
    }

    public static void reset() {
        instance = new HopliteTweaksConfig();
        save();
    }

    private void clamp() {
        hudXPercent = Math.clamp(hudXPercent, 0, 100);
        hudYPercent = Math.clamp(hudYPercent, 0, 100);
        hudScalePercent = Math.clamp(hudScalePercent, 50, 200);
        markerScalePercent = Math.clamp(markerScalePercent, 50, 200);
        markerHeightPercent = Math.clamp(markerHeightPercent, 0, 200);
        markerMinDistance = Math.clamp(markerMinDistance, 0, 2_000);
        markerTextScalePercent = Math.clamp(markerTextScalePercent, 50, 200);
        if (markerShape == null) {
            markerShape = MarkerShape.INVERTED_TRIANGLE;
        }
        if (lastCrateReminderWeek == null) {
            lastCrateReminderWeek = "";
        }
    }

    public enum MarkerShape {
        INVERTED_TRIANGLE("Inverted triangle"),
        TRIANGLE("Triangle"),
        DIAMOND("Diamond"),
        SQUARE("Square"),
        CHEVRON("Chevron");

        private final String label;

        MarkerShape(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
