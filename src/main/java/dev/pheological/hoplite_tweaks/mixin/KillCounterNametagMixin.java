package dev.pheological.hoplite_tweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.pheological.hoplite_tweaks.KillCounterRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
//? >=26 {
/*import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
*///?} else {
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
//?}

//? >=26 {
/*@Mixin(EntityRenderer.class)
*///?} else {
@Mixin(AvatarRenderer.class)
//?}
public abstract class KillCounterNametagMixin {
    // Wrap the final name submission, after the player's scoreboard offset and
    // other name decorations. Preserve the original visibility and lighting args.
    //? >=26.2 {
    /*@WrapOperation(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
        at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
    *///?} else if >=26 {
    /*@WrapOperation(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
        at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/level/CameraRenderState;)V"))
    *///?} else {
    @WrapOperation(method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
        at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V"))
    //?}
    private void hopliteTweaks$kills(SubmitNodeCollector collector, PoseStack pose, Vec3 attachment,
        int offset, Component name, boolean seeThrough, int light,
        //? <26.2 {
        double distance,
        //?}
        CameraRenderState camera, Operation<Void> original,
        //? >=26 {
        /*EntityRenderState state,
        *///?} else {
        AvatarRenderState state,
        //?}
        PoseStack enclosingPose, SubmitNodeCollector enclosingCollector, CameraRenderState enclosingCamera) {
        Component header = ((KillCounterRenderState) state).hopliteTweaks$nametagHeader();
        original.call(collector, pose, attachment, offset, name, seeThrough, light,
            //? <26.2 {
            distance,
            //?}
            camera);
        if (header != null) {
            pose.pushPose();
            pose.translate(0, 9 * 1.15f * 0.025f, 0);
            collector.submitNameTag(pose, attachment, offset, header, seeThrough, light,
                //? <26.2 {
                distance,
                //?}
                camera);
            pose.popPose();
        }
    }
}
