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
        int per = cfg.perAdvancement;
        List<ItemStack> items = new ArrayList<>();

        items.add(item(Items.KNOWLEDGE_BOOK, "Earning Skill Points", ChatFormatting.GOLD, List.of(
                "Points come from completing advancements.",
                "Each advancement counts once — they can't be farmed.")));

        items.add(item(Items.PAPER, "Every Advancement", ChatFormatting.YELLOW, List.of(
                "+" + per + " point" + (per == 1 ? "" : "s") + " each")));

        items.add(item(Items.NETHER_STAR, "Major Milestones", ChatFormatting.LIGHT_PURPLE, List.of(
                "Big goals — Nether, the End, bosses,",
                "netherite, and full collections —",
                "award far more than a normal advancement.")));

        items.add(item(Items.DIAMOND_CHESTPLATE, "VanillaSkills Goals", ChatFormatting.AQUA, List.of(
                "Craft full armor sets, discover the",
                "upgrade templates, forge a Dragon Ingot,",
                "and finish skill paths for bonus points.")));

        if (cfg.startingPoints > 0) {
            items.add(item(Items.EXPERIENCE_BOTTLE, "Starting Bonus", ChatFormatting.GREEN, List.of(
                    "+" + cfg.startingPoints + " points when you first join")));
        }

        if (cfg.ignoreRecipeAdvancements) {
            items.add(item(Items.CRAFTING_TABLE, "Recipe Unlocks", ChatFormatting.GRAY, List.of(
                    "Give no points.")));
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
