package io.github.andrewwwwwwwwwwwwwww.vanillaskills;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaSkills implements ModInitializer {
    public static final String MOD_ID = "vanillaskills";
    public static final Logger LOGGER = LoggerFactory.getLogger("VanillaSkills");

    @Override
    public void onInitialize() {
        LOGGER.info("VanillaSkills initializing");
        // Mending removal is handled entirely by ItemEnchantmentsMutableMixin.
        // Skill tree, item tiers, and other systems will be registered here later.
    }
}
