package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorTier;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorTiers;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Markers;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolTier;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolTiers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Skill-gated crafting: VanillaSkills armor and tools can be crafted only once the player has
 * unlocked the matching skill (which grants a "flag" effect). The raw materials/ingots are never
 * gated — only the finished gear. Enforced via {@code CraftResultGateMixin} on the crafting result
 * slot's {@code mayPickup}, so the item shows but can't be taken until the skill is unlocked.
 */
public final class CraftingGate {
    private CraftingGate() {}

    /** True if this crafted stack is VanillaSkills gear whose per-tier craft skill isn't unlocked. */
    public static boolean isLocked(Player player, ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (ArmorTier tier : ArmorTiers.TIERS) {
            if (tier.isWorn(stack)) return !hasFlag(player, "craft_armor_" + tier.id);
        }
        for (ToolTier tier : ToolTiers.TIERS) {
            if (Markers.has(stack, tier.markerKey)) return !hasFlag(player, "craft_tool_" + tier.id);
        }
        return false;
    }

    /** True if the player has unlocked the Armorsmith node that permits the Dragon smithing upgrade. */
    public static boolean canSmithDragon(Player player) {
        return hasFlag(player, "craft_armor_dragon");
    }

    /** Beneficial-potion duration multiplier from the Brewmaster lane (1.0 = none, up to 2.0). */
    public static float potionDurationMultiplier(Player player) {
        for (int level = 5; level >= 1; level--) {
            if (hasFlag(player, "long_potions_" + level)) return 1.0f + 0.2f * level;
        }
        return 1.0f;
    }

    /** Chance (0.0–0.2) to fully dodge incoming arrow damage, from the Evasion lane. */
    public static float arrowDodgeChance(Player player) {
        for (int level = 10; level >= 1; level--) {
            if (hasFlag(player, "arrow_dodge_" + level)) return 0.02f * level;
        }
        return 0.0f;
    }

    /** Bonus-crop tier (0–5) from the Cultivator lane; each tier = +20% bonus-crop chance. */
    public static int farmingLevel(Player player) {
        for (int level = 5; level >= 1; level--) {
            if (hasFlag(player, "cultivator_" + level)) return level;
        }
        return 0;
    }

    /** True if any of the player's unlocked skill nodes grants the given flag. */
    public static boolean hasFlag(Player player, String flag) {
        if (!(player instanceof ServerPlayer sp)) return false;
        PlayerSkillData data = VanillaSkills.PLAYERS.get(sp.getUUID());
        if (data == null) return false;
        SkillTree tree = VanillaSkills.TREE.tree();
        for (String id : data.unlocked) {
            SkillNode node = tree.byId(id);
            if (node == null) continue;
            for (SkillEffect effect : node.effects) {
                if ("flag".equals(effect.type) && flag.equals(effect.name)) return true;
            }
        }
        return false;
    }
}
