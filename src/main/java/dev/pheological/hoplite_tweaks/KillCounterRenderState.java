package dev.pheological.hoplite_tweaks;

import java.util.UUID;

/** Per-entity identity, reset on every extraction because render states are reused. */
public interface KillCounterRenderState {
    UUID hopliteTweaks$playerId();
    void hopliteTweaks$playerId(UUID id);
}
