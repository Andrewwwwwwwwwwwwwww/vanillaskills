package io.github.andrewwwwwwwwwwwwwww.vanillaskills.gui;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.PointsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Shows the ways players earn skill points (read from points.json). */
public final class PointsScreen {
    private PointsScreen() {}

    public static void open(ServerPlayer player) {
        PointsConfig cfg = VanillaSkills.PLAYERS.pointsConfig();
        List<ItemStack> items = new ArrayList<>();

        items.add(item(Items.KNOWLEDGE_BOOK, "Earn Skill Points", ChatFormatting.GOLD, List.of(
                "Skill points come from completing advancements.")));
        items.add(item(Items.PAPER, "Each advancement", ChatFormatting.YELLOW, List.of(
                "+" + cfg.perAdvancement + " point" + (cfg.perAdvancement == 1 ? "" : "s"))));
        if (cfg.startingPoints > 0) {
            items.add(item(Items.EXPERIENCE_BOTTLE, "Starting points", ChatFormatting.AQUA, List.of(
                    "+" + cfg.startingPoints + " when you first join")));
        }
        if (cfg.ignoreRecipeAdvancements) {
            items.add(item(Items.CRAFTING_TABLE, "Recipe unlocks", ChatFormatting.GRAY, List.of(
                    "Give no points")));
        }
        if (cfg.advancementOverrides != null) {
            for (Map.Entry<String, Integer> e : cfg.advancementOverrides.entrySet()) {
                items.add(item(Items.NETHER_STAR, e.getKey(), ChatFormatting.LIGHT_PURPLE, List.of(
                        "+" + e.getValue() + " point" + (e.getValue() == 1 ? "" : "s"))));
            }
        }

        InfoMenu.open(player, styled("Earning Points", ChatFormatting.AQUA), 6, items);
    }

    private static ItemStack item(net.minecraft.world.item.Item icon, String name, ChatFormatting color, List<String> lore) {
        ItemStack stack = new ItemStack(icon);
        stack.set(DataComponents.CUSTOM_NAME, styled(name, color));
        List<Component> lines = new ArrayList<>();
        for (String line : lore) lines.add(styled(line, ChatFormatting.GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lines));
        return stack;
    }

    private static Component styled(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color).withStyle(s -> s.withItalic(false));
    }
}
