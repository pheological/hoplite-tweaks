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
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Comparator;
import java.util.Locale;

/**
 * Renders Apollo teammates as camera-facing markers in the world.
 *
 * <p>The renderer intentionally owns its own geometry and only uses Apollo's
 * public wire data. It does not share implementation code with Teammates.</p>
 */
public final class TeammateMarkerRenderer {
    private static final Identifier MARKER_TEXTURE =
        Identifier.fromNamespaceAndPath(HopliteTweaks.MOD_ID, "textures/gui/teammate_marker.png");
    private static final Identifier SOLID_TEXTURE =
        Identifier.fromNamespaceAndPath("minecraft", "textures/block/white_concrete.png");
    private static final int FULL_BRIGHT = 0x00F000F0;
    private static final float MARKER_HALF_SIZE = 0.18F;

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
        ApolloState.teammates().stream()
            .filter(teammate -> !teammate.playerId().equals(client.player.getUUID()))
            .filter(teammate -> teammate.world().isBlank()
                || teammate.world().toLowerCase(Locale.ROOT).endsWith(dimension.toLowerCase(Locale.ROOT)))
            .map(teammate -> marker(client, teammate, tickDelta, cameraPosition))
            .filter(marker -> marker.distance >= config.markerMinDistance)
            .filter(marker -> config.markerMaxDistance == 0 || marker.distance <= config.markerMaxDistance)
            .sorted(Comparator.comparingDouble((Marker marker) -> marker.distance).reversed())
            .forEach(marker -> drawMarker(context, matrices, cameraRotation, marker, config));
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
            ? new Vec3(teammate.x(), teammate.y(), teammate.z())
            : loaded.getPosition(tickDelta);
        return new Marker(teammate, worldPosition, worldPosition.distanceTo(cameraPosition));
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
        HopliteTweaksConfig config
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
        int color = TeammateRole.colorFor(
            marker.teammate,
            config.kingMarkerColor,
            config.partyMarkerColor,
            config.teammateMarkerColor
        );

        matrices.pushPose();
        matrices.translate(relative.x, relative.y + height, relative.z);

        matrices.pushPose();
        matrices.mulPose(cameraRotation);
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

        Component label = markerLabel(marker, config);
        if (label != null) {
            Minecraft client = Minecraft.getInstance();
            int background = config.markerTextBackground
                ? (int) (client.options.getBackgroundOpacity(0.25F) * 255.0F) << 24
                : 0;
            float textScale = 0.025F * (float) scale * config.markerTextScalePercent / 100.0F;
            float textX = -client.font.width(label) / 2.0F;

            matrices.pushPose();
            matrices.translate(0.0D, 0.42D * scale, 0.0D);
            matrices.mulPose(cameraRotation);
            matrices.scale(textScale, -textScale, textScale);
            //? >=26 {
            /*context.submitNodeCollector().submitText(
            *///?} else {
            context.commandQueue().submitText(
            //?}
                matrices,
                textX,
                0.0F,
                label.getVisualOrderText(),
                false,
                Font.DisplayMode.SEE_THROUGH,
                FULL_BRIGHT,
                0xFFFFFFFF,
                background,
                0
            );
            matrices.popPose();
        }
        matrices.popPose();
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
            case INVERTED_TRIANGLE -> solidQuad(vertices, pose, color, -s, s, s, s, 0.0F, -s, 0.0F, -s);
            case TRIANGLE -> solidQuad(vertices, pose, color, -s, -s, 0.0F, s, s, -s, s, -s);
            case DIAMOND -> solidQuad(vertices, pose, color, 0.0F, -s, s, 0.0F, 0.0F, s, -s, 0.0F);
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

    private static Component markerLabel(Marker marker, HopliteTweaksConfig config) {
        String name = abbreviate(marker.teammate.displayName(), 22);
        String distance = String.format(Locale.ROOT, "%.1fm", marker.distance);
        Component nameText = Component.literal(name)
            .withStyle(style -> style.withColor(config.markerNameColor & 0xFFFFFF));
        Component distanceText = Component.literal(distance)
            .withStyle(style -> style.withColor(config.markerDistanceColor & 0xFFFFFF));
        if (config.showTeammateName && config.showTeammateDistance) {
            return nameText.copy().append(Component.literal("  •  ")).append(distanceText);
        }
        if (config.showTeammateName) {
            return nameText;
        }
        if (config.showTeammateDistance) {
            return distanceText;
        }
        return null;
    }

    private static String abbreviate(String value, int max) {
        if (value == null || value.isBlank()) {
            return "Teammate";
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    private record Marker(ApolloModels.Teammate teammate, Vec3 position, double distance) {
    }
}
