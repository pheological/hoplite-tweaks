package dev.pheological.hoplite_tweaks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.pheological.hoplite_tweaks.HopliteTweaks;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HopliteTweaksConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("hoplite-tweaks.json");
    private static HopliteTweaksConfig instance = new HopliteTweaksConfig();

    public boolean enabled = true;
    public boolean teammateMarkers = true;
    public boolean showTeammateName = true;
    public boolean duelTeamGlow = true;
    public boolean cooldownHud = true;
    public boolean showTeammateDistance = true;
    public boolean compactCooldowns = false;
    public boolean partyMessagePing = true;
    public boolean autoPartyChat = false;
    public boolean weeklyCrateReminder = true;
    public boolean autoPet = true;
    public boolean nickDetector = true;
    public boolean antiSlur = true;
    public boolean chatNameHighlights = true;
    public int markerScalePercent = 100;
    public int markerHeightPercent = 35;
    public int markerMaxDistance = 500;
    public int markerMinDistance = 3;
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
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                HopliteTweaksConfig loaded = GSON.fromJson(reader, HopliteTweaksConfig.class);
                if (loaded != null) {
                    instance = loaded;
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
        instance.clamp();
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
        markerMaxDistance = Math.clamp(markerMaxDistance, 0, 2_000);
        markerMinDistance = Math.clamp(markerMinDistance, 0, 32);
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
