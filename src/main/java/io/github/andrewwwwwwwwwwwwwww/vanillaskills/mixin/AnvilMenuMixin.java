package io.github.andrewwwwwwwwwwwwwww.vanillaskills.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The anvil clamps every enchantment to its max_level. Since VanillaSkills keeps Fortune's
 * max_level at 3 but mints Fortune IV/V directly, the anvil would knock those back to III.
 *
 * After the vanilla result is computed, this un-clamps: for any enchantment already present
 * on the result, if either input carries a higher level, the result is raised to it. This
 * only lifts enchantments the anvil already chose to apply (never adds new ones), so it
 * correctly: (a) keeps an over-level tool's enchant through repair/rename, and (b) applies a
 * Fortune IV/V book to a tool at full level.
 *
 * Slots are read via the inherited {@code getSlot} (the {@code inputSlots}/{@code resultSlots}
 * fields live in the superclass {@code ItemCombinerMenu} and can't be shadowed from here).
 */
@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Inject(method = "createResult", at = @At("TAIL"))
    private void vanillaskills$preserveOverLevelEnchantments(CallbackInfo ci) {
        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
        ItemStack result = self.getSlot(AnvilMenu.RESULT_SLOT).getItem();
        if (result.isEmpty()) return;

        DataComponentType<ItemEnchantments> resultType = enchantmentsType(result);
        ItemEnchantments resultEnch = result.get(resultType);
        if (resultEnch == null || resultEnch.isEmpty()) return;

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(resultEnch);
        boolean changed = false;
        for (int slot : new int[]{AnvilMenu.INPUT_SLOT, AnvilMenu.ADDITIONAL_SLOT}) {
            ItemStack input = self.getSlot(slot).getItem();
            if (input.isEmpty()) continue;
            ItemEnchantments inputEnch = input.get(enchantmentsType(input));
            if (inputEnch == null || inputEnch.isEmpty()) continue;
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : inputEnch.entrySet()) {
                Holder<Enchantment> key = entry.getKey();
                int inputLevel = entry.getIntValue();
                // Only lift enchantments the result already has (un-clamp, never add new).
                if (mutable.getLevel(key) > 0 && inputLevel > mutable.getLevel(key)) {
                    mutable.set(key, inputLevel);
                    changed = true;
                }
            }
        }
        if (changed) {
            result.set(resultType, mutable.toImmutable());
        }
    }

    private static DataComponentType<ItemEnchantments> enchantmentsType(ItemStack stack) {
        return stack.is(Items.ENCHANTED_BOOK) ? DataComponents.STORED_ENCHANTMENTS : DataComponents.ENCHANTMENTS;
    }
}
