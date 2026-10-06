package dev.pheological.hoplite_tweaks;

//? >=26.3 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
*///?}

class SeeThroughGeometryTest {
    //? >=26.3 {
    /*@ParameterizedTest
    @ValueSource(booleans = {false, true})
    void seeThroughGeometryStaysOutOfOitWithEitherTransparencySetting(boolean improvedTransparency) {
        SubmitNodeStorage storage = new SubmitNodeStorage();
        storage.setUseImprovedTransparency(improvedTransparency);
        var renderType = RenderTypes.textSeeThrough(Identifier.fromNamespaceAndPath(
            "hoplite_tweaks", "textures/gui/supply_beam.png"));
        assertTrue(renderType.hasBlending());

        SeeThroughGeometry.submit(storage, new PoseStack(), renderType, (pose, vertices) -> { });

        var submits = storage.order(0);
        assertFalse(submits.alwaysOnTopGizmos.isEmpty());
        assertTrue(submits.oitTranslucent.isEmpty());
        assertTrue(submits.translucentCustomGeometry.isEmpty());
        assertTrue(submits.solid.isEmpty());
    }
    *///?}
}
