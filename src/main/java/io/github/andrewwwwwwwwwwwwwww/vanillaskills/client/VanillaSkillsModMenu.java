package io.github.andrewwwwwwwwwwwwwww.vanillaskills.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Mod Menu integration: adds a Config button to the VanillaSkills entry in the Mods list. Only loaded
 * when Mod Menu is installed (Mod Menu reads the "modmenu" entrypoint), so it's a no-op otherwise.
 */
public class VanillaSkillsModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return VanillaSkillsConfigScreen::new;
    }
}
