package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads, saves and edits the server's skill tree (config/vanillaskills/skilltree.json).
 * If no file exists, the built-in 5-lane starter tree is written out.
 */
public class SkillTreeManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SkillTree tree = new SkillTree();

    public SkillTree tree() {
        return tree;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("vanillaskills").resolve("skilltree.json");
    }

    public void load() {
        Path path = path();
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path);
                SkillTree loaded = GSON.fromJson(json, SkillTree.class);
                tree = loaded != null ? loaded : defaultTree();
            } else {
                tree = defaultTree();
                save();
            }
        } catch (Exception e) {
            VanillaSkills.LOGGER.error("Failed to load skilltree.json, using default tree", e);
            tree = defaultTree();
        }
        tree.index();
        VanillaSkills.LOGGER.info("Loaded skill tree '{}' with {} nodes", tree.title, tree.size());
    }

    public void save() {
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(tree));
        } catch (IOException e) {
            VanillaSkills.LOGGER.error("Failed to save skilltree.json", e);
        }
    }

    /** Re-index and persist after an edit. */
    public void touchAndSave() {
        tree.index();
        save();
    }

    // ---- Default starter tree (5 lanes: Health, Speed, Mining, Luck, Damage) ----

    private static SkillTree defaultTree() {
        SkillTree t = new SkillTree();
        t.version = 1;
        t.title = "Skills";
        t.rows = 6;

        SkillNode root = new SkillNode("root", "Awakening", 49, 0, "minecraft:nether_star");
        root.description.add("The root of your potential.");
        t.nodes.add(root);

        // Health lane (col 0): slots 36, 27, 18
        addLane(t, "health", "Vitality", "minecraft:golden_apple",
                new int[]{36, 27, 18},
                new SkillEffect[]{
                        SkillEffect.attribute("minecraft:max_health", "add_value", 2.0),
                        SkillEffect.attribute("minecraft:max_health", "add_value", 2.0),
                        SkillEffect.attribute("minecraft:max_health", "add_value", 2.0)
                },
                new String[]{"+2 max health", "+2 max health (4 total)", "+2 max health (6 total)"});

        // Speed lane (col 2): slots 38, 29, 20
        addLane(t, "speed", "Fleet Foot", "minecraft:feather",
                new int[]{38, 29, 20},
                new SkillEffect[]{
                        SkillEffect.attribute("minecraft:movement_speed", "add_multiplied_base", 0.05),
                        SkillEffect.attribute("minecraft:movement_speed", "add_multiplied_base", 0.05),
                        SkillEffect.attribute("minecraft:movement_speed", "add_multiplied_base", 0.05)
                },
                new String[]{"+5% movement speed", "+5% movement speed (10% total)", "+5% movement speed (15% total)"});

        // Mining lane (col 4): slots 40, 31, 22
        addLane(t, "mining", "Prospector", "minecraft:iron_pickaxe",
                new int[]{40, 31, 22},
                new SkillEffect[]{
                        SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 1.0),
                        SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 2.0),
                        SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 2.0)
                },
                new String[]{"+1 mining efficiency", "+2 mining efficiency (3 total)", "+2 mining efficiency (5 total)"});

        // Luck lane (col 6): slots 42, 33, 24
        addLane(t, "luck", "Fortune Finder", "minecraft:rabbit_foot",
                new int[]{42, 33, 24},
                new SkillEffect[]{
                        SkillEffect.attribute("minecraft:luck", "add_value", 1.0),
                        SkillEffect.attribute("minecraft:luck", "add_value", 1.0),
                        SkillEffect.attribute("minecraft:luck", "add_value", 1.0)
                },
                new String[]{"+1 luck", "+1 luck (2 total)", "+1 luck (3 total)"});

        // Damage lane (col 8): slots 44, 35, 26
        addLane(t, "damage", "Warrior", "minecraft:iron_sword",
                new int[]{44, 35, 26},
                new SkillEffect[]{
                        SkillEffect.attribute("minecraft:attack_damage", "add_value", 1.0),
                        SkillEffect.attribute("minecraft:attack_damage", "add_value", 1.0),
                        SkillEffect.attribute("minecraft:attack_damage", "add_value", 2.0)
                },
                new String[]{"+1 attack damage", "+1 attack damage (2 total)", "+2 attack damage (4 total)"});

        return t;
    }

    private static void addLane(SkillTree t, String key, String name, String icon,
                                int[] slots, SkillEffect[] effects, String[] descriptions) {
        String[] numerals = {"I", "II", "III"};
        for (int i = 0; i < 3; i++) {
            SkillNode node = new SkillNode(key + "_" + (i + 1), name + " " + numerals[i], slots[i], i + 1, icon);
            node.description.add(descriptions[i]);
            node.effects.add(effects[i]);
            node.requires.add(i == 0 ? "root" : key + "_" + i);
            t.nodes.add(node);
        }
    }
}
