package io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

/**
 * The "Fortune Upgrade Smithing Template" is a vanilla netherite upgrade template carrying a
 * custom name and a hidden marker tag. Using a real vanilla item (rather than registering a
 * new one) keeps the mod fully server-side: vanilla clients can render it without the mod.
 * A recolored texture can be supplied later via a resource pack.
 */
public final class FortuneTemplate {
    private FortuneTemplate() {}

    public static final String MARKER_KEY = "vs_fortune_template";

    private static final CompoundTag MARKER = makeMarker();

    private static CompoundTag makeMarker() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(MARKER_KEY, true);
        return tag;
    }

    /** The display name shown on the template. */
    public static Component displayName() {
        return Component.literal("Fortune Upgrade").withStyle(ChatFormatting.AQUA).withStyle(s -> s.withItalic(false));
    }

    /** A fresh marker tag identifying our template. */
    public static CompoundTag markerTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(MARKER_KEY, true);
        return tag;
    }

    /** Build a fresh Fortune Upgrade template stack. */
    public static ItemStack create() {
        ItemStack stack = new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        stack.set(DataComponents.CUSTOM_NAME, displayName());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, markerTag());
        // Hide the vanilla "netherite upgrade" template description (shows just our name).
        stack.set(DataComponents.TOOLTIP_DISPLAY,
                new net.minecraft.world.item.component.TooltipDisplay(true, new java.util.LinkedHashSet<>()));
        return stack;
    }

    /** True if the stack is our marked Fortune Upgrade template. */
    public static boolean isTemplate(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) return false;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.matchedBy(MARKER);
    }
}
