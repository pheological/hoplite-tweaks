package dev.pheological.hoplite_tweaks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoApplySkinsTest {
    @Test
    void detectsApplyPromptRegardlessOfCaseOrBoldFormatting() {
        Component prompt = Component.literal("CLICK HERE TO APPLY")
            .withStyle(ChatFormatting.BOLD);

        assertTrue(AutoApplySkins.isApplyPrompt(prompt));
        assertFalse(AutoApplySkins.isApplyPrompt(Component.literal("Click here to preview")));
    }
}
