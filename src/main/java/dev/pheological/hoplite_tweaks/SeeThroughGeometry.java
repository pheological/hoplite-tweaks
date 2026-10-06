package dev.pheological.hoplite_tweaks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
//? >=26.3 {
/*import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
*///?}

/** Submits terrain-visible overlays without sending non-OIT pipelines into OIT. */
final class SeeThroughGeometry {
    private SeeThroughGeometry() { }

    static void submit(SubmitNodeCollector collector, PoseStack matrices, RenderType renderType,
                       SubmitNodeCollector.CustomGeometryRenderer renderer) {
        //? >=26.3 {
        /*// Even afterTerrain aliases oitTranslucent with Improved Transparency enabled.
        // The always-on-top phase stays separate and uses ordinary alpha blending.
        SubmitNodeCollection submits = (SubmitNodeCollection) collector.order(0);
        submits.alwaysOnTopGizmos.submit(new CustomFeatureRenderer.Submit(
            matrices.last().copy(), renderType, renderer));
        *///?} else {
        collector.submitCustomGeometry(matrices, renderType, renderer);
        //?}
    }
}
