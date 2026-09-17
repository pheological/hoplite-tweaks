package dev.pheological.hoplite_tweaks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.pheological.hoplite_tweaks.apollo.ApolloModels;
import dev.pheological.hoplite_tweaks.apollo.ApolloState;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
//? >=26 {
/*import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
*///?} else {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import org.joml.Quaternionf;

import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Renders packet-provided teammates as camera-facing markers in the world.
 *
 * <p>The renderer intentionally owns its own geometry and only uses Hoplite's
 * public wire data. It does not share implementation code with Teammates.</p>
 */
public final class TeammateMarkerRenderer {
    // Temporarily disabled pending admin review. Set to true to restore the existing
    // tab-list health parsing and marker widget without removing or rewriting it.
    private static final boolean HEALTH_DISPLAY_APPROVED = false;
    private static final Identifier MARKER_TEXTURE =
        Identifier.fromNamespaceAndPath(HopliteTweaks.MOD_ID, "textures/gui/teammate_marker.png");
    private static final Identifier SOLID_TEXTURE =
        Identifier.fromNamespaceAndPath("minecraft", "textures/block/white_concrete.png");
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final int HEALTH_COLOR = 0xFFFF6B7A;
    private static final int DEATH_COLOR = 0xFFFF3333;
    private static final float MARKER_HALF_SIZE = 0.18F;
    private static final float MARKER_HALF_HEIGHT = 0.22F;
    private static final float DIAMOND_HALF_HEIGHT = 0.30F;
    private static final float MARKER_VERTICAL_OFFSET = 0.74F;
    private static final Pattern HEART_HEALTH = Pattern.compile(
        "(\\d+(?:\\.\\d+)?)\\s*(?:❤|♥|hp\\b)",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");

    private TeammateMarkerRenderer() {
    }

    public static void initialize() {
        //? >=26 {
        /*LevelRenderEvents.COLLECT_SUBMITS.register(TeammateMarkerRenderer::render);
        *///?} else {
        WorldRenderEvents.AFTER_ENTITIES.register(TeammateMarkerRenderer::render);
        //?}
    }

    //? >=26 {
    /*private static void render(LevelRenderContext context) {
    *///?} else {
    private static void render(WorldRenderContext context) {
    //?}
        Minecraft client = Minecraft.getInstance();
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        if (!HopliteSession.isActive()
            || !config.enabled
            || !config.teammateMarkers
            || client.player == null
            || client.level == null) {
            return;
        }

        //? >=26 {
        /*PoseStack matrices = context.poseStack();
        Vec3 cameraPosition = context.levelState().cameraRenderState.pos;
        Quaternionf cameraRotation = context.levelState().cameraRenderState.orientation;
        *///?} else {
        PoseStack matrices = context.matrices();
        Vec3 cameraPosition = context.worldState().cameraRenderState.pos;
        Quaternionf cameraRotation = context.worldState().cameraRenderState.orientation;
        //?}
        float tickDelta = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        String dimension = client.level.dimension().identifier().getPath();
        Stream<Marker> liveMarkers = ApolloState.teammates().stream()
            .filter(teammate -> !teammate.playerId().equals(client.player.getUUID()))
            .filter(teammate -> ApolloState.deathLocations().stream()
                .noneMatch(death -> death.teammate().playerId().equals(teammate.playerId())))
            .filter(teammate -> sameDimension(teammate.world(), dimension))
            .map(teammate -> marker(client, teammate, tickDelta, cameraPosition));
        Stream<Marker> lastKnownMarkers = config.showLastKnownLocations
            ? ApolloState.lastKnownTeammates().stream()
                .filter(entry -> !entry.teammate().playerId().equals(client.player.getUUID()))
                .filter(entry -> sameDimension(entry.teammate().world(), dimension))
                .map(entry -> lastKnownMarker(entry, cameraPosition))
            : Stream.empty();
        long now = System.currentTimeMillis();
        long deathDurationMillis = config.deathMarkerDurationSeconds * 1_000L;
        Stream<Marker> deathMarkers = config.showDeathLocations
            ? ApolloState.deathLocations().stream()
                .filter(entry -> now - entry.diedAt() < deathDurationMillis)
                .filter(entry -> !entry.teammate().playerId().equals(client.player.getUUID()))
                .filter(entry -> sameDimension(entry.teammate().world(), dimension))
                .map(entry -> deathMarker(entry, cameraPosition))
            : Stream.empty();
        Stream.of(liveMarkers, lastKnownMarkers, deathMarkers).flatMap(stream -> stream)
            .filter(marker -> outsideMinimumDistance(marker.distance, config.markerMinDistance))
            .sorted(Comparator.comparingDouble((Marker marker) -> marker.distance).reversed())
            .forEach(marker -> drawMarker(context, matrices, cameraRotation, marker, config,
                client.player.getViewVector(tickDelta)));
    }

    private static Marker marker(
        Minecraft client,
        ApolloModels.Teammate teammate,
        float tickDelta,
        Vec3 cameraPosition
    ) {
        AbstractClientPlayer loaded = client.level.players().stream()
            .filter(player -> player.getUUID().equals(teammate.playerId()))
            .findFirst()
            .orElse(null);
        Vec3 worldPosition = loaded == null
            ? ApolloState.teammatePosition(teammate)
            : loaded.getPosition(tickDelta);
        return new Marker(
            teammate,
            worldPosition,
            worldPosition.distanceTo(cameraPosition),
            loaded != null,
            HEALTH_DISPLAY_APPROVED ? tabHealth(client, teammate.playerId()) : -1.0F,
            false,
            false,
            0L,
            teammate.displayName()
        );
    }

    private static Marker lastKnownMarker(
        ApolloModels.LastKnownTeammate entry,
        Vec3 cameraPosition
    ) {
        ApolloModels.Teammate teammate = entry.teammate();
        Vec3 position = new Vec3(teammate.x(), teammate.y(), teammate.z());
        String profileName = entry.profileName() == null || entry.profileName().isBlank()
            ? teammate.displayName()
            : entry.profileName();
        return new Marker(
            teammate,
            position,
            position.distanceTo(cameraPosition),
            false,
            -1.0F,
            true,
            false,
            0L,
            profileName
        );
    }

    private static Marker deathMarker(ApolloModels.DeathLocation entry, Vec3 cameraPosition) {
        ApolloModels.Teammate teammate = entry.teammate();
        Vec3 position = new Vec3(teammate.x(), teammate.y(), teammate.z());
        String profileName = entry.profileName() == null || entry.profileName().isBlank()
            ? teammate.displayName()
            : entry.profileName();
        return new Marker(
            teammate, position, position.distanceTo(cameraPosition), false, -1.0F,
            false, true, entry.diedAt(), profileName
        );
    }

    //? >=26 {
    /*private static void drawMarker(
        LevelRenderContext context,
    *///?} else {
    private static void drawMarker(
        WorldRenderContext context,
    //?}
        PoseStack matrices,
        Quaternionf cameraRotation,
        Marker marker,
        HopliteTweaksConfig config,
        Vec3 viewDirection
    ) {
        double scale = config.markerScalePercent / 100.0D;
        if (marker.distance > 10.0D) {
            scale *= marker.distance / 10.0D;
        }

        //? >=26 {
        /*Vec3 cameraPosition = context.levelState().cameraRenderState.pos;
        *///?} else {
        Vec3 cameraPosition = context.worldState().cameraRenderState.pos;
        //?}
        Vec3 relative = marker.position.subtract(cameraPosition);
        double height = 2.15D + config.markerHeightPercent / 100.0D;
        int color = marker.death
            ? fadedDeathColor(System.currentTimeMillis() - marker.diedAt,
                config.deathMarkerDurationSeconds * 1_000L)
            : marker.lastKnown ? config.lastKnownMarkerColor : TeammateRole.colorFor(
                marker.teammate,
                config.kingMarkerColor,
                config.partyMarkerColor,
                config.teammateMarkerColor
            );

        matrices.pushPose();
        matrices.translate(relative.x, relative.y + height, relative.z);

        boolean hideShape = marker.inRenderDistance
            && config.hideMarkerWhenTeammateInRenderDistance;
        if (!hideShape) {
            matrices.pushPose();
            matrices.translate(0.0D, MARKER_VERTICAL_OFFSET * scale, 0.0D);
            //? >=26.3 {
            /*matrices.rotate(cameraRotation);
            *///?} else {
            matrices.mulPose(cameraRotation);
            //?}
            matrices.scale((float) scale, (float) scale, (float) scale);
            //? >=26 {
            /*context.submitNodeCollector().submitCustomGeometry(
            *///?} else {
            context.commandQueue().submitCustomGeometry(
            //?}
                matrices,
                RenderTypes.textSeeThrough(config.markerShape == HopliteTweaksConfig.MarkerShape.CHEVRON
                    ? MARKER_TEXTURE : SOLID_TEXTURE),
                (pose, vertices) -> markerGeometry(pose, vertices, color, config.markerShape)
            );
            matrices.popPose();
        }

        boolean revealText = !config.revealMarkerTextOnLook
            || withinViewAngle(viewDirection, relative.add(0.0D, height, 0.0D), config.markerTextViewAngle);
        Component nameLabel = revealText ? markerNameLabel(marker, config) : null;
        Component distanceLabel = revealText ? markerDistanceLabel(marker, config) : null;
        Component healthLabel = markerHealthLabel(marker);
        if (nameLabel != null || distanceLabel != null || healthLabel != null) {
            Minecraft client = Minecraft.getInstance();
            int background = config.markerTextBackground
                ? (int) (client.options.getBackgroundOpacity(0.25F) * 255.0F) << 24
                : 0;
            float textScale = 0.025F * (float) scale * config.markerTextScalePercent / 100.0F;

            matrices.pushPose();
            matrices.translate(0.0D, 0.42D * scale, 0.0D);
            //? >=26.3 {
            /*matrices.rotate(cameraRotation);
            *///?} else {
            matrices.mulPose(cameraRotation);
            //?}
            matrices.scale(textScale, -textScale, textScale);
            if (nameLabel != null) {
                submitCenteredText(context, matrices, nameLabel, 0.0F, background);
            }
            if (distanceLabel != null || healthLabel != null) {
                submitWidgetRow(
                    context,
                    matrices,
                    distanceLabel,
                    healthLabel,
                    nameLabel == null ? 0.0F : 10.0F,
                    background
                );
            }
            matrices.popPose();
        }
        matrices.popPose();
    }

    //? >=26 {
    /*private static void submitCenteredText(
        LevelRenderContext context,
    *///?} else {
    private static void submitCenteredText(
        WorldRenderContext context,
    //?}
        PoseStack matrices,
        Component label,
        float y,
        int background
    ) {
        float x = -Minecraft.getInstance().font.width(label) / 2.0F;
        submitText(context, matrices, label, x, y, background);
    }

    //? >=26 {
    /*private static void submitWidgetRow(
        LevelRenderContext context,
    *///?} else {
    private static void submitWidgetRow(
        WorldRenderContext context,
    //?}
        PoseStack matrices,
        Component left,
        Component right,
        float y,
        int background
    ) {
        Font font = Minecraft.getInstance().font;
        if (left == null) {
            submitCenteredText(context, matrices, right, y, background);
            return;
        }
        if (right == null) {
            submitCenteredText(context, matrices, left, y, background);
            return;
        }

        float gap = 4.0F;
        float leftWidth = font.width(left);
        float totalWidth = leftWidth + gap + font.width(right);
        float startX = -totalWidth / 2.0F;
        submitText(context, matrices, left, startX, y, background);
        submitText(context, matrices, right, startX + leftWidth + gap, y, background);
    }

    //? >=26 {
    /*private static void submitText(
        LevelRenderContext context,
    *///?} else {
    private static void submitText(
        WorldRenderContext context,
    //?}
        PoseStack matrices,
        Component label,
        float x,
        float y,
        int background
    ) {
        //? >=26 {
        /*context.submitNodeCollector().submitText(
        *///?} else {
        context.commandQueue().submitText(
        //?}
            matrices,
            x,
            y,
            label.getVisualOrderText(),
            false,
            Font.DisplayMode.SEE_THROUGH,
            FULL_BRIGHT,
            0xFFFFFFFF,
            background,
            0
        );
    }

    private static void markerGeometry(
        PoseStack.Pose pose,
        VertexConsumer vertices,
        int color,
        HopliteTweaksConfig.MarkerShape shape
    ) {
        float s = MARKER_HALF_SIZE;
        if (shape == HopliteTweaksConfig.MarkerShape.CHEVRON) {
            vertex(vertices, pose, -s, -s, 0.0F, 0.0F, 1.0F, color);
            vertex(vertices, pose, s, -s, 0.0F, 1.0F, 1.0F, color);
            vertex(vertices, pose, s, s, 0.0F, 1.0F, 0.0F, color);
            vertex(vertices, pose, -s, s, 0.0F, 0.0F, 0.0F, color);
            return;
        }

        switch (shape) {
            case INVERTED_TRIANGLE -> solidQuad(
                vertices, pose, color,
                -0.025F, -MARKER_HALF_HEIGHT,
                0.025F, -MARKER_HALF_HEIGHT,
                s, MARKER_HALF_HEIGHT,
                -s, MARKER_HALF_HEIGHT
            );
            case TRIANGLE -> solidQuad(
                vertices, pose, color,
                -s, -MARKER_HALF_HEIGHT,
                s, -MARKER_HALF_HEIGHT,
                0.025F, MARKER_HALF_HEIGHT,
                -0.025F, MARKER_HALF_HEIGHT
            );
            case DIAMOND -> solidQuad(
                vertices, pose, color,
                0.0F, -DIAMOND_HALF_HEIGHT,
                s, 0.0F,
                0.0F, DIAMOND_HALF_HEIGHT,
                -s, 0.0F
            );
            case SQUARE -> solidQuad(vertices, pose, color, -s, -s, s, -s, s, s, -s, s);
            case CHEVRON -> {
            }
        }
    }

    private static void solidQuad(
        VertexConsumer vertices,
        PoseStack.Pose pose,
        int color,
        float x1, float y1,
        float x2, float y2,
        float x3, float y3,
        float x4, float y4
    ) {
        vertex(vertices, pose, x1, y1, 0.0F, 0.5F, 0.5F, color);
        vertex(vertices, pose, x2, y2, 0.0F, 0.5F, 0.5F, color);
        vertex(vertices, pose, x3, y3, 0.0F, 0.5F, 0.5F, color);
        vertex(vertices, pose, x4, y4, 0.0F, 0.5F, 0.5F, color);
    }

    private static void vertex(
        VertexConsumer vertices,
        PoseStack.Pose pose,
        float x,
        float y,
        float z,
        float u,
        float v,
        int color
    ) {
        vertices.addVertex(pose, x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setLight(FULL_BRIGHT);
    }

    private static Component markerNameLabel(Marker marker, HopliteTweaksConfig config) {
        if (!config.showTeammateName) {
            return null;
        }
        String name = abbreviate(marker.displayName, 22);
        var label = Component.literal(name)
            .withStyle(style -> style.withColor(config.markerNameColor & 0xFFFFFF));
        if (marker.death) {
            label.append(Component.literal(" · died")
                .withStyle(style -> style.withColor(DEATH_COLOR & 0xFFFFFF)));
        }
        return label;
    }

    private static Component markerDistanceLabel(Marker marker, HopliteTweaksConfig config) {
        boolean showDistance = config.showTeammateDistance
            && !(marker.inRenderDistance && config.hideDistanceWhenTeammateInRenderDistance);
        if (!showDistance) {
            return null;
        }
        return Component.literal(String.format(Locale.ROOT, "%.1fm", marker.distance))
            .withStyle(style -> style.withColor(config.markerDistanceColor & 0xFFFFFF));
    }

    private static Component markerHealthLabel(Marker marker) {
        if (marker.health < 0.0F) {
            return null;
        }
        return Component.literal(formatHealth(marker.health) + "❤")
            .withStyle(style -> style.withColor(HEALTH_COLOR & 0xFFFFFF));
    }

    private static float tabHealth(Minecraft client, java.util.UUID playerId) {
        if (client.getConnection() == null) {
            return -1.0F;
        }
        PlayerInfo info = client.getConnection().getPlayerInfo(playerId);
        if (info == null) {
            return -1.0F;
        }
        if (client.level != null) {
            Objective tabObjective = client.level.getScoreboard()
                .getDisplayObjective(DisplaySlot.LIST);
            if (tabObjective != null) {
                ReadOnlyScoreInfo score = client.level.getScoreboard().getPlayerScoreInfo(
                    ScoreHolder.fromGameProfile(info.getProfile()),
                    tabObjective
                );
                if (score != null) {
                    return score.value();
                }
            }
        }
        if (info.getTabListDisplayName() == null) {
            return -1.0F;
        }
        return extractTabHealth(
            info.getTabListDisplayName().getString(),
            info.getProfile().name()
        );
    }

    static float extractTabHealth(String displayText, String playerName) {
        if (displayText == null || displayText.isBlank()) {
            return -1.0F;
        }
        String clean = displayText.replaceAll("(?i)§[0-9A-FK-ORX]", "");
        Matcher heart = HEART_HEALTH.matcher(clean);
        if (heart.find()) {
            return parseHealth(heart.group(1));
        }

        String withoutName = playerName == null || playerName.isBlank()
            ? clean
            : Pattern.compile(Pattern.quote(playerName), Pattern.CASE_INSENSITIVE)
                .matcher(clean)
                .replaceFirst("");
        Matcher number = NUMBER.matcher(withoutName);
        return number.find() ? parseHealth(number.group()) : -1.0F;
    }

    static boolean outsideMinimumDistance(double distance, int minimumDistance) {
        return minimumDistance == 0 || distance > minimumDistance;
    }

    static String formatLastSeen(long elapsedMillis) {
        long seconds = Math.max(0L, elapsedMillis) / 1_000L;
        return seconds < 60L
            ? "last seen " + seconds + "s ago"
            : "last seen " + seconds / 60L + "m ago";
    }

    static int fadedDeathColor(long elapsedMillis, long durationMillis) {
        if (durationMillis <= 0L) {
            return DEATH_COLOR & 0x00FFFFFF;
        }
        double remaining = 1.0D - Math.clamp((double) elapsedMillis / durationMillis, 0.0D, 1.0D);
        int alpha = (int) Math.round(255.0D * remaining);
        return alpha << 24 | DEATH_COLOR & 0x00FFFFFF;
    }

    static boolean withinViewAngle(Vec3 viewDirection, Vec3 targetDirection, int angleDegrees) {
        if (viewDirection == null || targetDirection == null
            || viewDirection.lengthSqr() == 0.0D || targetDirection.lengthSqr() == 0.0D) {
            return false;
        }
        double cosine = viewDirection.normalize().dot(targetDirection.normalize());
        return cosine >= Math.cos(Math.toRadians(Math.clamp(angleDegrees, 0, 90)));
    }

    private static float parseHealth(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ignored) {
            return -1.0F;
        }
    }

    private static String formatHealth(float health) {
        if (health == Math.rint(health)) {
            return Integer.toString((int) health);
        }
        return String.format(Locale.ROOT, "%.1f", health);
    }

    private static String abbreviate(String value, int max) {
        if (value == null || value.isBlank()) {
            return "Teammate";
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    private static boolean sameDimension(String world, String dimension) {
        return world.isBlank()
            || world.toLowerCase(Locale.ROOT).endsWith(dimension.toLowerCase(Locale.ROOT));
    }

    private record Marker(
        ApolloModels.Teammate teammate,
        Vec3 position,
        double distance,
        boolean inRenderDistance,
        float health,
        boolean lastKnown,
        boolean death,
        long diedAt,
        String displayName
    ) {
    }
}
