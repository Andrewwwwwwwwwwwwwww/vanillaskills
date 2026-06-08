package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The shared bounty board: 3 active quests that re-roll on a timer (default every 5 hours). Persisted
 * to world/vanillaskills/questboard.json so it survives restarts.
 */
public class QuestBoard {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final long INTERVAL_MS = 5L * 60 * 60 * 1000; // 5 hours
    private static final int ACTIVE_COUNT = 3;
    private final Random random = new Random();

    private State state = new State();

    /** Serialized board state. */
    private static class State {
        long rotationId = 0;
        long nextRotationMs = 0;
        int[] activeIndices = new int[0];
    }

    public long rotationId() {
        return state.rotationId;
    }

    public long nextRotationMs() {
        return state.nextRotationMs;
    }

    /** The 3 currently-active quests (by their index in the active list). */
    public List<Quest> active() {
        List<Quest> out = new ArrayList<>();
        for (int idx : state.activeIndices) {
            if (idx >= 0 && idx < QuestPool.ALL.size()) out.add(QuestPool.ALL.get(idx));
        }
        return out;
    }

    public Quest active(int i) {
        List<Quest> a = active();
        return (i >= 0 && i < a.size()) ? a.get(i) : null;
    }

    public void load() {
        Path path = path();
        try {
            if (Files.exists(path)) {
                State loaded = GSON.fromJson(Files.readString(path), State.class);
                if (loaded != null) state = loaded;
            }
        } catch (Exception e) {
            VanillaSkills.LOGGER.error("Failed to load questboard.json", e);
        }
        long now = System.currentTimeMillis();
        if (state.activeIndices.length != ACTIVE_COUNT || state.nextRotationMs <= now) {
            reroll(now);
        }
    }

    public void tick(MinecraftServer server) {
        if (System.currentTimeMillis() >= state.nextRotationMs) {
            reroll(System.currentTimeMillis());
            server.getPlayerList().getPlayers().forEach(p -> p.sendSystemMessage(Component.literal(
                    "New bounties are available! Use /quests").withStyle(ChatFormatting.GOLD)));
        }
    }

    private void reroll(long now) {
        state.rotationId++;
        state.nextRotationMs = now + INTERVAL_MS;
        state.activeIndices = pickDistinct(ACTIVE_COUNT);
        save();
    }

    private int[] pickDistinct(int count) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < QuestPool.ALL.size(); i++) idx.add(i);
        java.util.Collections.shuffle(idx, random);
        int[] out = new int[Math.min(count, idx.size())];
        for (int i = 0; i < out.length; i++) out[i] = idx.get(i);
        return out;
    }

    public void save() {
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(state));
        } catch (Exception e) {
            VanillaSkills.LOGGER.error("Failed to save questboard.json", e);
        }
    }

    private static Path path() {
        MinecraftServer server = VanillaSkills.server;
        return server.getWorldPath(LevelResource.ROOT).resolve("vanillaskills").resolve("questboard.json");
    }
}
