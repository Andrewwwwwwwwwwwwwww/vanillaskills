package io.github.andrewwwwwwwwwwwwwww.vanillaskills;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.command.SkillCommands;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.PointsConfig;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.AlloyRecipes;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.ArmorCraftingRecipe;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonScale;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonSet;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.RoseGoldSet;
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
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
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

    private static final int ROSE_GOLD_INTERVAL = 10;
    private static final int STATUS_REFRESH_INTERVAL = 40;
    private static final int DRAGON_SCALE_DROP = 8;
    private static final int ELYTRA_FORGE_INTERVAL = 20; // scan items on anvils/grindstones once a second

    // Data-driven recipes granted on join so they appear in the vanilla recipe book.
    private static final java.util.List<net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>>> BOOK_RECIPES =
            java.util.List.of(recipeKey("rose_gold_ingot"), recipeKey("steel_ingot"), recipeKey("crystallized_diamond"));

    private static net.minecraft.resources.ResourceKey<net.minecraft.world.item.crafting.Recipe<?>> recipeKey(String path) {
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, path));
    }
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
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "tool_crafting"),
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolCraftingRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "steel_shield"),
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.shield.ShieldInfuseRecipe.SERIALIZER);
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
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "crystallized_diamond"),
                AlloyRecipes.Crystal.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "dragon_ingot"),
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonIngotRecipe.SERIALIZER);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(MOD_ID, "dragon_template_dup"),
                io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.DragonTemplateRecipe.SERIALIZER);
        io.github.andrewwwwwwwwwwwwwww.vanillaskills.loot.DragonTemplateLoot.register();

        ServerLifecycleEvents.SERVER_STARTED.register(srv -> {
            server = srv;
            PLAYERS.setPointsConfig(PointsConfig.load());
            TREE.load();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(srv -> PLAYERS.saveAllAndClear());

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof EnderDragon && entity.level() instanceof ServerLevel level) {
                ItemStack scales = DragonScale.create();
                scales.setCount(DRAGON_SCALE_DROP);
                ItemEntity drop = new ItemEntity(level, entity.getX(), entity.getY() + 1.0, entity.getZ(), scales);
                level.addFreshEntity(drop);
            }
        });

        // Cultivator skill: chance for bonus crops when harvesting a mature crop.
        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerLevel level) || !(player instanceof ServerPlayer sp)) return;
            net.minecraft.world.item.Item product =
                    io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Farming.matureCropProduct(state);
            if (product == null) return;
            int farmLevel = io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.CraftingGate.farmingLevel(sp);
            if (farmLevel <= 0) return;
            if (sp.getRandom().nextFloat() < 0.2f * farmLevel) {
                net.minecraft.world.level.block.Block.popResource(level, pos,
                        new ItemStack(product, 1 + sp.getRandom().nextInt(2)));
            }
        });

        // Hardwood swords & axes inflict a little poison on hit.
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
            if (blocked || damageTaken <= 0.0f) return;
            if (!(source.getEntity() instanceof ServerPlayer attacker)) return;
            ItemStack weapon = attacker.getMainHandItem();
            if (!io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Markers.has(weapon,
                    io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool.ToolTiers.HARDWOOD.markerKey)) return;
            if (!(weapon.is(net.minecraft.world.item.Items.STONE_SWORD)
                    || weapon.is(net.minecraft.world.item.Items.STONE_AXE))) return;
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 40, 0)); // Poison I, 2s
        });

        ServerPlayerEvents.JOIN.register(player -> {
            PLAYERS.onJoin(player);
            player.awardRecipesByKey(BOOK_RECIPES); // show our data recipes in the recipe book
        });
        ServerPlayerEvents.LEAVE.register(player -> {
            PLAYERS.unload(player.getUUID());
            DragonSet.onPlayerLeave(player.getUUID());
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> PLAYERS.applyAll(newPlayer));

        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SkillCommands.register(dispatcher));
    }

    private void onServerTick(MinecraftServer srv) {
        tickCounter++;
        DragonSet.tick(srv);
        if (tickCounter % ELYTRA_FORGE_INTERVAL == 0) {
            io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.DragonElytraForge.tick(srv);
        }
        if (tickCounter % ROSE_GOLD_INTERVAL == 0) {
            RoseGoldSet.tick(srv);
        }
        if (tickCounter % STATUS_REFRESH_INTERVAL == 0) {
            SkillTree tree = TREE.tree();
            for (ServerPlayer player : srv.getPlayerList().getPlayers()) {
                PlayerSkillData data = PLAYERS.get(player.getUUID());
                SkillEffects.refreshStatusEffects(player, data, tree);
            }
        }
    }
}
