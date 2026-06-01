package io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Rose Gold set behaviour:
 * - Immunity to all negative status effects, but ONLY while the full set is worn.
 * - A live tooltip on worn pieces showing which of the four pieces are present/missing. Pieces in
 *   the player's inventory are reset to a static description so they don't show stale info.
 */
public final class RoseGoldSet {
    private RoseGoldSet() {}

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final String[] SLOT_NAMES = {"Helmet", "Chestplate", "Leggings", "Boots"};

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            update(player);
        }
    }

    private static void update(ServerPlayer player) {
        ItemStack[] worn = new ItemStack[4];
        boolean[] present = new boolean[4];
        int count = 0;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = player.getItemBySlot(ARMOR_SLOTS[i]);
            worn[i] = stack;
            if (ArmorTiers.ROSE_GOLD.isWorn(stack)) {
                present[i] = true;
                count++;
            }
        }

        if (count == 4) {
            stripHarmfulEffects(player);
        }

        // Live checklist on each worn rose gold piece.
        ItemLore live = liveLore(present, count);
        for (int i = 0; i < 4; i++) {
            if (present[i]) ensureLore(worn[i], live);
        }

        // Reset rose gold pieces sitting in the inventory (not worn) to the static description.
        ItemLore base = baseLore();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack == worn[0] || stack == worn[1] || stack == worn[2] || stack == worn[3]) continue;
            if (ArmorTiers.ROSE_GOLD.isWorn(stack)) ensureLore(stack, base);
        }
    }

    private static void ensureLore(ItemStack stack, ItemLore desired) {
        ItemLore current = stack.get(DataComponents.LORE);
        if (!desired.equals(current)) {
            stack.set(DataComponents.LORE, desired);
        }
    }

    /** Static description shown on a crafted / stored Rose Gold piece. */
    public static ItemLore baseLore() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(""));
        lines.add(styled("Rose Gold Set", ChatFormatting.AQUA));
        lines.add(styled("Full set: immune to all", ChatFormatting.GRAY));
        lines.add(styled("negative status effects", ChatFormatting.GRAY));
        return new ItemLore(lines);
    }

    private static ItemLore liveLore(boolean[] present, int count) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(""));
        lines.add(styled("Rose Gold Set (" + count + "/4)", ChatFormatting.AQUA));
        for (int i = 0; i < 4; i++) {
            lines.add(styled((present[i] ? "+ " : "- ") + SLOT_NAMES[i],
                    present[i] ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
        lines.add(count == 4
                ? styled("Immune to negative effects", ChatFormatting.GREEN)
                : styled("Wear all 4 for immunity", ChatFormatting.GRAY));
        return new ItemLore(lines);
    }

    private static void stripHarmfulEffects(ServerPlayer player) {
        List<Holder<MobEffect>> harmful = null;
        for (MobEffectInstance instance : player.getActiveEffects()) {
            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                if (harmful == null) harmful = new ArrayList<>();
                harmful.add(instance.getEffect());
            }
        }
        if (harmful != null) {
            for (Holder<MobEffect> effect : harmful) player.removeEffect(effect);
        }
    }

    private static Component styled(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color).withStyle(s -> s.withItalic(false));
    }
}
