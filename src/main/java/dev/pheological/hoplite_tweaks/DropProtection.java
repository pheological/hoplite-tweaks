package dev.pheological.hoplite_tweaks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public final class DropProtection {
    private static final List<String> LEGENDARY_KEYWORDS = List.of(
        "emerald blade",
        "aiglos",
        "armadillo detonator",
        "artemis",
        "beehive blaster",
        "crimson chainsword",
        "cloud sword",
        "corrupted crossbow",
        "death note",
        "reinforced elytra",
        "horn of winter",
        "enderbow",
        "chrono sword",
        "dragon katana",
        "dragon scepter",
        "eagle eye bow",
        "elder eye of possession",
        "evoker wand",
        "excalibur",
        "jim",
        "freezing chakram",
        "ghastly whistle",
        "golem hammer",
        "gruntilda",
        "guardian cannon",
        "harpoon launcher",
        "headhunter",
        "hypnosis staff",
        "kim",
        "gerald",
        "lich staff",
        "magma cannon",
        "magma club",
        "midas sword",
        "mjolnir",
        "phantom longbow",
        "poseiden",
        "pufferfish cannon",
        "ravager horn",
        "reaper scythe",
        "ribbit reel",
        "sakura tessen",
        "sceptre of arachne",
        "sculkweaver",
        "shadow blade",
        "shrink ray",
        "sonic crossbow",
        "tim",
        "vampire sabre",
        "villager wand",
        "void staff",
        "war pick",
        "wither sickles"
    );

    private DropProtection() {
    }

    public static boolean isLegendary(ItemStack stack) {
        return stack != null && isLegendaryName(stack.getHoverName().getString());
    }

    static boolean isLegendaryName(String itemName) {
        if (itemName == null) {
            return false;
        }
        String normalized = itemName.toLowerCase(Locale.ROOT);
        return LEGENDARY_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    public static void showPressAgainMessage() {
        HopliteChat.send(
            Component.literal("[Hoplite Tweaks] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Press drop again to drop this item.")
                    .withStyle(ChatFormatting.YELLOW))
        );
    }
}
