package dev.pheological.hoplite_tweaks;

//? >=26.3 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRendererMap;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
*///?}

class SeeThroughGeometryTest {
    //? >=26.3 {
    /*@ParameterizedTest
    @ValueSource(booleans = {false, true})
    void dispatchesThroughSeeThroughPassWithoutOitOrDebugGizmos(boolean improvedTransparency) {
        SubmitNodeStorage storage = new SubmitNodeStorage();
        storage.setUseImprovedTransparency(improvedTransparency);
        FeatureRendererMap renderers = new FeatureRendererMap();
        SeeThroughGeometryRenderer.register(renderers);
        var renderType = RenderTypes.textSeeThrough(Identifier.fromNamespaceAndPath(
            "hoplite_tweaks", "textures/gui/supply_beam.png"));
        assertTrue(renderType.hasBlending());
        PoseStack matrices = new PoseStack();
        matrices.translate(0, 0, -10);
        SeeThroughGeometry.submit(storage, matrices, renderType, (pose, vertices) -> { });
        matrices.translate(0, 0, -10);
        SeeThroughGeometry.submit(storage, matrices, renderType, (pose, vertices) -> { });
        matrices.translate(0, 0, -100);

        var submits = storage.order(0);
        assertTrue(submits.alwaysOnTopGizmos.isEmpty());
        assertTrue(submits.oitTranslucent.isEmpty());
        assertTrue(submits.translucentCustomGeometry.isEmpty());
        assertTrue(submits.solid.isEmpty());
        assertFalse(storage.seeThrough().isEmpty());

        // Exercise the phase drain used when preparing a frame. Geometry must reach
        // the see-through dispatcher, including with the debug-gizmo pass empty.
        List<SeeThroughGeometryRenderer.Submit> dispatched = new ArrayList<>();
        storage.drainPhases(phase -> phase.sortInto((node, sorted) -> {
            assertSame(storage.seeThrough(), phase);
            assertTrue(sorted);
            var submit = assertInstanceOf(SeeThroughGeometryRenderer.Submit.class, node);
            assertInstanceOf(SeeThroughGeometryRenderer.class,
                renderers.getOrThrow(submit.featureType()));
            assertSame(renderType, submit.renderType());
            dispatched.add(submit);
        }));
        assertEquals(2, dispatched.size());
        assertEquals(400.0F, dispatched.get(0).distanceToCameraSq());
        assertEquals(100.0F, dispatched.get(1).distanceToCameraSq());
        assertTrue(storage.seeThrough().isEmpty());
    }
    *///?}
}
