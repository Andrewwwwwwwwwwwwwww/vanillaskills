package io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;

/**
 * The custom crafting metals. They are vanilla copper ingots stamped with a name + marker +
 * model hook (so a resource pack can retexture them). Copper is used as the base because it has
 * no vanilla armor recipe, avoiding recipe collisions; the two metals are told apart by marker.
 */
public final class Alloys {
    private Alloys() {}

    public static final String ROSE_GOLD_MARKER = "vs_rose_gold_ingot";
    public static final String STEEL_MARKER = "vs_steel_ingot";

    private static final int ROSE_GOLD_COLOR = 0xE8B7A6;
    private static final int STEEL_COLOR = 0xB8C0C8;

    public static ItemStack roseGoldIngot() {
        return stamp(ROSE_GOLD_MARKER, "Rose Gold Ingot", ROSE_GOLD_COLOR, "vanillaskills:rose_gold_ingot");
    }

    public static boolean isRoseGoldIngot(ItemStack stack) {
        return stack.is(Items.COPPER_INGOT) && Markers.has(stack, ROSE_GOLD_MARKER);
    }

    public static ItemStack steelIngot() {
        return stamp(STEEL_MARKER, "Steel Ingot", STEEL_COLOR, "vanillaskills:steel_ingot");
    }

    public static boolean isSteelIngot(ItemStack stack) {
        return stack.is(Items.COPPER_INGOT) && Markers.has(stack, STEEL_MARKER);
    }

    private static ItemStack stamp(String marker, String name, int color, String modelHook) {
        ItemStack stack = new ItemStack(Items.COPPER_INGOT);
        stack.set(DataComponents.CUSTOM_NAME, Markers.name(name, color));
        Markers.applyMarker(stack, marker);
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(), List.of(), List.of(modelHook), List.of()));
        return stack;
    }
}
