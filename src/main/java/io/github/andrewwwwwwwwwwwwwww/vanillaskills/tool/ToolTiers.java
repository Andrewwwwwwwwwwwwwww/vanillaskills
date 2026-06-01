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
    private static final Item[] STONE_TOOLS = {STONE_PICKAXE, STONE_AXE, STONE_SHOVEL, STONE_HOE, STONE_SWORD};
    private static final Item[] GOLD_TOOLS = {GOLDEN_PICKAXE, GOLDEN_AXE, GOLDEN_SHOVEL, GOLDEN_HOE, GOLDEN_SWORD};
    private static final Item[] IRON_TOOLS = {IRON_PICKAXE, IRON_AXE, IRON_SHOVEL, IRON_HOE, IRON_SWORD};

    // Hardwood = stone tier (better than stone, can't mine diamond), crafted from Wood blocks.
    public static final ToolTier HARDWOOD = new ToolTier(
            "hardwood", "Hardwood", 0x9A6B3F, "vs_tool_hardwood",
            STONE_TOOLS, 200, itemSet(ArmorTiers.WOOD_ITEMS), stack -> WOOD_SET.contains(stack.getItem()));

    // Rose Gold = gold tier (gold speed, can't mine diamond) but far more durable.
    public static final ToolTier ROSE_GOLD = new ToolTier(
            "rose_gold", "Rose Gold", 0xE8B7A6, "vs_tool_rose_gold",
            GOLD_TOOLS, 250, itemSet(COPPER_INGOT), Alloys::isRoseGoldIngot);

    // Steel = iron tier (mines diamond), more durable than iron but below diamond.
    public static final ToolTier STEEL = new ToolTier(
            "steel", "Steel", 0xB8C0C8, "vs_tool_steel",
            IRON_TOOLS, 800, itemSet(IRON_INGOT), Alloys::isSteelIngot);

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
