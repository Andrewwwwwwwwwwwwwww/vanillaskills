package io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Shapeless crafting-table recipes for the two custom metals:
 *   Rose Gold Ingot: 4 gold ingots + 4 copper ingots -> 8 rose gold ingots
 *   Steel Ingot:     1 iron ingot + 1 coal           -> 1 steel ingot
 */
public final class AlloyRecipes {
    private AlloyRecipes() {}

    private static boolean isPlainCopper(ItemStack s) {
        return s.is(Items.COPPER_INGOT) && !Alloys.isRoseGoldIngot(s) && !Alloys.isSteelIngot(s);
    }

    public static class RoseGold extends CustomRecipe {
        public static final RoseGold INSTANCE = new RoseGold();
        public static final RecipeSerializer<RoseGold> SERIALIZER = new RecipeSerializer<>(
                MapCodec.unit(INSTANCE), StreamCodec.<RegistryFriendlyByteBuf, RoseGold>unit(INSTANCE));

        @Override
        public boolean matches(CraftingInput input, Level level) {
            int gold = 0, copper = 0;
            for (int i = 0; i < input.size(); i++) {
                ItemStack s = input.getItem(i);
                if (s.isEmpty()) continue;
                if (s.is(Items.GOLD_INGOT)) gold++;          // count slots, not item counts
                else if (isPlainCopper(s)) copper++;
                else return false;
            }
            return gold == 4 && copper == 4;
        }

        @Override
        public ItemStack assemble(CraftingInput input) {
            ItemStack out = Alloys.roseGoldIngot();
            out.setCount(4);
            return out;
        }

        @Override
        public CraftingBookCategory category() {
            return CraftingBookCategory.MISC;
        }

        @Override
        public RecipeSerializer<RoseGold> getSerializer() {
            return SERIALIZER;
        }
    }

    public static class Steel extends CustomRecipe {
        public static final Steel INSTANCE = new Steel();
        public static final RecipeSerializer<Steel> SERIALIZER = new RecipeSerializer<>(
                MapCodec.unit(INSTANCE), StreamCodec.<RegistryFriendlyByteBuf, Steel>unit(INSTANCE));

        @Override
        public boolean matches(CraftingInput input, Level level) {
            int iron = 0, coal = 0;
            for (int i = 0; i < input.size(); i++) {
                ItemStack s = input.getItem(i);
                if (s.isEmpty()) continue;
                if (s.is(Items.IRON_INGOT)) iron++;          // count slots, not item counts
                else if (s.is(Items.COAL)) coal++;
                else return false;
            }
            return iron == 1 && coal == 1;
        }

        @Override
        public ItemStack assemble(CraftingInput input) {
            return Alloys.steelIngot();
        }

        @Override
        public CraftingBookCategory category() {
            return CraftingBookCategory.MISC;
        }

        @Override
        public RecipeSerializer<Steel> getSerializer() {
            return SERIALIZER;
        }
    }
}
