package dev.pheological.hoplite_tweaks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
//? >=26.3 {
/*import net.minecraft.client.renderer.SubmitNodeCollection;

*///?}

/** Submits terrain-visible overlays without sending non-OIT pipelines into OIT. */
final class SeeThroughGeometry {
    private SeeThroughGeometry() { }

    static void submit(SubmitNodeCollector collector, PoseStack matrices, RenderType renderType,
                       SubmitNodeCollector.CustomGeometryRenderer renderer) {
        //? >=26.3 {
        /*// Use the same pass as see-through labels. The gizmo pass is conditional,
        // and ordinary translucent custom geometry enters OIT on 26.3.
        SubmitNodeCollection submits = (SubmitNodeCollection) collector.order(0);
        submits.seeThrough.submit(new SeeThroughGeometryRenderer.Submit(
            matrices.last().copy(), renderType, renderer));
        *///?} else {
        collector.submitCustomGeometry(matrices, renderType, renderer);
        //?}
    }
}
