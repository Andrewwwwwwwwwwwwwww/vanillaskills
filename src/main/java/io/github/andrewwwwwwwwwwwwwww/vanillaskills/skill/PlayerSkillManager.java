package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.PointsConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns per-player skill data: loading/saving, point grants, unlocks, and applying effects.
 */
public class PlayerSkillManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<UUID, PlayerSkillData> cache = new ConcurrentHashMap<>();
    private PointsConfig points = new PointsConfig();

    public void setPointsConfig(PointsConfig points) {
        this.points = points;
    }

    public PointsConfig pointsConfig() {
        return points;
    }

    public PlayerSkillData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadFromDisk);
    }

    // ---- lifecycle ----

    /** Called when a player joins: load, initialize once, (re)apply all effects. */
    public void onJoin(ServerPlayer player) {
        PlayerSkillData data = get(player.getUUID());
        SkillTree tree = VanillaSkills.TREE.tree();

        if (!data.initialized) {
            data.pointsAvailable += points.startingPoints;
            data.pointsEarned += points.startingPoints;
            if (tree.has(SkillTree.ROOT_ID)) data.unlocked.add(SkillTree.ROOT_ID);
            tallyExistingAdvancements(player, data);
            data.initialized = true;
            save(player.getUUID());
        } else if (tree.has(SkillTree.ROOT_ID)) {
            data.unlocked.add(SkillTree.ROOT_ID);
        }

        applyAll(player);
        reevaluateAdvancements(player); // retroactively grant path/completion rewards
    }

    /** Re-check lane-completion and full-tree advancements (e.g. for trees finished before the feature existed). */
    private void reevaluateAdvancements(ServerPlayer player) {
        PlayerSkillData data = get(player.getUUID());
        SkillTree tree = VanillaSkills.TREE.tree();
        for (SkillCategory cat : tree.categories()) {
            boolean any = false, full = true;
            for (SkillNode n : tree.nodesIn(cat.id)) {
                any = true;
                if (!data.hasUnlocked(n.id)) { full = false; break; }
            }
            if (any && full) { awardAdvancement(player, "vanillaskills:specialist", "path"); break; }
        }
        checkCompletionist(player, data);
    }

    private void awardAdvancement(ServerPlayer player, String id, String criterion) {
        MinecraftServer server = VanillaSkills.server;
        if (server == null) return;
        for (AdvancementHolder holder : server.getAdvancements().getAllAdvancements()) {
            if (holder.id().toString().equals(id)) {
                player.getAdvancements().award(holder, criterion);
                return;
            }
        }
    }

    /** Reapply all attribute effects (e.g. after respawn). */
    public void applyAll(ServerPlayer player) {
        PlayerSkillData data = get(player.getUUID());
        SkillTree tree = VanillaSkills.TREE.tree();
        for (String id : data.unlocked) {
            SkillNode node = tree.byId(id);
            if (node != null) SkillEffects.applyNode(player, node);
        }
    }

    // ---- advancement points ----

    /** Called by the advancement mixin when an advancement completes. */
    public void onAdvancementCompleted(ServerPlayer player, String advancementId) {
        if (points.ignoreRecipeAdvancements && isRecipe(advancementId)) return;
        PlayerSkillData data = get(player.getUUID());
        if (data.creditedAdvancements.contains(advancementId)) return;

        int amount = points.pointsFor(advancementId);
        data.creditedAdvancements.add(advancementId);
        if (amount > 0) {
            data.grantPoints(amount);
            player.sendSystemMessage(Component.literal("+" + amount + " skill point" + (amount == 1 ? "" : "s"))
                    .withStyle(ChatFormatting.GREEN));
        }
        save(player.getUUID());
    }

    private void tallyExistingAdvancements(ServerPlayer player, PlayerSkillData data) {
        MinecraftServer server = VanillaSkills.server;
        if (server == null) return;
        for (AdvancementHolder holder : server.getAdvancements().getAllAdvancements()) {
            String id = holder.id().toString();
            if (points.ignoreRecipeAdvancements && isRecipe(id)) continue;
            if (data.creditedAdvancements.contains(id)) continue;
            if (player.getAdvancements().getOrStartProgress(holder).isDone()) {
                int amount = points.pointsFor(id);
                data.creditedAdvancements.add(id);
                if (amount > 0) data.grantPoints(amount);
            }
        }
    }

    /** Op command: wipe credited advancements and re-tally (e.g. after editing points.json). */
    public int recalc(ServerPlayer player) {
        PlayerSkillData data = get(player.getUUID());
        int before = data.pointsEarned;
        data.creditedAdvancements.clear();
        // Re-tally fully from scratch for earned points; keep spent unlocks intact.
        int earnedBefore = data.pointsEarned;
        int availableBefore = data.pointsAvailable;
        data.pointsEarned = 0;
        // recompute earned from advancements
        int spent = earnedBefore - availableBefore; // points already spent on unlocks
        tallyExistingAdvancements(player, data);
        data.pointsAvailable = Math.max(0, data.pointsEarned - spent);
        save(player.getUUID());
        return data.pointsEarned - before;
    }

    private static boolean isRecipe(String advancementId) {
        int colon = advancementId.indexOf(':');
        String path = colon >= 0 ? advancementId.substring(colon + 1) : advancementId;
        return path.startsWith("recipes/");
    }

    // ---- unlocking ----

    /** Attempt to unlock a node for the player; messages the player with the result. */
    public boolean unlock(ServerPlayer player, String nodeId) {
        SkillTree tree = VanillaSkills.TREE.tree();
        SkillNode node = tree.byId(nodeId);
        if (node == null) {
            player.sendSystemMessage(Component.literal("That skill no longer exists.").withStyle(ChatFormatting.RED));
            return false;
        }
        PlayerSkillData data = get(player.getUUID());
        if (data.hasUnlocked(nodeId)) return false;

        for (String req : node.requires) {
            if (!data.hasUnlocked(req)) {
                SkillNode reqNode = tree.byId(req);
                String reqName = reqNode != null ? reqNode.title : req;
                player.sendSystemMessage(Component.literal("Requires: " + reqName).withStyle(ChatFormatting.RED));
                return false;
            }
        }
        if (data.pointsAvailable < node.cost) {
            player.sendSystemMessage(Component.literal("Not enough points (need " + node.cost + ").")
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        data.pointsAvailable -= node.cost;
        data.unlocked.add(nodeId);
        SkillEffects.applyNode(player, node);
        checkPathAdvancement(player, node, data);
        checkCompletionist(player, data);
        save(player.getUUID());
        player.sendSystemMessage(Component.literal("Unlocked " + node.title + "!").withStyle(ChatFormatting.GREEN));
        return true;
    }

    /** Grants the "Specialist" advancement when the player has fully unlocked a lane. */
    private void checkPathAdvancement(ServerPlayer player, SkillNode node, PlayerSkillData data) {
        if (node.category == null) return;
        SkillTree tree = VanillaSkills.TREE.tree();
        for (SkillNode n : tree.nodesIn(node.category)) {
            if (!data.hasUnlocked(n.id)) return; // lane not complete yet
        }
        MinecraftServer server = VanillaSkills.server;
        if (server == null) return;
        for (AdvancementHolder holder : server.getAdvancements().getAllAdvancements()) {
            if (holder.id().toString().equals("vanillaskills:specialist")) {
                player.getAdvancements().award(holder, "path");
                break;
            }
        }
    }

    /** When every node in the tree is unlocked, grant the completion advancement + 5 Dragon Ingots (once). */
    private void checkCompletionist(ServerPlayer player, PlayerSkillData data) {
        SkillTree tree = VanillaSkills.TREE.tree();
        for (SkillNode n : tree.nodes) {
            if (!data.hasUnlocked(n.id)) return; // tree not fully unlocked
        }
        MinecraftServer server = VanillaSkills.server;
        if (server == null) return;
        for (AdvancementHolder holder : server.getAdvancements().getAllAdvancements()) {
            if (holder.id().toString().equals("vanillaskills:completionist")) {
                if (player.getAdvancements().getOrStartProgress(holder).isDone()) return; // already rewarded
                player.getAdvancements().award(holder, "complete");
                net.minecraft.world.item.ItemStack reward =
                        io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonIngot.create();
                reward.setCount(5);
                if (!player.getInventory().add(reward)) player.drop(reward, false);
                player.sendSystemMessage(Component.literal("Skill tree mastered! +5 Dragon Ingots")
                        .withStyle(ChatFormatting.GOLD));
                return;
            }
        }
    }

    /** Op command: clear all unlocks and refund earned points. */
    public void reset(ServerPlayer player) {
        SkillTree tree = VanillaSkills.TREE.tree();
        PlayerSkillData data = get(player.getUUID());
        for (String id : data.unlocked) {
            SkillNode node = tree.byId(id);
            if (node != null) SkillEffects.removeNode(player, node);
        }
        data.unlocked.clear();
        data.pointsAvailable = data.pointsEarned;
        if (tree.has(SkillTree.ROOT_ID)) data.unlocked.add(SkillTree.ROOT_ID);
        save(player.getUUID());
    }

    public void addPoints(ServerPlayer player, int amount) {
        PlayerSkillData data = get(player.getUUID());
        data.grantPoints(amount);
        save(player.getUUID());
    }

    public void setPoints(ServerPlayer player, int amount) {
        PlayerSkillData data = get(player.getUUID());
        int spent = data.pointsEarned - data.pointsAvailable;
        data.pointsAvailable = Math.max(0, amount);
        data.pointsEarned = data.pointsAvailable + Math.max(0, spent);
        save(player.getUUID());
    }

    // ---- persistence ----

    private Path playersDir() {
        MinecraftServer server = VanillaSkills.server;
        return server.getWorldPath(LevelResource.ROOT).resolve("vanillaskills").resolve("players");
    }

    private Path playerFile(UUID uuid) {
        return playersDir().resolve(uuid + ".json");
    }

    private PlayerSkillData loadFromDisk(UUID uuid) {
        Path file = playerFile(uuid);
        try {
            if (Files.exists(file)) {
                PlayerSkillData data = GSON.fromJson(Files.readString(file), PlayerSkillData.class);
                if (data != null) {
                    data.normalize();
                    return data;
                }
            }
        } catch (Exception e) {
            VanillaSkills.LOGGER.error("Failed to load skill data for {}", uuid, e);
        }
        return new PlayerSkillData();
    }

    public void save(UUID uuid) {
        PlayerSkillData data = cache.get(uuid);
        if (data == null) return;
        try {
            Files.createDirectories(playersDir());
            Files.writeString(playerFile(uuid), GSON.toJson(data));
        } catch (IOException e) {
            VanillaSkills.LOGGER.error("Failed to save skill data for {}", uuid, e);
        }
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) save(uuid);
    }

    /** Save everything and drop the cache (on server stop, so singleplayer world-switches start clean). */
    public void saveAllAndClear() {
        saveAll();
        cache.clear();
    }

    public void unload(UUID uuid) {
        save(uuid);
        cache.remove(uuid);
    }
}
