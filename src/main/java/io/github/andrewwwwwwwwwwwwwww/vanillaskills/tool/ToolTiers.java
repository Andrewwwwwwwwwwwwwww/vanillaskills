package io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Alloys;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorTiers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.minecraft.world.item.Items.*;

/**
 * The three tool tiers, sharing materials and repair rules with the armor tiers. Hardwood is
 * wooden-based (light), Rose Gold and Steel are iron-based (so they harvest the iron tier).
 */
public final class ToolTiers {
    private ToolTiers() {}

    private static final Set<Item> WOOD_SET = Set.of(ArmorTiers.WOOD_ITEMS);

    // Ordered to match ToolKind: pickaxe, axe, shovel, hoe, sword.
    private static final Item[] WOOD_TOOLS = {WOODEN_PICKAXE, WOODEN_AXE, WOODEN_SHOVEL, WOODEN_HOE, WOODEN_SWORD};
    private static final Item[] IRON_TOOLS = {IRON_PICKAXE, IRON_AXE, IRON_SHOVEL, IRON_HOE, IRON_SWORD};

    public static final ToolTier HARDWOOD = new ToolTier(
            "hardwood", "Hardwood", 0x9A6B3F, "vs_tool_hardwood",
            WOOD_TOOLS, 250, itemSet(ArmorTiers.WOOD_ITEMS), stack -> WOOD_SET.contains(stack.getItem()));

    public static final ToolTier ROSE_GOLD = new ToolTier(
            "rose_gold", "Rose Gold", 0xE8B7A6, "vs_tool_rose_gold",
            IRON_TOOLS, 550, itemSet(COPPER_INGOT), Alloys::isRoseGoldIngot);

    public static final ToolTier STEEL = new ToolTier(
            "steel", "Steel", 0xB8C0C8, "vs_tool_steel",
            IRON_TOOLS, 850, itemSet(COPPER_INGOT), Alloys::isSteelIngot);

    public static final List<ToolTier> TIERS = List.of(HARDWOOD, ROSE_GOLD, STEEL);

    public static ToolTier tierForMaterial(ItemStack stack) {
        for (ToolTier tier : TIERS) {
            if (tier.material.test(stack)) return tier;
        }
        return null;
    }

    private static HolderSet<Item> itemSet(Item... items) {
        List<Holder<Item>> holders = new ArrayList<>();
        for (Item item : items) holders.add(item.builtInRegistryHolder());
        return HolderSet.direct(holders);
    }
}
