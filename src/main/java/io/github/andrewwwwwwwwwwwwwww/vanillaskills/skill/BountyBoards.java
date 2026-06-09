package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Physical bounty boards rendered as holograms-style floating text. An op summons a board with
 * {@code /quests board}; it spawns a native text-display entity plus an overlapping invisible
 * {@link Interaction} entity that anyone can right-click to open the quest GUI. Board anchors persist
 * to {@code world/vanillaskills/questboards.json}; the entities are tagged so they can be cleaned up.
 */
public class BountyBoards {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String TAG = "vanillaskills_board";
    private static final double REMOVE_RANGE_SQR = 36.0; // 6 blocks

    private List<Entry> boards = new ArrayList<>();

    private static class Entry {
        String dim;
        int x, y, z;
        Entry() {}
        Entry(String dim, int x, int y, int z) { this.dim = dim; this.x = x; this.y = y; this.z = z; }
    }

    private static String dimId(ServerLevel level) {
        return level.dimension().identifier().toString();
    }

    public void place(ServerPlayer op) {
        ServerLevel level = (ServerLevel) op.level();
        BlockPos base;
        HitResult hit = op.pick(6.0, 1.0f, false);
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
            base = bhr.getBlockPos();
        } else {
            base = op.blockPosition();
        }
        for (Entry e : boards) {
            if (e.dim.equals(dimId(level)) && e.x == base.getX() && e.y == base.getY() && e.z == base.getZ()) {
                op.sendSystemMessage(Component.literal("There's already a board there.").withStyle(ChatFormatting.RED));
                return;
            }
        }

        double ax = base.getX() + 0.5, ay = base.getY() + 1.3, az = base.getZ() + 0.5;
        spawnEntities(level, ax, ay, az);

        boards.add(new Entry(dimId(level), base.getX(), base.getY(), base.getZ()));
        save();
        op.sendSystemMessage(Component.literal("Bounty board placed — right-click the floating text to open the quests.")
                .withStyle(ChatFormatting.GREEN));
    }

    private static void spawnEntities(ServerLevel level, double ax, double ay, double az) {
        Display.TextDisplay text = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
        Component label = Component.literal("✦ Bounty Board ✦")
                .withStyle(s -> s.withColor(0xFFD700).withItalic(false))
                .append(Component.literal("\n"))
                .append(Component.literal("Right-click to view bounties")
                        .withStyle(s -> s.withColor(0xAAAAAA).withItalic(false)));
        text.setText(label);
        text.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        configure(text);
        text.snapTo(ax, ay, az, 0.0f, 0.0f);
        level.addFreshEntity(text);

        Interaction interaction = new Interaction(EntityType.INTERACTION, level);
        interaction.setWidth(1.6f);
        interaction.setHeight(1.0f);
        interaction.setResponse(true);
        configure(interaction);
        interaction.snapTo(ax, ay - 0.5, az, 0.0f, 0.0f); // anchor is bottom centre
        level.addFreshEntity(interaction);
    }

    private static void configure(Entity e) {
        e.setNoGravity(true);
        e.setInvulnerable(true);
        e.addTag(TAG);
    }

    public void removeNear(ServerPlayer op) {
        ServerLevel level = (ServerLevel) op.level();
        String dim = dimId(level);
        BlockPos p = op.blockPosition();
        Entry best = null;
        double bestD = Double.MAX_VALUE;
        for (Entry e : boards) {
            if (!e.dim.equals(dim)) continue;
            double dx = e.x - p.getX(), dy = e.y - p.getY(), dz = e.z - p.getZ();
            double d = dx * dx + dy * dy + dz * dz;
            if (d <= REMOVE_RANGE_SQR && d < bestD) { best = e; bestD = d; }
        }
        if (best == null) {
            op.sendSystemMessage(Component.literal("No bounty board within 6 blocks.").withStyle(ChatFormatting.RED));
            return;
        }
        AABB box = new AABB(new BlockPos(best.x, best.y, best.z)).inflate(2.0);
        for (Entity e : level.getEntitiesOfClass(Entity.class, box, en -> en.entityTags().contains(TAG))) {
            e.discard();
        }
        boards.remove(best);
        save();
        op.sendSystemMessage(Component.literal("Bounty board removed.").withStyle(ChatFormatting.GREEN));
    }

    // ---- persistence ----

    public void load() {
        Path path = path();
        try {
            if (Files.exists(path)) {
                Entry[] loaded = GSON.fromJson(Files.readString(path), Entry[].class);
                boards = loaded != null ? new ArrayList<>(List.of(loaded)) : new ArrayList<>();
            }
        } catch (Exception e) {
            VanillaSkills.LOGGER.error("Failed to load questboards.json", e);
        }
    }

    public void save() {
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(boards));
        } catch (Exception e) {
            VanillaSkills.LOGGER.error("Failed to save questboards.json", e);
        }
    }

    private static Path path() {
        MinecraftServer server = VanillaSkills.server;
        return server.getWorldPath(LevelResource.ROOT).resolve("vanillaskills").resolve("questboards.json");
    }
}
