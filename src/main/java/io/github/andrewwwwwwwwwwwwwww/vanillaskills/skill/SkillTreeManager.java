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

    /**
     * Overwrite the tree with the built-in default (picking up new lanes/nodes after a mod update),
     * backing up any existing skilltree.json first.
     *
     * @return the backup file path, or null if there was no existing file (or the backup failed).
     */
    public Path regenerate() {
        Path path = path();
        Path backup = null;
        try {
            if (Files.exists(path)) {
                backup = path.resolveSibling("skilltree.backup-" + System.currentTimeMillis() + ".json");
                Files.copy(path, backup, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            VanillaSkills.LOGGER.error("Failed to back up skilltree.json before regen", e);
            backup = null;
        }
        tree = defaultTree();
        tree.index();
        save();
        VanillaSkills.LOGGER.info("Regenerated default skill tree ({} nodes)", tree.size());
        return backup;
    }

    // ---- Default starter tree (5 lanes: Health, Speed, Mining, Luck, Damage) ----

    // Lane-relative slots for the 3 tiers (a vertical path up the centre of the lane view).
    private static final int[] TIER_SLOTS = {40, 31, 22};
    // Five-tier vertical path (centre column, rows 4..0) for the longer lanes — clear of the
    // bottom-row Back/Points/Stats buttons.
    private static final int[] TIER_SLOTS_5 = {40, 31, 22, 13, 4};
    // Cost progression for the 5-node craft/brew lanes (start at 10, scaling).
    private static final int[] COSTS_5 = {10, 15, 20, 25, 30};
    // Two centred rows of five (rows 2-3, columns 2-6) for 10-node lanes.
    private static final int[] FIVE_AND_FIVE = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
    // Lane icons on the lane-select screen, laid out as a tidy 6x2 block:
    //   row 1 (10..16): Vitality, Fleet Foot, Prospector, Fortune, Warrior, Guardian, Reach
    //   row 2 (19..25): Mountaineer, Aquatic, Armorsmith, Toolsmith, Brewmaster, Evasion, Cultivator
    //   row 3 (31):     Night Vision (capstone, centred under the two rows)
    private static final int[] CATEGORY_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 31};

    private static SkillTree defaultTree() {
        SkillTree t = new SkillTree();
        t.version = 1;
        t.title = "Skills";
        t.rows = 6;

        // Vitality: +1 heart (2 HP) per node, up to 3 full rows of hearts (60 HP total) in 20 nodes.
        addScalingLane(t, "health", "Vitality", "minecraft:golden_apple", 0,
                "minecraft:max_health", "add_value", 2.0, gridSlots(20),
                n -> "+1 heart — +" + (2 * n) + " HP total");

        // Fleet Foot: +2% movement speed per node, up to +30%.
        addScalingLane(t, "speed", "Fleet Foot", "minecraft:feather", 1,
                "minecraft:movement_speed", "add_multiplied_base", 0.02, gridSlots(15),
                n -> "+2% movement speed — +" + (2 * n) + "% total");

        // Prospector: 5 nodes totalling +12 mining efficiency, enough that an Efficiency V diamond
        // pickaxe (speed 34) clears the 45-speed instamine threshold for stone — i.e. Haste II speeds.
        addLaneNodes(t, "mining", "Prospector", "minecraft:diamond_pickaxe", 2,
                TIER_SLOTS_5, new int[]{2, 4, 6, 8, 10},
                new SkillEffect[][]{
                        {SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 2.0)},
                        {SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 2.0)},
                        {SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 2.0)},
                        {SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 3.0)},
                        {SkillEffect.attribute("minecraft:mining_efficiency", "add_value", 3.0)}
                },
                new String[]{"+2 mining efficiency", "+2 (4 total)", "+2 (6 total)", "+3 (9 total)",
                        "+3 (12 total) — instamine stone with an Efficiency V diamond pickaxe"});

        // Fortune Finder: +0.5 luck per node, up to +5.
        addScalingLane(t, "luck", "Fortune Finder", "minecraft:rabbit_foot", 3,
                "minecraft:luck", "add_value", 0.5, FIVE_AND_FIVE,
                n -> "+0.5 luck — +" + fmt(0.5 * n) + " total");

        // Warrior: +0.5 attack damage per node, up to +10.
        addScalingLane(t, "damage", "Warrior", "minecraft:iron_sword", 4,
                "minecraft:attack_damage", "add_value", 0.5, gridSlots(20),
                n -> "+0.5 attack damage — +" + fmt(0.5 * n) + " total");

        // Guardian: +1 armor per node, up to +10.
        addScalingLane(t, "guardian", "Guardian", "minecraft:iron_chestplate", 5,
                "minecraft:armor", "add_value", 1.0, FIVE_AND_FIVE,
                n -> "+1 armor — +" + n + " total");

        // Reach: +0.25 block & entity interaction range per node, up to +2.5.
        addScalingLaneMulti(t, "reach", "Reach", "minecraft:spyglass", 6, FIVE_AND_FIVE,
                new String[]{"minecraft:block_interaction_range", "minecraft:entity_interaction_range"},
                "add_value", 0.25,
                n -> "+0.25 block & entity reach — +" + fmt(0.25 * n) + " total");

        // Mountaineer: pricier than the other short lanes — auto-stepping full blocks is strong QoL.
        addLaneNodes(t, "mountaineer", "Mountaineer", "minecraft:ladder", 7,
                TIER_SLOTS, new int[]{5, 10, 15},
                new SkillEffect[][]{
                        {SkillEffect.attribute("minecraft:step_height", "add_value", 0.2)},
                        {SkillEffect.attribute("minecraft:step_height", "add_value", 0.2)},
                        {SkillEffect.attribute("minecraft:step_height", "add_value", 0.1)}
                },
                new String[]{"Step up taller ledges", "Step up a full block", "Step up 1.1 blocks"});

        // Aquatic: 9 nodes spreading three underwater perks — breath, swim speed, underwater mining.
        // Costs ramp up steeply so reaching full underwater capability is a real investment.
        addLaneNodes(t, "aquatic", "Aquatic", "minecraft:heart_of_the_sea", 8,
                gridSlots(9), new int[]{3, 5, 7, 9, 11, 13, 16, 19, 24},
                new SkillEffect[][]{
                        {SkillEffect.attribute("minecraft:oxygen_bonus", "add_value", 1.0)},
                        {SkillEffect.attribute("minecraft:water_movement_efficiency", "add_value", 0.34)},
                        {SkillEffect.attribute("minecraft:submerged_mining_speed", "add_value", 0.27)},
                        {SkillEffect.attribute("minecraft:oxygen_bonus", "add_value", 1.0)},
                        {SkillEffect.attribute("minecraft:water_movement_efficiency", "add_value", 0.33)},
                        {SkillEffect.attribute("minecraft:submerged_mining_speed", "add_value", 0.27)},
                        {SkillEffect.attribute("minecraft:oxygen_bonus", "add_value", 1.0)},
                        {SkillEffect.attribute("minecraft:water_movement_efficiency", "add_value", 0.33)},
                        {SkillEffect.attribute("minecraft:submerged_mining_speed", "add_value", 0.26)}
                },
                new String[]{"+1 breath", "+34% swim speed", "+27% underwater mining",
                        "+1 breath (+2)", "+67% swim speed", "+54% underwater mining",
                        "+1 breath (+3)", "Full surface swim speed", "Full underwater mining"});

        // Armorsmith: one node per armor tier, unlocking the right to craft that tier's armor.
        addLaneNodes(t, "armorsmith", "Armorsmith", "minecraft:smithing_table", 9, TIER_SLOTS_5, COSTS_5,
                flagEffects("craft_armor_hardwood", "craft_armor_rose_gold", "craft_armor_steel",
                        "craft_armor_crystal", "craft_armor_dragon"),
                new String[]{"Craft Hardwood armor", "Craft Rose Gold armor", "Craft Steel armor",
                        "Craft Crystalline armor", "Craft Dragon armor (smithing upgrade)"});

        // Toolsmith: one node per tool tier.
        addLaneNodes(t, "toolsmith", "Toolsmith", "minecraft:crafting_table", 10, TIER_SLOTS_5, COSTS_5,
                flagEffects("craft_tool_hardwood", "craft_tool_rose_gold", "craft_tool_steel",
                        "craft_tool_crystal", "craft_tool_dragon"),
                new String[]{"Craft Hardwood tools", "Craft Rose Gold tools", "Craft Steel tools",
                        "Craft Crystalline tools", "Craft Dragon tools"});

        // Brewmaster: each node extends beneficial potion durations by a further +20% (up to +100%).
        addLaneNodes(t, "brewmaster", "Brewmaster", "minecraft:brewing_stand", 11, TIER_SLOTS_5, COSTS_5,
                flagEffects("long_potions_1", "long_potions_2", "long_potions_3", "long_potions_4", "long_potions_5"),
                new String[]{"Beneficial potions +20% duration", "+40% total", "+60% total", "+80% total",
                        "+100% total (well past the 8-min cap)"});

        // Evasion: +2% chance to completely dodge incoming arrow damage per node, up to 20%.
        // Expensive — fully dodging 1-in-5 arrows is a strong defensive perk.
        addLaneNodes(t, "evasion", "Evasion", "minecraft:arrow", 12,
                FIVE_AND_FIVE, new int[]{3, 5, 7, 9, 11, 13, 16, 19, 22, 26},
                flagEffects("arrow_dodge_1", "arrow_dodge_2", "arrow_dodge_3", "arrow_dodge_4", "arrow_dodge_5",
                        "arrow_dodge_6", "arrow_dodge_7", "arrow_dodge_8", "arrow_dodge_9", "arrow_dodge_10"),
                new String[]{"+2% chance to dodge arrows", "4% total", "6% total", "8% total", "10% total",
                        "12% total", "14% total", "16% total", "18% total", "20% total"});

        // Cultivator: each node adds +20% chance for bonus crops when harvesting mature crops (→100%).
        addLaneNodes(t, "cultivator", "Cultivator", "minecraft:wheat", 13,
                TIER_SLOTS_5, new int[]{3, 5, 8, 11, 15},
                flagEffects("cultivator_1", "cultivator_2", "cultivator_3", "cultivator_4", "cultivator_5"),
                new String[]{"+20% chance for bonus crops", "40% chance", "60% chance", "80% chance",
                        "100% chance (always bonus)"});

        // Night Vision: a single capstone node granting permanent night vision.
        addLaneNodes(t, "nightvision", "Night Vision", "minecraft:golden_carrot", 14,
                new int[]{22}, new int[]{150},
                new SkillEffect[][]{ { SkillEffect.status("minecraft:night_vision", 0) } },
                new String[]{"Permanent Night Vision"});

        return t;
    }

    /** A many-node lane where each node adds {@code perNode} of one attribute, at the given slots. */
    private static void addScalingLane(SkillTree t, String key, String name, String icon, int laneIndex,
                                       String attribute, String operation, double perNode, int[] slots,
                                       java.util.function.IntFunction<String> describe) {
        int count = slots.length;
        SkillEffect[][] effects = new SkillEffect[count][];
        String[] descriptions = new String[count];
        for (int i = 0; i < count; i++) {
            effects[i] = new SkillEffect[]{SkillEffect.attribute(attribute, operation, perNode)};
            descriptions[i] = describe.apply(i + 1);
        }
        addLaneNodes(t, key, name, icon, laneIndex, slots, scalingCosts(count), effects, descriptions);
    }

    /** Like {@link #addScalingLane} but each node adds {@code perNode} to several attributes at once. */
    private static void addScalingLaneMulti(SkillTree t, String key, String name, String icon, int laneIndex,
                                            int[] slots, String[] attributes, String operation, double perNode,
                                            java.util.function.IntFunction<String> describe) {
        int count = slots.length;
        SkillEffect[][] effects = new SkillEffect[count][];
        String[] descriptions = new String[count];
        for (int i = 0; i < count; i++) {
            SkillEffect[] node = new SkillEffect[attributes.length];
            for (int a = 0; a < attributes.length; a++) node[a] = SkillEffect.attribute(attributes[a], operation, perNode);
            effects[i] = node;
            descriptions[i] = describe.apply(i + 1);
        }
        addLaneNodes(t, key, name, icon, laneIndex, slots, scalingCosts(count), effects, descriptions);
    }

    /**
     * Lays {@code count} node slots in a tidy centred 7-wide block (columns 1–7, rows 1 down),
     * leaving a border around the edges and the bottom button row — much neater than filling from
     * the top-left corner. Handles up to 28 nodes (4 rows).
     */
    private static int[] gridSlots(int count) {
        int[] slots = new int[count];
        for (int i = 0; i < count; i++) {
            int row = i / 7;
            int col = i % 7;
            slots[i] = 10 + row * 9 + col; // start at slot 10 (row 1, col 1)
        }
        return slots;
    }

    /** Placeholder gentle cost ramp (1,1,1,1,2,2,...) — tuned later with the points system. */
    private static int[] scalingCosts(int count) {
        int[] costs = new int[count];
        for (int i = 0; i < count; i++) costs[i] = 1 + i / 4;
        return costs;
    }

    private static String fmt(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    /** Roman numeral for any positive count (lane node titles can exceed VI). */
    private static String roman(int n) {
        if (n <= 0) return String.valueOf(n);
        int[] values = {100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (n >= values[i]) { sb.append(symbols[i]); n -= values[i]; }
        }
        return sb.toString();
    }

    private static SkillEffect[][] flagEffects(String... flags) {
        SkillEffect[][] effects = new SkillEffect[flags.length][];
        for (int i = 0; i < flags.length; i++) effects[i] = new SkillEffect[]{SkillEffect.flag(flags[i])};
        return effects;
    }

    /** A lane with one node per entry, using the given slots/costs/effects (with a prerequisite chain). */
    private static void addLaneNodes(SkillTree t, String key, String name, String icon, int laneIndex,
                                     int[] slots, int[] costs, SkillEffect[][] effectsPerNode, String[] descriptions) {
        t.categories.add(new SkillCategory(key, name, icon, CATEGORY_SLOTS[laneIndex]));
        for (int i = 0; i < slots.length; i++) {
            SkillNode node = new SkillNode(key + "_" + (i + 1), name + " " + roman(i + 1), key, slots[i], costs[i], icon);
            node.description.add(descriptions[i]);
            for (SkillEffect effect : effectsPerNode[i]) node.effects.add(effect);
            if (i > 0) node.requires.add(key + "_" + i);
            t.nodes.add(node);
        }
    }
}
