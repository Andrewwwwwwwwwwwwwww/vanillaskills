package io.github.andrewwwwwwwwwwwwwww.vanillaskills.client;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * The Mod Menu config screen. Currently just the gameplay toggles (Mending). Edits the local
 * {@code config/vanillaskills/gameplay.json} and applies it; in singleplayer that takes effect
 * immediately, since the client and integrated server share the live flag. On a multiplayer server
 * the server's own config is authoritative — this only changes your local copy.
 */
public class VanillaSkillsConfigScreen extends Screen {
    private final Screen parent;
    private boolean mending;

    public VanillaSkillsConfigScreen(Screen parent) {
        super(Component.literal("VanillaSkills"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.mending = GameplayConfig.MENDING_ENABLED;
        int cx = this.width / 2;
        int y = this.height / 4 + 24;

        addRenderableWidget(Button.builder(mendingLabel(), b -> {
            mending = !mending;
            b.setMessage(mendingLabel());
        }).bounds(cx - 120, y, 240, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(cx - 120, y + 28, 240, 20).build());
    }

    private Component mendingLabel() {
        return Component.literal("Mending: " + (mending ? "Enabled" : "Removed (default)"));
    }

    @Override
    public void onClose() {
        // Persist to gameplay.json and publish the live flag (shared with the integrated server in SP).
        GameplayConfig cfg = GameplayConfig.load();
        cfg.mendingEnabled = mending;
        cfg.save();
        GameplayConfig.MENDING_ENABLED = mending;
        this.minecraft.setScreenAndShow(parent);
    }
}
