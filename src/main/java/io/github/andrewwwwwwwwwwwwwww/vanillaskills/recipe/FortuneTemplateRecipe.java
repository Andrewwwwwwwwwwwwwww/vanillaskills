package io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe;

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
 * Crafts the Fortune Upgrade Smithing Template using the netherite upgrade template's recipe
 * shape, but yielding our renamed/marked template:
 *
 *   D T D     D = diamond
 *   D N D     T = netherite upgrade smithing template
 *   D D D     N = netherite ingot
 */
public class FortuneTemplateRecipe extends CustomRecipe {
    public static final FortuneTemplateRecipe INSTANCE = new FortuneTemplateRecipe();
    public static final RecipeSerializer<FortuneTemplateRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(INSTANCE),
            StreamCodec.<RegistryFriendlyByteBuf, FortuneTemplateRecipe>unit(INSTANCE));

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != 3 || input.height() != 3) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack s = input.getItem(i);
            switch (i) {
                case 1 -> { if (!s.is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)) return false; }
                case 4 -> { if (!s.is(Items.NETHERITE_INGOT)) return false; }
                default -> { if (!s.is(Items.DIAMOND)) return false; }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return FortuneTemplate.create();
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.EQUIPMENT;
    }

    @Override
    public RecipeSerializer<FortuneTemplateRecipe> getSerializer() {
        return SERIALIZER;
    }
}
