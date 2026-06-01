package io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.minecraft.world.item.Items.*;

/**
 * Definitions for the three current armor tiers and the material lookup used by the recipe.
 * Stat numbers are intentionally easy to tune here.
 */
public final class ArmorTiers {
    private ArmorTiers() {}

    /** "Wood" blocks (all-bark cubes + hyphae, stripped and not) — Hardwood's crafting material. */
    public static final Item[] WOOD_ITEMS = {
            OAK_WOOD, SPRUCE_WOOD, BIRCH_WOOD, JUNGLE_WOOD, ACACIA_WOOD, DARK_OAK_WOOD,
            MANGROVE_WOOD, CHERRY_WOOD, PALE_OAK_WOOD, CRIMSON_HYPHAE, WARPED_HYPHAE,
            STRIPPED_OAK_WOOD, STRIPPED_SPRUCE_WOOD, STRIPPED_BIRCH_WOOD, STRIPPED_JUNGLE_WOOD,
            STRIPPED_ACACIA_WOOD, STRIPPED_DARK_OAK_WOOD, STRIPPED_MANGROVE_WOOD,
            STRIPPED_CHERRY_WOOD, STRIPPED_PALE_OAK_WOOD, STRIPPED_CRIMSON_HYPHAE, STRIPPED_WARPED_HYPHAE
    };
    private static final Set<Item> WOOD_SET = Set.of(WOOD_ITEMS);

    // Per-piece arrays are ordered: helmet, chestplate, leggings, boots.

    public static final ArmorTier HARDWOOD = new ArmorTier(
            "hardwood", "Hardwood", 0x9A6B3F, "vs_armor_hardwood",
            new Item[]{LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS},
            new int[]{2, 4, 3, 2}, 0.0, 0.0, 0.02, new int[]{120, 176, 165, 143},
            itemSet(WOOD_ITEMS), stack -> WOOD_SET.contains(stack.getItem()));

    public static final ArmorTier ROSE_GOLD = new ArmorTier(
            "rose_gold", "Rose Gold", 0xE8B7A6, "vs_armor_rose_gold",
            new Item[]{GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS},
            new int[]{2, 5, 4, 2}, 0.0, 0.0, 0.0, new int[]{200, 288, 270, 234},
            itemSet(COPPER_INGOT), Alloys::isRoseGoldIngot);

    public static final ArmorTier STEEL = new ArmorTier(
            "steel", "Steel", 0xB8C0C8, "vs_armor_steel",
            new Item[]{IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS},
            new int[]{3, 7, 5, 3}, 2.0, 0.0, -0.01, new int[]{330, 481, 451, 390},
            itemSet(IRON_INGOT), Alloys::isSteelIngot);

    public static final List<ArmorTier> TIERS = List.of(HARDWOOD, ROSE_GOLD, STEEL);

    /** The tier whose crafting material this stack is, or null. */
    public static ArmorTier tierForMaterial(ItemStack stack) {
        for (ArmorTier tier : TIERS) {
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
