package io.github.andrewwwwwwwwwwwwwww.vanillaskills;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.command.SkillCommands;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.PointsConfig;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.AlloyRecipes;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorCraftingRecipe;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.RoseGoldImmunity;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.loot.FortuneTemplateLoot;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.FortuneTemplateRecipe;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.FortuneUpgradeRecipe;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.PlayerSkillData;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.PlayerSkillManager;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillEffects;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillTree;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillTreeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VanillaSkills implements ModInitializer {
    public static final String MOD_ID = "vanillaskills";
    public static final Logger LOGGER = LoggerFactory.getLogger("VanillaSkills");

    public static MinecraftServer server;
    public static final SkillTreeManager TREE = new SkillTreeManager();
    public static final PlayerSkillManager PLAYERS = new PlayerSkillManager();

    private static final int STATUS_REFRESH_INTERVAL = 40;
    private int tickCounter = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("VanillaSkills initializing");

        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "fortune_upgrade"),
                FortuneUpgradeRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "fortune_template"),
                FortuneTemplateRecipe.SERIALIZER);
        FortuneTemplateLoot.register();

        io.github.andrewwwwwwwwwwwwwww.vanillaskills.creative.VanillaSkillsItemGroup.register();

        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "armor_crafting"),
                ArmorCraftingRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "rose_gold_ingot"),
                AlloyRecipes.RoseGold.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "steel_ingot"),
                AlloyRecipes.Steel.SERIALIZER);

        ServerLifecycleEvents.SERVER_STARTED.register(srv -> {
            server = srv;
            PLAYERS.setPointsConfig(PointsConfig.load());
            TREE.load();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(srv -> PLAYERS.saveAll());

        ServerPlayerEvents.JOIN.register(PLAYERS::onJoin);
        ServerPlayerEvents.LEAVE.register(player -> PLAYERS.unload(player.getUUID()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> PLAYERS.applyAll(newPlayer));

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SkillCommands.register(dispatcher));
    }

    private void onServerTick(MinecraftServer srv) {
        RoseGoldImmunity.tick(srv);
        if (++tickCounter < STATUS_REFRESH_INTERVAL) return;
        tickCounter = 0;
        SkillTree tree = TREE.tree();
        for (ServerPlayer player : srv.getPlayerList().getPlayers()) {
            PlayerSkillData data = PLAYERS.get(player.getUUID());
            SkillEffects.refreshStatusEffects(player, data, tree);
        }
    }
}
