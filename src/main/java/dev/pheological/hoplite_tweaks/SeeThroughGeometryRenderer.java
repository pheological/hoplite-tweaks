package dev.pheological.hoplite_tweaks;

//? >=26.3 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererMap;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.List;

// Custom geometry in the regular alpha-blended, distance-sorted see-through pass.
public final class SeeThroughGeometryRenderer extends RenderTypeFeatureRenderer<SeeThroughGeometryRenderer.Submit> {
    public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("hoplite_tweaks:see_through");

    public static void register(FeatureRendererMap renderers) {
        renderers.put(TYPE, new SeeThroughGeometryRenderer());
    }

    @Override
    protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
        for (Submit submit : submits) {
            submit.renderer().render(submit.pose(), getVertexBuilder(submit.renderType()));
        }
    }

    public record Submit(PoseStack.Pose pose, RenderType renderType,
                         SubmitNodeCollector.CustomGeometryRenderer renderer) implements TranslucentSubmit {
        @Override
        public float distanceToCameraSq() {
            return TranslucentSubmit.computeDistanceToCameraSq(pose.pose());
        }

        @Override
        public FeatureRendererType<Submit> featureType() {
            return TYPE;
        }
    }
}
*///?} else {
public final class SeeThroughGeometryRenderer {
    private SeeThroughGeometryRenderer() { }
}
//?}
