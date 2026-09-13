package dev.pheological.hoplite_tweaks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.pheological.hoplite_tweaks.config.HopliteTweaksConfig;
//? >=26 {
/*import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
*///?} else {
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

final class SupplyBeamRenderer {
    private static final Identifier WHITE = Identifier.fromNamespaceAndPath(
        HopliteTweaks.MOD_ID, "textures/gui/supply_beam.png");

    private SupplyBeamRenderer() { }

    static void initialize() {
        //? >=26 {
        /*LevelRenderEvents.COLLECT_SUBMITS.register(SupplyBeamRenderer::render);
        *///?} else {
        WorldRenderEvents.AFTER_ENTITIES.register(SupplyBeamRenderer::render);
        //?}
    }

    //? >=26 {
    /*private static void render(LevelRenderContext context) {
    *///?} else {
    private static void render(WorldRenderContext context) {
    //?}
        Minecraft client = Minecraft.getInstance();
        HopliteTweaksConfig config = HopliteTweaksConfig.get();
        if (client.player == null || client.level == null || !config.enabled || !config.supplyCrateBeams
            || !SupplyBeams.STATE.isWorld(client.level)
            || !HopliteSession.isActive()) return;
        //? >=26 {
        /*PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;
        *///?} else {
        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;
        //?}
        // Screen-right offsets do not change depth, so proportional widths project identically.
        //? >=26 {
        /*var rotation = context.levelState().cameraRenderState.orientation;
        *///?} else {
        var rotation = context.worldState().cameraRenderState.orientation;
        //?}
        Vector3f rightVector = rotation.transform(new Vector3f(1, 0, 0));
        Vector3f forwardVector = rotation.transform(new Vector3f(0, 0, -1));
        Vec3 right = new Vec3(rightVector.x, rightVector.y, rightVector.z);
        Vec3 forward = new Vec3(forwardVector.x, forwardVector.y, forwardVector.z);
        double height = config.supplyBeamHeight;
        // Keep the entire projected column well inside the far plane and terrain fog.
        double limit = Math.max(16.0D, Math.min(64.0D, client.options.getEffectiveRenderDistance() * 8.0D));
        int color = (config.supplyBeamOpacityPercent * 255 / 100) << 24 | (config.supplyBeamColor & 0xFFFFFF);
        for (var drop : SupplyBeams.STATE.drops()) {
            Vec3 relative = new Vec3(drop.position().x(), drop.groundY(), drop.position().z()).subtract(camera);
            double bottomDepth = relative.dot(forward);
            double topDepth = bottomDepth + height * forward.y;
            double widthRatio = SupplyBeamStyle.widthAtDepth(1, config.supplyBeamThicknessPercent);
            // Keep signed depths until interpolation so a column crossing the camera plane
            // does not flare out when looking sharply up or down.
            double bottomWidth = bottomDepth * widthRatio;
            double topWidth = topDepth * widthRatio;
            double width = Math.max(0, Math.max(bottomWidth, topWidth));
            double scale = SupplyBeamState.projectionScale(relative.x, relative.y, relative.z, height, width, limit);
            matrices.pushPose();
            matrices.translate(relative.x * scale, relative.y * scale, relative.z * scale);
            matrices.scale((float) scale, (float) scale, (float) scale);
            //? >=26 {
            /*context.submitNodeCollector().submitCustomGeometry(
            *///?} else {
            context.commandQueue().submitCustomGeometry(
            //?}
                matrices, RenderTypes.textSeeThrough(WHITE),
                (pose, vertices) -> geometry(pose, vertices, right, bottomWidth, topWidth, (float) height, color));
            matrices.popPose();
        }
    }

    // One ribbon avoids the dark seams and stacked translucent faces of a box beam.
    // Cross-section bands create a pale luminous core, saturated body, and feathered halo.
    private static final float[] BANDS = {-1, -0.72F, -0.4F, -0.16F, 0, 0.16F, 0.4F, 0.72F, 1};
    private static final float[] ALPHA = {0, 0.04F, 0.2F, 0.65F, 1, 0.65F, 0.2F, 0.04F, 0};
    private static final float[] LIGHT = {0, 0, 0, 0.35F, 0.8F, 0.35F, 0, 0, 0};
    private static final float[] ROWS = {0, 0.01F, 0.05F, 0.75F, 0.9F, 1};
    private static final float[] FADE = {0, 0.8F, 1, 1, 0.6F, 0};

    private static void geometry(PoseStack.Pose pose, VertexConsumer out, Vec3 right,
                                 double bottomWidth, double topWidth, float height, int color) {
        for (int row = 0; row < ROWS.length - 1; row++) {
            for (int band = 0; band < BANDS.length - 1; band++) {
                vertex(pose, out, right, bottomWidth, topWidth, height, row, band, color);
                vertex(pose, out, right, bottomWidth, topWidth, height, row, band + 1, color);
                vertex(pose, out, right, bottomWidth, topWidth, height, row + 1, band + 1, color);
                vertex(pose, out, right, bottomWidth, topWidth, height, row + 1, band, color);
            }
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer out, Vec3 right,
                               double bottomWidth, double topWidth, float height, int row, int band, int color) {
        double width = Math.max(0, bottomWidth + (topWidth - bottomWidth) * ROWS[row]);
        double offset = BANDS[band] * width * 0.5D;
        int shaded = SupplyBeamStyle.shade(color, ALPHA[band] * FADE[row], LIGHT[band]);
        out.addVertex(pose, (float) (right.x * offset),
                height * ROWS[row] + (float) (right.y * offset), (float) (right.z * offset))
            .setColor(shaded).setUv(0.5F, 0.5F).setLight(0x00F000F0);
    }
}
