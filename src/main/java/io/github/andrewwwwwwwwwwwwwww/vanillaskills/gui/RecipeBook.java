package io.github.andrewwwwwwwwwwwwwww.vanillaskills.gui;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Alloys;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonIngot;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonScale;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.DragonUpgradeTemplate;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.FortuneTemplate;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.shield.SteelShield;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** The custom crafting recipes shown in the in-game recipe book (one 3x3 display per page). */
public final class RecipeBook {
    private RecipeBook() {}

    /** One displayable recipe: a 3x3 grid of inputs (length 9; nulls allowed) and a result. */
    public record Display(String title, ItemStack[] grid, ItemStack result) {}

    private static final ItemStack E = ItemStack.EMPTY;

    public static List<Display> all() {
        List<Display> r = new ArrayList<>();

        ItemStack scale = DragonScale.create();
        ItemStack netherite = new ItemStack(Items.NETHERITE_INGOT);
        r.add(new Display("Dragon Ingot", new ItemStack[]{
                scale.copy(), scale.copy(), scale.copy(),
                scale.copy(), netherite.copy(), scale.copy(),
                scale.copy(), scale.copy(), scale.copy()}, DragonIngot.create()));

        ItemStack steel = Alloys.steelIngot();
        r.add(new Display("Steel Shield", new ItemStack[]{
                steel.copy(), new ItemStack(Items.SHIELD), steel.copy(),
                steel.copy(), steel.copy(), steel.copy(),
                E, steel.copy(), E}, SteelShield.create()));

        ItemStack chorus = new ItemStack(Items.CHORUS_FLOWER);
        ItemStack endRod = new ItemStack(Items.END_ROD);
        r.add(new Display("Dragon Upgrade Template (×2)", new ItemStack[]{
                chorus.copy(), DragonUpgradeTemplate.create(), chorus.copy(),
                chorus.copy(), netherite.copy(), chorus.copy(),
                endRod.copy(), new ItemStack(Items.SHULKER_SHELL), endRod.copy()}, count(DragonUpgradeTemplate.create(), 2)));

        ItemStack berry = new ItemStack(Items.GLOW_BERRIES);
        ItemStack sculk = new ItemStack(Items.SCULK);
        r.add(new Display("Fortune Upgrade Template (×2)", new ItemStack[]{
                berry.copy(), FortuneTemplate.create(), berry.copy(),
                sculk.copy(), new ItemStack(Items.DIAMOND_BLOCK), sculk.copy(),
                berry.copy(), new ItemStack(Items.EMERALD_BLOCK), berry.copy()}, count(FortuneTemplate.create(), 2)));

        ItemStack lapis = new ItemStack(Items.LAPIS_BLOCK);
        ItemStack diaBlock = new ItemStack(Items.DIAMOND_BLOCK);
        r.add(new Display("Fortune IV Book", new ItemStack[]{
                lapis.copy(), diaBlock.copy(), lapis.copy(),
                fortuneBook(3), FortuneTemplate.create(), fortuneBook(3),
                lapis.copy(), diaBlock.copy(), lapis.copy()}, fortuneBook(4)));

        r.add(new Display("Fortune V Book", new ItemStack[]{
                lapis.copy(), diaBlock.copy(), lapis.copy(),
                fortuneBook(4), FortuneTemplate.create(), fortuneBook(4),
                lapis.copy(), diaBlock.copy(), lapis.copy()}, fortuneBook(5)));

        ItemStack gold = new ItemStack(Items.GOLD_INGOT);
        ItemStack copper = new ItemStack(Items.COPPER_INGOT);
        r.add(new Display("Rose Gold Ingot (×4)", new ItemStack[]{
                gold.copy(), copper.copy(), gold.copy(),
                copper.copy(), E, copper.copy(),
                gold.copy(), copper.copy(), gold.copy()}, count(Alloys.roseGoldIngot(), 4)));

        r.add(new Display("Steel Ingot", new ItemStack[]{
                E, E, E,
                new ItemStack(Items.IRON_INGOT), new ItemStack(Items.COAL), new ItemStack(Items.IRON_INGOT),
                E, E, E}, Alloys.steelIngot()));

        ItemStack diamond = new ItemStack(Items.DIAMOND);
        r.add(new Display("Crystallized Diamond (×4)", new ItemStack[]{
                diamond.copy(), E, diamond.copy(),
                E, new ItemStack(Items.AMETHYST_BLOCK), E,
                diamond.copy(), E, diamond.copy()}, count(Alloys.crystallizedDiamond(), 4)));

        return r;
    }

    private static ItemStack count(ItemStack stack, int n) {
        stack.setCount(n);
        return stack;
    }

    /** A display-only enchanted book labelled "Fortune N" (glint, no real enchantment needed for display). */
    private static ItemStack fortuneBook(int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        String roman = switch (level) { case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; default -> String.valueOf(level); };
        book.set(DataComponents.CUSTOM_NAME, Component.literal("Fortune " + roman)
                .withStyle(ChatFormatting.AQUA).withStyle(s -> s.withItalic(false)));
        book.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return book;
    }
}
