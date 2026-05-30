package io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

/**
 * Crafting-table recipe that mints Fortune IV and V by writing the level directly onto a
 * tool that already has Fortune III (-&gt; IV) or IV (-&gt; V). Recipe = the tool + 8 diamonds.
 *
 * Fortune's data-driven max_level stays 3, so the enchanting table, villagers, loot and
 * anvil-combine all remain capped at III. Writing a higher level directly bypasses that cap;
 * the extra drops work because vanilla ore/crop loot tables read the raw enchantment level.
 */
public class FortuneUpgradeRecipe extends CustomRecipe {
    public static final FortuneUpgradeRecipe INSTANCE = new FortuneUpgradeRecipe();
    public static final RecipeSerializer<FortuneUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(INSTANCE),
            StreamCodec.<RegistryFriendlyByteBuf, FortuneUpgradeRecipe>unit(INSTANCE));

    private static final int REQUIRED_DIAMONDS = 8;
    private static final int MIN_INPUT_LEVEL = 3;
    private static final int MAX_LEVEL = 5;

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack tool = null;
        int diamonds = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(Items.DIAMOND)) {
                diamonds += s.getCount();
                continue;
            }
            int fl = fortuneLevel(s);
            if (fl >= MIN_INPUT_LEVEL && fl < MAX_LEVEL && tool == null && s.getCount() == 1) {
                tool = s;
                continue;
            }
            return false; // any other item, a second tool, or a stacked tool -> no match
        }
        return tool != null && diamonds == REQUIRED_DIAMONDS;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack tool = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (!s.isEmpty() && !s.is(Items.DIAMOND) && fortuneLevel(s) >= MIN_INPUT_LEVEL) {
                tool = s;
                break;
            }
        }
        if (tool == null) return ItemStack.EMPTY;

        ItemStack out = tool.copy();
        out.setCount(1);
        ItemEnchantments ench = out.get(DataComponents.ENCHANTMENTS);
        if (ench == null) return ItemStack.EMPTY;

        Holder<Enchantment> fortune = null;
        int lvl = 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> e : ench.entrySet()) {
            if (e.getKey().is(Enchantments.FORTUNE)) {
                fortune = e.getKey();
                lvl = e.getIntValue();
                break;
            }
        }
        if (fortune == null) return ItemStack.EMPTY;

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ench);
        mutable.set(fortune, Math.min(MAX_LEVEL, lvl + 1));
        out.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        return out;
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public RecipeSerializer<FortuneUpgradeRecipe> getSerializer() {
        return SERIALIZER;
    }

    private static int fortuneLevel(ItemStack stack) {
        ItemEnchantments ench = stack.get(DataComponents.ENCHANTMENTS);
        if (ench == null || ench.isEmpty()) return 0;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : ench.entrySet()) {
            if (entry.getKey().is(Enchantments.FORTUNE)) return entry.getIntValue();
        }
        return 0;
    }
}
