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

    // Ordered to match ToolKind: pickaxe, axe, shovel, hoe, sword, spear (spear uses the vanilla spear item).
    private static final Item[] STONE_TOOLS = {STONE_PICKAXE, STONE_AXE, STONE_SHOVEL, STONE_HOE, STONE_SWORD, STONE_SPEAR};
    private static final Item[] GOLD_TOOLS = {GOLDEN_PICKAXE, GOLDEN_AXE, GOLDEN_SHOVEL, GOLDEN_HOE, GOLDEN_SWORD, GOLDEN_SPEAR};
    private static final Item[] IRON_TOOLS = {IRON_PICKAXE, IRON_AXE, IRON_SHOVEL, IRON_HOE, IRON_SWORD, IRON_SPEAR};
    private static final Item[] DIAMOND_TOOLS = {DIAMOND_PICKAXE, DIAMOND_AXE, DIAMOND_SHOVEL, DIAMOND_HOE, DIAMOND_SWORD, DIAMOND_SPEAR};
    private static final Item[] NETHERITE_TOOLS = {NETHERITE_PICKAXE, NETHERITE_AXE, NETHERITE_SHOVEL, NETHERITE_HOE, NETHERITE_SWORD, NETHERITE_SPEAR};

    // Hardwood = stone tier (better than stone, can't mine diamond), crafted from Wood blocks.
    public static final ToolTier HARDWOOD = new ToolTier(
            "hardwood", "Hardwood", 0x9A6B3F, "vs_tool_hardwood",
            STONE_TOOLS, 200, 0.5, 0.1, 0.0, itemSet(ArmorTiers.WOOD_ITEMS), stack -> WOOD_SET.contains(stack.getItem()));

    // Rose Gold = gold tier, between gold and iron in damage (gold sword 4 -> 5), quick strikes.
    public static final ToolTier ROSE_GOLD = new ToolTier(
            "rose_gold", "Rose Gold", 0xE8B7A6, "vs_tool_rose_gold",
            GOLD_TOOLS, 250, 1.0, 0.2, 0.0, itemSet(GOLD_INGOT), Alloys::isRoseGoldIngot);

    // Steel = iron tier, between iron and diamond in damage (iron sword 6 -> 6.5), more durable than iron.
    public static final ToolTier STEEL = new ToolTier(
            "steel", "Steel", 0xB8C0C8, "vs_tool_steel",
            IRON_TOOLS, 800, 0.5, 0.1, 0.0, itemSet(IRON_INGOT), Alloys::isSteelIngot);

    // Crystalline = diamond tier, between diamond and netherite in damage (diamond sword 7 -> 7.5).
    public static final ToolTier CRYSTAL = new ToolTier(
            "crystal", "Crystalline", 0xB389E8, "vs_tool_crystal",
            DIAMOND_TOOLS, 1800, 0.5, 0.1, 0.0, itemSet(DIAMOND), Alloys::isCrystallizedDiamond);

    // Dragon = netherite tier (top), highest durability; strongest strikes. Crafted from Dragon Ingots.
    // The pickaxe carries +18 mining_efficiency so Efficiency V + Haste II + full Prospector instamine
    // deepslate (needs effective mining speed >= 90: (9 + 26 + 12 + 18) * 1.4 = 91).
    public static final ToolTier DRAGON = new ToolTier(
            "dragon", "Dragon", 0xC23BD6, "vs_tool_dragon",
            NETHERITE_TOOLS, 2500, 1.5, 0.1, 18.0, itemSet(NETHERITE_INGOT),
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonIngot::isDragonIngot);

    public static final List<ToolTier> TIERS = List.of(HARDWOOD, ROSE_GOLD, STEEL, CRYSTAL, DRAGON);

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
