package io.github.andrewwwwwwwwwwwwwww.vanillaskills.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Controls how many skill points players earn from advancements.
 * Stored at config/vanillaskills/points.json.
 */
public class PointsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int perAdvancement = 1;
    public boolean ignoreRecipeAdvancements = true;
    public int startingPoints = 0;
    public Map<String, Integer> advancementOverrides = new HashMap<>();

    public int pointsFor(String advancementId) {
        Integer override = advancementOverrides.get(advancementId);
        if (override != null) return override;
        return perAdvancement;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("vanillaskills").resolve("points.json");
    }

    public static PointsConfig load() {
        Path path = path();
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path);
                PointsConfig cfg = GSON.fromJson(json, PointsConfig.class);
                if (cfg == null) cfg = new PointsConfig();
                if (cfg.advancementOverrides == null) cfg.advancementOverrides = new HashMap<>();
                return cfg;
            }
        } catch (Exception e) {
            VanillaSkills.LOGGER.error("Failed to load points.json, using defaults", e);
        }
        PointsConfig cfg = defaults();
        cfg.save();
        return cfg;
    }

    public void save() {
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(this));
        } catch (IOException e) {
            VanillaSkills.LOGGER.error("Failed to save points.json", e);
        }
    }

    private static PointsConfig defaults() {
        PointsConfig cfg = new PointsConfig();
        cfg.advancementOverrides.put("minecraft:story/mine_diamond", 3);
        cfg.advancementOverrides.put("minecraft:nether/obtain_blaze_rod", 2);
        cfg.advancementOverrides.put("minecraft:end/kill_dragon", 10);
        return cfg;
    }
}
