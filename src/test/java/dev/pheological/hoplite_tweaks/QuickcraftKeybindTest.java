package dev.pheological.hoplite_tweaks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuickcraftKeybindTest {
    @Test
    void extractsCommandFromFormattedPromptWithInheritedClickAction() {
        Component prompt = Component.literal("YOU HAVE ALL THE ITEMS ")
            .append(Component.literal("TO CRAFT").withStyle(ChatFormatting.BOLD))
            .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("/quickcraft sword")));

        assertEquals("/quickcraft sword", QuickcraftKeybind.commandFromPrompt(prompt));
    }

    @Test
    void findsClickableSiblingLikeAutoApplySkins() {
        Component prompt = Component.literal("You have all the items to craft a sword! ")
            .append(Component.literal("[Craft]").withStyle(style ->
                style.withClickEvent(new ClickEvent.RunCommand("/quickcraft sword"))));

        assertEquals("/quickcraft sword", QuickcraftKeybind.commandFromPrompt(prompt));
    }

    @Test
    void ignoresUnrelatedMessagesAndPromptsWithoutRunnableCommands() {
        assertEquals("", QuickcraftKeybind.commandFromPrompt(null));
        assertEquals("", QuickcraftKeybind.commandFromPrompt(Component.literal("You need more items")
            .withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("/quickcraft sword")))));
        assertEquals("", QuickcraftKeybind.commandFromPrompt(
            Component.literal("You have all the items to craft a sword!")));
        assertEquals("", QuickcraftKeybind.commandFromPrompt(
            Component.literal("You have all the items to craft a sword!")
                .withStyle(style -> style.withClickEvent(new ClickEvent.SuggestCommand("/quickcraft sword")))));
    }
}
