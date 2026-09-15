package dev.pheological.hoplite_tweaks;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

final class HopliteKeybindings {
    static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath(HopliteTweaks.MOD_ID, "main"));

    private HopliteKeybindings() {
    }
}
