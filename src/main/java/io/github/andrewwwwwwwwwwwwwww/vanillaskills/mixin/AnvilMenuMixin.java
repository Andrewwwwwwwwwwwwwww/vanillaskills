package io.github.andrewwwwwwwwwwwwwww.vanillaskills.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps the anvil from downgrading over-level enchantments (e.g. a Fortune IV/V pickaxe
 * minted by the FortuneUpgradeRecipe) when the tool is repaired or renamed. After the
 * vanilla result is computed, any enchantment whose level on the base (left) item is
 * higher than on the result is restored to the base level.
 */
@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    @Shadow
    @Final
    protected Container inputSlots;

    @Shadow
    @Final
    protected ResultContainer resultSlots;

    @Inject(method = "createResult", at = @At("TAIL"))
    private void vanillaskills$preserveOverLevelEnchantments(CallbackInfo ci) {
        ItemStack base = inputSlots.getItem(0);
        ItemStack result = resultSlots.getItem(0);
        if (base.isEmpty() || result.isEmpty()) return;

        ItemEnchantments baseEnch = base.get(DataComponents.ENCHANTMENTS);
        if (baseEnch == null || baseEnch.isEmpty()) return;

        ItemEnchantments resultEnch = result.get(DataComponents.ENCHANTMENTS);
        ItemEnchantments.Mutable mutable =
                new ItemEnchantments.Mutable(resultEnch == null ? ItemEnchantments.EMPTY : resultEnch);

        boolean changed = false;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : baseEnch.entrySet()) {
            int baseLevel = entry.getIntValue();
            if (baseLevel > mutable.getLevel(entry.getKey())) {
                mutable.set(entry.getKey(), baseLevel);
                changed = true;
            }
        }
        if (changed) {
            result.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        }
    }
}
