package io.github.andrewwwwwwwwwwwwwww.vanillaskills.client;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The Mod Menu config screen: the gameplay/pacing settings from gameplay.json, as click-to-cycle buttons
 * (widget-only — 26.2 reworked GUI rendering, so no manual draw calls). Saves on close and applies the
 * values; in singleplayer that's live since the client and integrated server share the JVM. On a
 * multiplayer server the server's own gameplay.json is authoritative — this edits your local copy only.
 */
public class VanillaSkillsConfigScreen extends Screen {
    private static final int[] BOUNTY_HOURS = {1, 2, 3, 5, 8, 12, 24};
    private static final int[] SHOP_HOURS = {6, 12, 24, 48, 72};
    private static final int[] RATIOS = {1, 2, 3, 4, 5};
    private static final int[] GRADUATE = {5, 10, 15, 20, 25};

    private final Screen parent;
    private GameplayConfig cfg;

    public VanillaSkillsConfigScreen(Screen parent) {
        super(Component.literal("VanillaSkills"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 280, x = this.width / 2 - w / 2, y = this.height / 4, gap = 24;
        if (this.minecraft.getSingleplayerServer() == null) {
            // Settings are per-world; with no single-player world loaded there's nothing to edit here.
            // (On a multiplayer server the server's own config is authoritative.)
            Button notice = Button.builder(Component.literal("Open a single-player world to edit its settings"),
                    b -> {}).bounds(x, y, w, 20).build();
            notice.active = false;
            addRenderableWidget(notice);
            addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(x, y + gap, w, 20).build());
            return;
        }
        if (cfg == null) cfg = GameplayConfig.load();

        addRenderableWidget(Button.builder(mendingLabel(), b -> {
            cfg.mendingEnabled = !cfg.mendingEnabled;
            b.setMessage(mendingLabel());
        }).bounds(x, y, w, 20).build());

        addRenderableWidget(Button.builder(bountyLabel(), b -> {
            cfg.bountyRefreshHours = cycle(BOUNTY_HOURS, cfg.bountyRefreshHours);
            b.setMessage(bountyLabel());
        }).bounds(x, y + gap, w, 20).build());

        addRenderableWidget(Button.builder(shopLabel(), b -> {
            cfg.shopRefreshHours = cycle(SHOP_HOURS, cfg.shopRefreshHours);
            b.setMessage(shopLabel());
        }).bounds(x, y + gap * 2, w, 20).build());

        addRenderableWidget(Button.builder(ratioLabel(), b -> {
            cfg.convertRatio = cycle(RATIOS, cfg.convertRatio);
            b.setMessage(ratioLabel());
        }).bounds(x, y + gap * 3, w, 20).build());

        addRenderableWidget(Button.builder(graduateLabel(), b -> {
            cfg.graduateAt = cycle(GRADUATE, cfg.graduateAt);
            b.setMessage(graduateLabel());
        }).bounds(x, y + gap * 4, w, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(x, y + gap * 5 + 10, w, 20).build());
    }

    /** Advance to the next preset after {@code current}; if current isn't a preset, snap to the first. */
    private static int cycle(int[] presets, int current) {
        for (int i = 0; i < presets.length; i++) {
            if (presets[i] == current) return presets[(i + 1) % presets.length];
        }
        return presets[0];
    }

    private Component mendingLabel() { return Component.literal("Mending: " + (cfg.mendingEnabled ? "Enabled" : "Removed (default)")); }
    private Component bountyLabel() { return Component.literal("Bounty board refresh: " + cfg.bountyRefreshHours + "h"); }
    private Component shopLabel() { return Component.literal("Quest Shop refresh: " + cfg.shopRefreshHours + "h"); }
    private Component ratioLabel() { return Component.literal("Convert: " + cfg.convertRatio + " Quest → 1 Skill Shard"); }
    private Component graduateLabel() { return Component.literal("Graduate after: " + cfg.graduateAt + " quests"); }

    @Override
    public void onClose() {
        if (cfg != null) {
            cfg.save();
            GameplayConfig.load(); // re-read + apply all live values
        }
        this.minecraft.setScreenAndShow(parent);
    }
}
