package io.github.andrewwwwwwwwwwwwwww.vanillaskills.loot;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.recipe.DragonUpgradeTemplate;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Adds the Dragon Upgrade template to End City treasure chests at ~4% per chest
 * (template weight 4 vs empty weight 96 = 4/100). Injected via the loot API (non-destructive),
 * producing a vanilla netherite template carrying our name + marker, so it stays server-side.
 */
public final class DragonTemplateLoot {
    private DragonTemplateLoot() {}

    private static final int TEMPLATE_WEIGHT = 4;
    private static final int EMPTY_WEIGHT = 96;

    public static void register() {
        LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
            if (key.equals(BuiltInLootTables.END_CITY_TREASURE)) {
                builder.withPool(templatePool());
            }
        });
    }

    private static LootPool.Builder templatePool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0f))
                .add(EmptyLootItem.emptyItem().setWeight(EMPTY_WEIGHT))
                .add(LootItem.lootTableItem(DragonUpgradeTemplate.BASE)
                        .setWeight(TEMPLATE_WEIGHT)
                        .apply(SetComponentsFunction.setComponent(DataComponents.CUSTOM_NAME, DragonUpgradeTemplate.displayName()))
                        .apply(SetComponentsFunction.setComponent(DataComponents.CUSTOM_DATA, CustomData.of(DragonUpgradeTemplate.markerTag())))
                        .apply(SetComponentsFunction.setComponent(DataComponents.CUSTOM_MODEL_DATA, DragonUpgradeTemplate.modelData()))
                        .apply(SetComponentsFunction.setComponent(DataComponents.LORE, DragonUpgradeTemplate.lore())));
    }
}
