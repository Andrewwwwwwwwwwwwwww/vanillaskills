package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Markers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** Per-player bounty progress: rotation sync, kill tracking, gather turn-in, and claiming. */
public final class Quests {
    private Quests() {}

    /** Resets a player's quest progress when the board has rolled over to a new rotation. */
    public static void sync(ServerPlayer player) {
        PlayerSkillData data = VanillaSkills.PLAYERS.get(player.getUUID());
        long rotation = VanillaSkills.QUESTS.rotationId();
        if (data.questRotation != rotation) {
            data.questRotation = rotation;
            data.questKills.clear();
            data.questClaimed.clear();
        }
    }

    /** Called when a player kills something — advances any matching active KILL quests. */
    public static void onKill(ServerPlayer killer, Entity dead) {
        sync(killer);
        PlayerSkillData data = VanillaSkills.PLAYERS.get(killer.getUUID());
        List<Quest> active = VanillaSkills.QUESTS.active();
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(dead.getType()).toString();
        boolean hostile = dead instanceof Enemy;
        boolean changed = false;
        for (int i = 0; i < active.size(); i++) {
            Quest q = active.get(i);
            if (q.type() != Quest.Type.KILL || data.questClaimed.contains(i)) continue;
            boolean match = q.target().equals(Quest.ANY_HOSTILE) ? hostile : q.target().equals(id);
            if (!match) continue;
            int cur = data.questKills.getOrDefault(i, 0);
            if (cur >= q.amount()) continue;
            data.questKills.put(i, cur + 1);
            changed = true;
            if (cur + 1 == q.amount()) {
                killer.sendSystemMessage(Component.literal("Bounty ready to claim: " + q.title() + " — /quests")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
        if (changed) VanillaSkills.PLAYERS.save(killer.getUUID());
    }

    public static boolean isClaimed(ServerPlayer player, int index) {
        return VanillaSkills.PLAYERS.get(player.getUUID()).questClaimed.contains(index);
    }

    /** Progress toward the quest (kills done, or items currently held), capped at the amount. */
    public static int progress(ServerPlayer player, int index) {
        Quest q = VanillaSkills.QUESTS.active(index);
        if (q == null) return 0;
        if (q.type() == Quest.Type.KILL) {
            return Math.min(q.amount(), VanillaSkills.PLAYERS.get(player.getUUID()).questKills.getOrDefault(index, 0));
        }
        return Math.min(q.amount(), countItem(player, q.target()));
    }

    /** Attempt to claim a quest's reward; messages the player with the result. */
    public static void claim(ServerPlayer player, int index) {
        sync(player);
        Quest q = VanillaSkills.QUESTS.active(index);
        if (q == null) return;
        PlayerSkillData data = VanillaSkills.PLAYERS.get(player.getUUID());
        if (data.questClaimed.contains(index)) {
            player.sendSystemMessage(Component.literal("You've already claimed this bounty.").withStyle(ChatFormatting.RED));
            return;
        }
        if (q.type() == Quest.Type.KILL) {
            int cur = data.questKills.getOrDefault(index, 0);
            if (cur < q.amount()) {
                player.sendSystemMessage(Component.literal("Not done yet: " + cur + "/" + q.amount()).withStyle(ChatFormatting.RED));
                return;
            }
        } else {
            int have = countItem(player, q.target());
            if (have < q.amount()) {
                player.sendSystemMessage(Component.literal("You need " + (q.amount() - have) + " more.").withStyle(ChatFormatting.RED));
                return;
            }
            removeItem(player, q.target(), q.amount());
        }
        data.questClaimed.add(index);
        VanillaSkills.PLAYERS.addPoints(player, q.reward());
        player.sendSystemMessage(Component.literal("Bounty complete: " + q.title() + "  +" + q.reward()
                + " skill point" + (q.reward() == 1 ? "" : "s")).withStyle(ChatFormatting.GOLD));
        VanillaSkills.PLAYERS.save(player.getUUID());
    }

    public static Item item(String id) {
        Identifier loc = Identifier.tryParse(id);
        if (loc == null) return Items.PAPER;
        return BuiltInRegistries.ITEM.get(loc).map(Holder::value).orElse(Items.PAPER);
    }

    private static int countItem(ServerPlayer player, String id) {
        Item item = item(id);
        int n = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(item) && !Markers.isOurs(s)) n += s.getCount(); // don't count our marked variants
        }
        return n;
    }

    private static void removeItem(ServerPlayer player, String id, int amount) {
        Item item = item(id);
        int remaining = amount;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(item) && !Markers.isOurs(s)) {
                int take = Math.min(remaining, s.getCount());
                s.shrink(take);
                remaining -= take;
            }
        }
    }
}
