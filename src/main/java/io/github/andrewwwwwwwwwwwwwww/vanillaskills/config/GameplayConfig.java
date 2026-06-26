package io.github.andrewwwwwwwwwwwwwww.vanillaskills.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.QuestShop;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Quests;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Optional gameplay/pacing config. Stored at config/vanillaskills/gameplay.json. These are server-side
 * settings (the bounty board, shop, and economy are shared) — edit the file (or use the Mod Menu screen
 * in singleplayer) and they apply on load / {@code /skill reload}, no cheats required.
 */
public class GameplayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // --- Live values published to the rest of the mod on load() ---

    /** Read by {@code ItemEnchantmentsMutableMixin}. false (default) = Mending is stripped everywhere. */
    public static volatile boolean MENDING_ENABLED = false;
    /** Read by {@code QuestBoard} when re-rolling: ms between bounty rotations. */
    public static volatile long BOUNTY_REFRESH_MS = 5L * 3_600_000L;
    /** Read by {@code QuestShop}: ms between shop rotations. */
    public static volatile long SHOP_REFRESH_MS = 24L * 3_600_000L;
    // QuestShop.CONVERT_RATIO and Quests.GRADUATE_AT are pushed directly on load().

    // --- Persisted fields (gameplay.json) ---

    /** When true, Mending is available as normal; when false, the mod removes it everywhere. */
    public boolean mendingEnabled = false;
    /** Hours between bounty-board rotations (default 5). */
    public int bountyRefreshHours = 5;
    /** Hours between Quest Shop rotations (default 24). */
    public int shopRefreshHours = 24;
    /** Quest Shards needed per 1 Skill Shard at the converter (default 3). */
    public int convertRatio = 3;
    /** Bounties to finish on the starter board before joining the shared main board (default 15). */
    public int graduateAt = 15;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("vanillaskills").resolve("gameplay.json");
    }

    /** Load gameplay.json (writing a default file if absent) and publish its values across the mod. */
    public static GameplayConfig load() {
        Path path = path();
        GameplayConfig cfg;
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path);
                cfg = GSON.fromJson(json, GameplayConfig.class);
                if (cfg == null) cfg = new GameplayConfig();
            } else {
                cfg = new GameplayConfig();
                cfg.save();
            }
        } catch (Exception e) {
            VanillaSkills.LOGGER.error("Failed to load gameplay.json, using defaults", e);
            cfg = new GameplayConfig();
        }
        cfg.apply();
        return cfg;
    }

    /** Publish this config's values to the live flags / consumers (clamped to sane minimums). */
    public void apply() {
        MENDING_ENABLED = mendingEnabled;
        BOUNTY_REFRESH_MS = Math.max(1, bountyRefreshHours) * 3_600_000L;
        SHOP_REFRESH_MS = Math.max(1, shopRefreshHours) * 3_600_000L;
        QuestShop.CONVERT_RATIO = Math.max(1, convertRatio);
        Quests.GRADUATE_AT = Math.max(1, graduateAt);
    }

    public void save() {
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(this));
        } catch (IOException e) {
            VanillaSkills.LOGGER.error("Failed to save gameplay.json", e);
        }
    }
}
