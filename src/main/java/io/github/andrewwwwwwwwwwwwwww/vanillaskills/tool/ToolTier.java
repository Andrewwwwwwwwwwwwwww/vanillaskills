package io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Markers;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.Repairable;

import java.util.List;
import java.util.function.Predicate;

/**
 * One tool tier. Pieces are vanilla tools stamped with overriding components: durability, a name,
 * a marker, a repair list, and a model hook. Base attack/mining behaviour is inherited from the
 * base vanilla tool (so the harvest tier is sensible); the tier's identity is its durability,
 * name, repair material, and (later) combat theme.
 */
public class ToolTier {
    public final String id;
    public final String displayName;
    public final int nameColor;
    public final String markerKey;
    private final Item[] baseItems;   // indexed by ToolKind.ordinal()
    private final int durability;
    private final HolderSet<Item> repairItems;
    public final Predicate<ItemStack> material;

    public ToolTier(String id, String displayName, int nameColor, String markerKey,
                    Item[] baseItems, int durability, HolderSet<Item> repairItems,
                    Predicate<ItemStack> material) {
        this.id = id;
        this.displayName = displayName;
        this.nameColor = nameColor;
        this.markerKey = markerKey;
        this.baseItems = baseItems;
        this.durability = durability;
        this.repairItems = repairItems;
        this.material = material;
    }

    public ItemStack create(ToolKind kind) {
        ItemStack stack = new ItemStack(baseItems[kind.ordinal()]);
        stack.set(DataComponents.CUSTOM_NAME, Markers.name(displayName + " " + kind.word, nameColor));
        Markers.applyMarker(stack, markerKey);
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(), List.of(), List.of("vanillaskills:" + id + "_" + kind.lower()), List.of()));
        stack.set(DataComponents.MAX_DAMAGE, durability);
        stack.set(DataComponents.REPAIRABLE, new Repairable(repairItems));
        return stack;
    }
}
