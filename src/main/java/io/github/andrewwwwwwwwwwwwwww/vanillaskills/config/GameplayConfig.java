package io.github.andrewwwwwwwwwwwwwww.vanillaskills.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Optional gameplay toggles. Stored at config/vanillaskills/gameplay.json.
 */
public class GameplayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Live flag read by {@code ItemEnchantmentsMutableMixin}. {@code false} (the default) means the mod
     * strips Mending from every newly enchanted or generated item, exactly as it did before this became
     * configurable. When {@code true}, Mending is left in the game and behaves normally.
     */
    public static volatile boolean MENDING_ENABLED = false;

    /** When true, Mending is available as normal; when false, the mod removes it everywhere. */
    public boolean mendingEnabled = false;

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("vanillaskills").resolve("gameplay.json");
    }

    /** Load gameplay.json (writing a default file if absent) and publish its flags. */
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
        MENDING_ENABLED = cfg.mendingEnabled;
        return cfg;
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
