package io.github.andrewwwwwwwwwwwwwww.vanillaskills.tool;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.armor.Markers;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
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
        applyAttributes(stack, baseItem, kind);
        return stack;
    }

    // A spear hits a little harder, swings slower, and reaches further than the matching sword.
    private static final double SPEAR_DAMAGE_BONUS = 1.0;
    private static final double SPEAR_SPEED_PENALTY = -0.5;
    private static final double SPEAR_REACH = 1.0;

    /** Copy the base tool's attribute modifiers, boosting attack damage/speed and (spears) reach. */
    private void applyAttributes(ItemStack stack, Item baseItem, ToolKind kind) {
        double damageBonus = attackDamageBonus + (kind == ToolKind.SPEAR ? SPEAR_DAMAGE_BONUS : 0);
        double speedBonus = attackSpeedBonus + (kind == ToolKind.SPEAR ? SPEAR_SPEED_PENALTY : 0);
        double reach = kind == ToolKind.SPEAR ? SPEAR_REACH : 0;
        if (damageBonus == 0 && speedBonus == 0 && reach == 0) return;

        ItemAttributeModifiers base = new ItemStack(baseItem).get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (base == null) base = ItemAttributeModifiers.EMPTY;

        ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : base.modifiers()) {
            AttributeModifier mod = entry.modifier();
            if (damageBonus != 0 && entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                mod = new AttributeModifier(mod.id(), mod.amount() + damageBonus, mod.operation());
            } else if (speedBonus != 0 && entry.attribute().is(Attributes.ATTACK_SPEED)) {
                mod = new AttributeModifier(mod.id(), mod.amount() + speedBonus, mod.operation());
            }
            b.add(entry.attribute(), mod, entry.slot(), entry.display());
        }
        if (reach != 0) {
            b.add(Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(Identifier.fromNamespaceAndPath("vanillaskills", id + ".spear.reach"),
                            reach, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, b.build());
    }
}
