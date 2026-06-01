package io.github.andrewwwwwwwwwwwwwww.vanillaskills.shield;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Markers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Repairable;

import java.util.List;

/**
 * A vanilla shield infused with steel: a marked shield with much greater durability and a "thorns"
 * effect (handled by ShieldThornsMixin) that hurts melee attackers when their hit is blocked. The
 * base shield's blocking behaviour (BLOCKS_ATTACKS component) is inherited, so it still blocks.
 */
public final class SteelShield {
    private SteelShield() {}

    public static final String MARKER = "vs_steel_shield";
    public static final int DURABILITY = 1200;     // vanilla shield is 336
    public static final float THORNS_DAMAGE = 4.0f;
    private static final int COLOR = 0xB8C0C8;

    public static ItemStack create() {
        ItemStack stack = new ItemStack(Items.SHIELD);
        stack.set(DataComponents.CUSTOM_NAME, Markers.name("Steel-Infused Shield", COLOR));
        Markers.applyMarker(stack, MARKER);
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(), List.of(), List.of("vanillaskills:steel_shield"), List.of()));
        stack.set(DataComponents.MAX_DAMAGE, DURABILITY);
        stack.set(DataComponents.REPAIRABLE, new Repairable(repairItems()));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                line("Hardened with steel for great durability.", ChatFormatting.GRAY),
                line("Blocking a melee hit injures the attacker.", ChatFormatting.GRAY))));
        return stack;
    }

    public static boolean isSteelShield(ItemStack stack) {
        return stack.is(Items.SHIELD) && Markers.has(stack, MARKER);
    }

    private static HolderSet<Item> repairItems() {
        return HolderSet.direct(List.<Holder<Item>>of(Items.IRON_INGOT.builtInRegistryHolder()));
    }

    private static Component line(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color).withStyle(s -> s.withItalic(false));
    }
}
