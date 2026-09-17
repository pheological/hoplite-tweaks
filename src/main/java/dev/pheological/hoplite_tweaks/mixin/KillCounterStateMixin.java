package dev.pheological.hoplite_tweaks.mixin;

import dev.pheological.hoplite_tweaks.KillCounterRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import java.util.UUID;

@Mixin(EntityRenderState.class)
public abstract class KillCounterStateMixin implements KillCounterRenderState {
    @Unique private UUID hopliteTweaks$playerId;
    @Unique private Component hopliteTweaks$nametagHeader;
    public UUID hopliteTweaks$playerId() { return hopliteTweaks$playerId; }
    public void hopliteTweaks$playerId(UUID id) { hopliteTweaks$playerId = id; }
    public Component hopliteTweaks$nametagHeader() { return hopliteTweaks$nametagHeader; }
    public void hopliteTweaks$nametagHeader(Component header) { hopliteTweaks$nametagHeader = header; }
}
