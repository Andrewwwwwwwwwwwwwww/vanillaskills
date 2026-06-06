package io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Markers;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Repairable;

import java.util.List;
import java.util.function.Predicate;

/**
 * One tool tier. Pieces are vanilla tools stamped with overriding components: durability, a name,
 * a marker, a repair list, a model hook, and small attack-damage / attack-speed bonuses over the
 * base tool. Harvest tier and base behaviour come from the base vanilla tool.
 */
public class ToolTier {
    public final String id;
    public final String displayName;
    public final int nameColor;
    public final String markerKey;
    private final Item[] baseItems;   // indexed by ToolKind.ordinal()
    private final int durability;
    private final double attackDamageBonus;
    private final double attackSpeedBonus;
    private final HolderSet<Item> repairItems;
    public final Predicate<ItemStack> material;

    public ToolTier(String id, String displayName, int nameColor, String markerKey,
                    Item[] baseItems, int durability, double attackDamageBonus, double attackSpeedBonus,
                    HolderSet<Item> repairItems, Predicate<ItemStack> material) {
        this.id = id;
        this.displayName = displayName;
        this.nameColor = nameColor;
        this.markerKey = markerKey;
        this.baseItems = baseItems;
        this.durability = durability;
        this.attackDamageBonus = attackDamageBonus;
        this.attackSpeedBonus = attackSpeedBonus;
        this.repairItems = repairItems;
        this.material = material;
    }

    public ItemStack create(ToolKind kind) {
        Item baseItem = baseItems[kind.ordinal()];
        ItemStack stack = new ItemStack(baseItem);
        stack.set(DataComponents.CUSTOM_NAME, Markers.name(displayName + " " + kind.word, nameColor));
        Markers.applyMarker(stack, markerKey);
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(), List.of(), List.of("vanillaskills:" + id + "_" + kind.lower()), List.of()));
        stack.set(DataComponents.MAX_DAMAGE, durability);
        stack.set(DataComponents.REPAIRABLE, new Repairable(repairItems));
        applyAttributes(stack, baseItem);
        return stack;
    }

    /**
     * Copy the base tool's attribute modifiers, boosting attack damage/speed by the tier bonus.
     * Spears now use the real vanilla spear item, which already carries its own reach and slower
     * swing, so no spear-specific tweaks are needed here.
     */
    private void applyAttributes(ItemStack stack, Item baseItem) {
        if (attackDamageBonus == 0 && attackSpeedBonus == 0) return;

        ItemAttributeModifiers base = new ItemStack(baseItem).get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (base == null) base = ItemAttributeModifiers.EMPTY;

        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : base.modifiers()) {
            AttributeModifier mod = entry.modifier();
            if (attackDamageBonus != 0 && entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                mod = new AttributeModifier(mod.id(), mod.amount() + attackDamageBonus, mod.operation());
            } else if (attackSpeedBonus != 0 && entry.attribute().is(Attributes.ATTACK_SPEED)) {
                mod = new AttributeModifier(mod.id(), mod.amount() + attackSpeedBonus, mod.operation());
            }
            b.add(entry.attribute(), mod, entry.slot(), entry.display());
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
    }
}
