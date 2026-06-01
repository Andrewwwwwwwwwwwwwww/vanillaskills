package io.github.andrewwwwwwwwwwwwwww.vanillaskills.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/** The player's current attribute totals, shown as icons whose tooltip is the value. */
public final class StatsScreen {
    private StatsScreen() {}

    public static void open(ServerPlayer player) {
        List<ItemStack> items = new ArrayList<>();
        add(items, player, Attributes.MAX_HEALTH, Items.APPLE, "Max Health");
        add(items, player, Attributes.ARMOR, Items.IRON_CHESTPLATE, "Armor");
        add(items, player, Attributes.ARMOR_TOUGHNESS, Items.DIAMOND_CHESTPLATE, "Armor Toughness");
        add(items, player, Attributes.KNOCKBACK_RESISTANCE, Items.SHIELD, "Knockback Resistance");
        add(items, player, Attributes.ATTACK_DAMAGE, Items.IRON_SWORD, "Attack Damage");
        add(items, player, Attributes.ATTACK_SPEED, Items.CLOCK, "Attack Speed");
        add(items, player, Attributes.MOVEMENT_SPEED, Items.FEATHER, "Movement Speed");
        add(items, player, Attributes.MINING_EFFICIENCY, Items.IRON_PICKAXE, "Mining Efficiency");
        add(items, player, Attributes.LUCK, Items.RABBIT_FOOT, "Luck");
        InfoMenu.open(player, styled("Your Stats", ChatFormatting.AQUA), 3, items);
    }

    private static void add(List<ItemStack> items, ServerPlayer player, Holder<Attribute> attribute, Item icon, String label) {
        ItemStack stack = new ItemStack(icon);
        stack.set(DataComponents.CUSTOM_NAME, styled(label, ChatFormatting.AQUA));
        double value = player.getAttributeValue(attribute);
        stack.set(DataComponents.LORE, new ItemLore(List.of(styled(format(value), ChatFormatting.WHITE))));
        items.add(stack);
    }

    private static String format(double value) {
        return Math.abs(value - Math.rint(value)) < 1e-6 ? String.valueOf((long) value) : String.format("%.2f", value);
    }

    private static Component styled(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color).withStyle(s -> s.withItalic(false));
    }
}
