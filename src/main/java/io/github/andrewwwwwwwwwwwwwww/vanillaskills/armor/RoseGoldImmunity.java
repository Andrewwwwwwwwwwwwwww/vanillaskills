package io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor;

import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;

/**
 * While wearing a full Rose Gold set, the player is immune to all negative status effects
 * (harmful effects are stripped each tick). Piglin neutrality comes for free from the golden
 * armor base. Runs cheaply each server tick.
 */
public final class RoseGoldImmunity {
    private RoseGoldImmunity() {}

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!isFullSet(player)) continue;
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
    }

    private static boolean isFullSet(ServerPlayer player) {
        return ArmorTiers.ROSE_GOLD.isWorn(player.getItemBySlot(EquipmentSlot.HEAD))
                && ArmorTiers.ROSE_GOLD.isWorn(player.getItemBySlot(EquipmentSlot.CHEST))
                && ArmorTiers.ROSE_GOLD.isWorn(player.getItemBySlot(EquipmentSlot.LEGS))
                && ArmorTiers.ROSE_GOLD.isWorn(player.getItemBySlot(EquipmentSlot.FEET));
    }
}
