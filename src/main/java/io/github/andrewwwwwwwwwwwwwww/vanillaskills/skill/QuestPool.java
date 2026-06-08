package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import java.util.List;

/** The pool of possible bounty-board quests; 3 are picked each rotation. */
public final class QuestPool {
    private QuestPool() {}

    public static final List<Quest> ALL = List.of(
            // ---- Gather (turned in at the board) ----
            new Quest(Quest.Type.GATHER, "minecraft:iron_ingot", 32, 2, "Gather 32 Iron Ingots"),
            new Quest(Quest.Type.GATHER, "minecraft:gold_ingot", 16, 2, "Gather 16 Gold Ingots"),
            new Quest(Quest.Type.GATHER, "minecraft:diamond", 10, 3, "Gather 10 Diamonds"),
            new Quest(Quest.Type.GATHER, "minecraft:copper_ingot", 64, 1, "Gather 64 Copper Ingots"),
            new Quest(Quest.Type.GATHER, "minecraft:wheat", 64, 1, "Gather 64 Wheat"),
            new Quest(Quest.Type.GATHER, "minecraft:leather", 24, 2, "Gather 24 Leather"),
            new Quest(Quest.Type.GATHER, "minecraft:gunpowder", 16, 2, "Gather 16 Gunpowder"),
            new Quest(Quest.Type.GATHER, "minecraft:redstone", 32, 1, "Gather 32 Redstone"),
            new Quest(Quest.Type.GATHER, "minecraft:emerald", 8, 2, "Gather 8 Emeralds"),
            new Quest(Quest.Type.GATHER, "minecraft:coal", 64, 1, "Gather 64 Coal"),
            new Quest(Quest.Type.GATHER, "minecraft:ender_pearl", 8, 3, "Gather 8 Ender Pearls"),
            new Quest(Quest.Type.GATHER, "minecraft:bone", 32, 1, "Gather 32 Bones"),

            // ---- Kill ----
            new Quest(Quest.Type.KILL, "minecraft:zombie", 25, 1, "Slay 25 Zombies"),
            new Quest(Quest.Type.KILL, "minecraft:skeleton", 25, 1, "Slay 25 Skeletons"),
            new Quest(Quest.Type.KILL, "minecraft:creeper", 15, 2, "Slay 15 Creepers"),
            new Quest(Quest.Type.KILL, "minecraft:spider", 20, 1, "Slay 20 Spiders"),
            new Quest(Quest.Type.KILL, "minecraft:enderman", 10, 3, "Slay 10 Endermen"),
            new Quest(Quest.Type.KILL, "minecraft:blaze", 8, 3, "Slay 8 Blazes"),
            new Quest(Quest.Type.KILL, "minecraft:piglin", 15, 2, "Slay 15 Piglins"),
            new Quest(Quest.Type.KILL, "minecraft:drowned", 15, 2, "Slay 15 Drowned"),
            new Quest(Quest.Type.KILL, "minecraft:witch", 8, 2, "Slay 8 Witches"),
            new Quest(Quest.Type.KILL, Quest.ANY_HOSTILE, 50, 2, "Slay 50 hostile mobs")
    );
}
