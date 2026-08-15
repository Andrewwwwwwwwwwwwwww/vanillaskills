package io.github.andrewwwwwwwwwwwwwww.vanillaskills.data;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Feat;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;

/**
 * All datapack-loaded VanillaSkills content, refreshed on every datapack reload (server start and
 * {@code /reload}).
 *
 * <p>The mod ships its own defaults as a datapack inside its jar, so there is exactly one code path:
 * built-in content and pack content load the same way, and a pack can override either.
 *
 * <p>Fields are {@code volatile} and swapped wholesale for a fresh immutable list, so gameplay code
 * reading them on the server thread never sees a half-built collection.
 *
 * <p>⚠ The reload listener runs during the initial datapack load, which happens <b>before</b>
 * {@code SERVER_STARTED} assigns {@link VanillaSkills#server} — so nothing here may touch world state
 * or {@link VanillaSkills#worldDir()}.
 */
public final class VsContent {
    private VsContent() {}

    private static volatile List<Feat> feats = List.of();
    private static volatile List<CrateDef> crates = List.of();

    /** Re-read every content type from the datapacks. */
    public static void reload(ResourceManager manager) {
        feats = VsJsonLoader.load(manager, "feat", FeatDef.class).stream()
                .map(FeatDef::toFeat)
                .toList();
        crates = VsJsonLoader.load(manager, "crate", CrateDef.class);

        VanillaSkills.LOGGER.info("Datapack content loaded: {} feat(s), {} crate(s)",
                feats.size(), crates.size());
    }

    /** Every loaded feat, in declaration order. Never null; empty before the first reload. */
    public static List<Feat> feats() {
        return feats;
    }

    /** Every loaded crate, in declaration order. Never null; empty before the first reload. */
    public static List<CrateDef> crates() {
        return crates;
    }

    /** The crate with this id, or null. */
    public static CrateDef crate(String id) {
        for (CrateDef def : crates) {
            if (def.id.equals(id)) return def;
        }
        return null;
    }
}
