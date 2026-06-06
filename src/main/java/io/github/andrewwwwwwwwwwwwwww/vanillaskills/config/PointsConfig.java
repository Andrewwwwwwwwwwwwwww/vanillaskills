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
        cfg.perAdvancement = 5;
        cfg.startingPoints = 5;
        cfg.ignoreRecipeAdvancements = true;

        // Progression-weighted milestones (these REPLACE the base per-advancement value).
        // Tuned so a near-completionist earns ~900-930 points — slightly above the ~826 needed
        // for the whole tree, so full completion is a genuine late-game goal.
        Map<String, Integer> o = cfg.advancementOverrides;
        // Story
        o.put("minecraft:story/smelt_iron", 8);
        o.put("minecraft:story/mine_diamond", 12);
        o.put("minecraft:story/enter_the_nether", 12);
        // Nether
        o.put("minecraft:nether/obtain_blaze_rod", 12);
        o.put("minecraft:nether/get_wither_skull", 15);
        o.put("minecraft:nether/summon_wither", 25);
        o.put("minecraft:nether/create_beacon", 20);
        o.put("minecraft:nether/obtain_ancient_debris", 15);
        o.put("minecraft:nether/netherite_armor", 25);
        o.put("minecraft:nether/all_potions", 20);
        o.put("minecraft:nether/all_effects", 40);
        // End
        o.put("minecraft:end/enter_end", 12);
        o.put("minecraft:end/kill_dragon", 35);
        o.put("minecraft:end/respawn_dragon", 12);
        o.put("minecraft:end/elytra", 20);
        o.put("minecraft:end/levitate", 25);
        // Adventure
        o.put("minecraft:adventure/adventuring_time", 30);
        o.put("minecraft:adventure/kill_all_mobs", 30);
        o.put("minecraft:adventure/totem_of_undying", 15);
        o.put("minecraft:adventure/hero_of_the_village", 15);
        // Husbandry
        o.put("minecraft:husbandry/balanced_diet", 25);
        o.put("minecraft:husbandry/breed_all_animals", 25);
        o.put("minecraft:husbandry/obtain_netherite_hoe", 20);
        o.put("minecraft:husbandry/complete_catalogue", 12);

        // VanillaSkills' own advancements (extra, mod-themed point sources).
        o.put("vanillaskills:root", 0); // tick-granted to everyone — no free points
        o.put("vanillaskills:metallurgist", 5);
        o.put("vanillaskills:set_hardwood", 8);
        o.put("vanillaskills:set_rose_gold", 10);
        o.put("vanillaskills:set_steel", 12);
        o.put("vanillaskills:set_crystal", 15);
        o.put("vanillaskills:set_dragon", 25);
        o.put("vanillaskills:fortune_template", 8);
        o.put("vanillaskills:dragon_template", 10);
        o.put("vanillaskills:dragon_ingot", 8);
        o.put("vanillaskills:armored_elytra", 12);
        o.put("vanillaskills:steel_shield", 8);
        o.put("vanillaskills:specialist", 15);
        o.put("vanillaskills:completionist", 25);
        return cfg;
    }
}
