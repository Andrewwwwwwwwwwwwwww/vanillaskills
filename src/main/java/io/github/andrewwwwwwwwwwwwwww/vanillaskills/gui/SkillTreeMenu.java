package io.github.andrewwwwwwwwwwwwwww.vanillaskills.gui;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.PlayerSkillData;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillNode;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillTree;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Chest GUI for the skill tree. In normal mode, clicking an available node unlocks it. In edit
 * mode (ops, via {@code /skill editor}), clicking picks up / moves / swaps nodes and right-click
 * deletes them, writing changes back to skilltree.json. All item movement is blocked either way.
 */
public class SkillTreeMenu extends ChestMenu {
    private final ServerPlayer player;
    private final SimpleContainer container;
    private final boolean editMode;
    private String selected; // node id currently picked up in edit mode

    public static void open(ServerPlayer player) {
        openInternal(player, false);
    }

    public static void openEditor(ServerPlayer player) {
        openInternal(player, true);
    }

    private static void openInternal(ServerPlayer player, boolean editMode) {
        SkillTree tree = VanillaSkills.TREE.tree();
        String base = tree.title == null ? "Skills" : tree.title;
        Component title = Component.literal(editMode ? base + " (Edit Mode)" : base);
        player.openMenu(new SimpleMenuProvider(
                (syncId, inv, p) -> new SkillTreeMenu(syncId, inv, (ServerPlayer) p, editMode), title));
    }

    public SkillTreeMenu(int syncId, Inventory inv, ServerPlayer player, boolean editMode) {
        this(syncId, inv, player, editMode, new SimpleContainer(VanillaSkills.TREE.tree().slotCount()));
    }

    private SkillTreeMenu(int syncId, Inventory inv, ServerPlayer player, boolean editMode, SimpleContainer container) {
        super(menuTypeFor(VanillaSkills.TREE.tree().rows), syncId, inv, container, clampRows(VanillaSkills.TREE.tree().rows));
        this.player = player;
        this.editMode = editMode;
        this.container = container;
        populate();
    }

    private static int clampRows(int rows) {
        return Math.max(1, Math.min(6, rows));
    }

    private static MenuType<ChestMenu> menuTypeFor(int rows) {
        return switch (clampRows(rows)) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    private void populate() {
        SkillTree tree = VanillaSkills.TREE.tree();
        PlayerSkillData data = VanillaSkills.PLAYERS.get(player.getUUID());
        int size = container.getContainerSize();
        for (int slot = 0; slot < size; slot++) {
            SkillNode node = tree.bySlot(slot);
            if (node != null) {
                container.setItem(slot, editMode ? buildEditItem(node) : buildNodeItem(node, data));
            } else {
                container.setItem(slot, ItemStack.EMPTY);
            }
        }
        int counterSlot = size - 1;
        if (tree.bySlot(counterSlot) == null) {
            container.setItem(counterSlot, editMode ? buildEditInfo() : buildCounter(data));
        }
    }

    // ---- normal mode ----

    private ItemStack buildNodeItem(SkillNode node, PlayerSkillData data) {
        boolean unlocked = data.hasUnlocked(node.id);
        boolean prereqMet = node.requires.stream().allMatch(data::hasUnlocked);
        boolean affordable = data.pointsAvailable >= node.cost;

        ItemStack stack = new ItemStack(resolveItem(node.icon));

        ChatFormatting nameColor = unlocked ? ChatFormatting.GREEN
                : !prereqMet ? ChatFormatting.RED
                : affordable ? ChatFormatting.YELLOW
                : ChatFormatting.GRAY;
        stack.set(DataComponents.CUSTOM_NAME, styled(node.title + (unlocked ? " ✔" : ""), nameColor));

        List<Component> lore = new ArrayList<>();
        for (String line : node.description) lore.add(styled(line, ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        if (unlocked) {
            lore.add(styled("Unlocked", ChatFormatting.GREEN));
        } else if (!prereqMet) {
            List<String> missing = new ArrayList<>();
            for (String req : node.requires) {
                if (!data.hasUnlocked(req)) {
                    SkillNode rn = VanillaSkills.TREE.tree().byId(req);
                    missing.add(rn != null ? rn.title : req);
                }
            }
            lore.add(styled("Requires: " + String.join(", ", missing), ChatFormatting.RED));
        } else if (affordable) {
            lore.add(styled("Click to unlock — cost " + node.cost, ChatFormatting.YELLOW));
        } else {
            lore.add(styled("Cost " + node.cost + " (need more points)", ChatFormatting.RED));
        }
        stack.set(DataComponents.LORE, new ItemLore(lore));

        if (unlocked) stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    private ItemStack buildCounter(PlayerSkillData data) {
        ItemStack stack = new ItemStack(Items.EXPERIENCE_BOTTLE);
        stack.set(DataComponents.CUSTOM_NAME, styled("Points: " + data.pointsAvailable, ChatFormatting.AQUA));
        return stack;
    }

    // ---- edit mode ----

    private ItemStack buildEditItem(SkillNode node) {
        boolean isSelected = node.id.equals(selected);
        ItemStack stack = new ItemStack(resolveItem(node.icon));
        stack.set(DataComponents.CUSTOM_NAME,
                styled(node.title + (isSelected ? " (moving)" : ""), isSelected ? ChatFormatting.GOLD : ChatFormatting.AQUA));

        List<Component> lore = new ArrayList<>();
        lore.add(styled("id: " + node.id, ChatFormatting.DARK_GRAY));
        lore.add(styled("slot " + node.slot + "   cost " + node.cost, ChatFormatting.GRAY));
        lore.add(styled("effects: " + node.effects.size() + "   requires: " + node.requires, ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(styled("Left-click: pick up / place / swap", ChatFormatting.YELLOW));
        lore.add(styled("Right-click: delete node", ChatFormatting.RED));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        if (isSelected) stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    private ItemStack buildEditInfo() {
        ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);
        stack.set(DataComponents.CUSTOM_NAME, styled("Edit Mode", ChatFormatting.GOLD));
        List<Component> lore = new ArrayList<>();
        lore.add(styled("Left-click a node to pick it up,", ChatFormatting.GRAY));
        lore.add(styled("then an empty slot to move it,", ChatFormatting.GRAY));
        lore.add(styled("or another node to swap.", ChatFormatting.GRAY));
        lore.add(styled("Right-click a node to delete it.", ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        lore.add(styled("Add nodes / set cost / effects:", ChatFormatting.DARK_GRAY));
        lore.add(styled("use /skill edit ...", ChatFormatting.DARK_GRAY));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        return stack;
    }

    private void handleEditClick(int slotId, int button) {
        SkillTree tree = VanillaSkills.TREE.tree();
        SkillNode atSlot = tree.bySlot(slotId);

        if (button == 1) { // right-click: delete (root is protected)
            if (atSlot != null && !atSlot.id.equals(SkillTree.ROOT_ID)) {
                tree.nodes.remove(atSlot);
                if (atSlot.id.equals(selected)) selected = null;
                VanillaSkills.TREE.touchAndSave();
            }
            return;
        }

        // left-click
        if (selected == null) {
            if (atSlot != null) selected = atSlot.id; // pick up
            return;
        }
        SkillNode sel = tree.byId(selected);
        if (sel == null) {
            selected = null;
            return;
        }
        if (atSlot == null) {
            sel.slot = slotId; // move to empty
            VanillaSkills.TREE.touchAndSave();
            selected = null;
        } else if (atSlot.id.equals(selected)) {
            selected = null; // clicked itself -> cancel
        } else {
            int tmp = sel.slot; // swap
            sel.slot = atSlot.slot;
            atSlot.slot = tmp;
            VanillaSkills.TREE.touchAndSave();
            selected = null;
        }
    }

    // ---- shared ----

    private static MutableComponent styled(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color).withStyle(s -> s.withItalic(false));
    }

    private static Item resolveItem(String iconId) {
        if (iconId == null) return Items.STONE;
        Identifier id = Identifier.tryParse(iconId);
        if (id == null) return Items.STONE;
        return BuiltInRegistries.ITEM.get(id).map(Holder::value).orElse(Items.STONE);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player clicker) {
        if (slotId >= 0 && slotId < container.getContainerSize() && clicker instanceof ServerPlayer sp) {
            if (editMode) {
                handleEditClick(slotId, button);
            } else {
                SkillNode node = VanillaSkills.TREE.tree().bySlot(slotId);
                if (node != null) {
                    VanillaSkills.PLAYERS.unlock(sp, node.id);
                }
            }
        }
        populate();
        sendAllDataToRemote();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
