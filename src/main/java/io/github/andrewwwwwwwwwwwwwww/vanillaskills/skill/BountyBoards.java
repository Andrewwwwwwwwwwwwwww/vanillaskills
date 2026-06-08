package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.mixin.ArmorStandAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Physical bounty boards: an op summons a lectern with floating "Bounty Board" text that anyone can
 * right-click to open the quest GUI. Board positions persist to world/vanillaskills/questboards.json;
 * the floating text is an invisible marker armor stand tagged so it can be cleaned up on removal.
 */
public class BountyBoards {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String TAG = "vanillaskills_board";
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

    public boolean isBoard(ServerLevel level, BlockPos pos) {
        String dim = dimId(level);
        for (Entry e : boards) {
            if (e.dim.equals(dim) && e.x == pos.getX() && e.y == pos.getY() && e.z == pos.getZ()) return true;
        }
        return false;
    }

    public void place(ServerPlayer op) {
        ServerLevel level = (ServerLevel) op.level();
        BlockPos pos;
        HitResult hit = op.pick(6.0, 1.0f, false);
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
            pos = bhr.getBlockPos().relative(bhr.getDirection());
        } else {
            pos = op.blockPosition();
        }
        if (isBoard(level, pos)) {
            op.sendSystemMessage(Component.literal("There's already a board there.").withStyle(ChatFormatting.RED));
            return;
        }

        level.setBlockAndUpdate(pos, Blocks.LECTERN.defaultBlockState());

        ArmorStand stand = EntityType.ARMOR_STAND.create(level, EntitySpawnReason.COMMAND);
        if (stand != null) {
            stand.snapTo(pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 0.0f, 0.0f);
            stand.setInvisible(true);
            stand.setNoGravity(true);
            stand.setInvulnerable(true);
            stand.setCustomName(Component.literal("✦ Bounty Board ✦")
                    .withStyle(s -> s.withColor(0xFFD700).withItalic(false)));
            stand.setCustomNameVisible(true);
            stand.addTag(TAG);
            ((ArmorStandAccessor) stand).vanillaskills$setMarker(true);
            level.addFreshEntity(stand);
        }

        boards.add(new Entry(dimId(level), pos.getX(), pos.getY(), pos.getZ()));
        save();
        op.sendSystemMessage(Component.literal("Bounty board placed — right-click it to open the quests.")
                .withStyle(ChatFormatting.GREEN));
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
        BlockPos bp = new BlockPos(best.x, best.y, best.z);
        if (level.getBlockState(bp).is(Blocks.LECTERN)) {
            level.setBlockAndUpdate(bp, Blocks.AIR.defaultBlockState());
        }
        AABB box = new AABB(bp).inflate(2.0);
        for (ArmorStand s : level.getEntitiesOfClass(ArmorStand.class, box, e -> e.entityTags().contains(TAG))) {
            s.discard();
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
