package io.github.andrewwwwwwwwwwwwwww.vanillaskills.gui;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.PlayerSkillData;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillCategory;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillNode;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillTree;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
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
 * The skill GUI. With {@code category == null} it's the lane-select screen (one icon per lane);
 * with a category set it shows that lane's nodes. Edit mode lets ops manage lanes and nodes.
 */
public class SkillTreeMenu extends ChestMenu {
    private static final int POINTS_SLOT = 48;
    private static final int STATS_SLOT = 50;
    private static final int BACK_SLOT = 45;

    private final ServerPlayer player;
    private final SimpleContainer container;
    private final boolean editMode;
    private final String category;   // null = lane-select view
    private String selected;         // node id picked up in edit mode

    public static void open(ServerPlayer player) {
        openInternal(player, false, null);
    }

    public static void openEditor(ServerPlayer player) {
        openInternal(player, true, null);
    }

    public static void openCategory(ServerPlayer player, String categoryId, boolean editMode) {
        openInternal(player, editMode, categoryId);
    }

    private static void openInternal(ServerPlayer player, boolean editMode, String category) {
        SkillTree tree = VanillaSkills.TREE.tree();
        String base;
        if (category != null) {
            SkillCategory cat = tree.category(category);
            base = cat != null ? cat.title : "Skills";
        } else {
            base = tree.title == null ? "Skills" : tree.title;
        }
        Component title = Component.literal(editMode ? base + " (Edit Mode)" : base);
        player.openMenu(new SimpleMenuProvider(
                (syncId, inv, p) -> new SkillTreeMenu(syncId, inv, (ServerPlayer) p, editMode, category), title));
    }

    public SkillTreeMenu(int syncId, Inventory inv, ServerPlayer player, boolean editMode, String category) {
        super(menuTypeFor(VanillaSkills.TREE.tree().rows), syncId, inv,
                new SimpleContainer(VanillaSkills.TREE.tree().slotCount()), clampRows(VanillaSkills.TREE.tree().rows));
        this.player = player;
        this.editMode = editMode;
        this.category = category;
        this.container = (SimpleContainer) getContainer();
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
        for (int i = 0; i < size; i++) container.setItem(i, ItemStack.EMPTY);

        if (category == null) {
            for (SkillCategory cat : tree.categories()) {
                container.setItem(cat.slot, buildCategoryItem(cat, data));
            }
            if (editMode) {
                container.setItem(size - 1, buildEditInfo(true));
            } else {
                container.setItem(POINTS_SLOT, buildCounter(data));
                container.setItem(STATS_SLOT, buildStatsHead());
            }
        } else {
            for (SkillNode node : tree.nodesIn(category)) {
                container.setItem(node.slot, editMode ? buildEditItem(node) : buildNodeItem(node, data));
            }
            container.setItem(BACK_SLOT, backButton());
            if (editMode) {
                container.setItem(size - 1, buildEditInfo(false));
            } else {
                container.setItem(POINTS_SLOT, buildCounter(data));
                container.setItem(STATS_SLOT, buildStatsHead());
            }
        }
    }

    // ---- lane select ----

    private ItemStack buildCategoryItem(SkillCategory cat, PlayerSkillData data) {
        SkillTree tree = VanillaSkills.TREE.tree();
        int total = 0, unlocked = 0;
        for (SkillNode node : tree.nodesIn(cat.id)) {
            total++;
            if (data.hasUnlocked(node.id)) unlocked++;
        }
        ItemStack stack = new ItemStack(resolveItem(cat.icon));
        Guis.hideStats(stack);
        // A locked lane (e.g. Night Vision) stays sealed until its earned-Shard gate is met — and we
        // deliberately don't reveal the requirement, so players can't bee-line to it.
        if (!editMode && isLaneLocked(cat, data)) {
            stack.set(DataComponents.CUSTOM_NAME, styled(cat.title, ChatFormatting.DARK_GRAY));
            stack.set(DataComponents.LORE, new ItemLore(List.of(styled("🔒 Locked", ChatFormatting.RED))));
            return stack;
        }
        stack.set(DataComponents.CUSTOM_NAME, styled(cat.title, ChatFormatting.AQUA));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                styled(unlocked + "/" + total + " unlocked", ChatFormatting.GRAY),
                styled(editMode ? "Click to edit this lane" : "Click to open", ChatFormatting.YELLOW))));
        if (total > 0 && unlocked == total) stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    /** True if any node in this lane is still gated behind an earned-Shard requirement the player hasn't met. */
    private static boolean isLaneLocked(SkillCategory cat, PlayerSkillData data) {
        for (SkillNode node : VanillaSkills.TREE.tree().nodesIn(cat.id)) {
            if (node.minEarned > 0 && data.pointsEarned < node.minEarned) return true;
        }
        return false;
    }

    // ---- lane view (nodes) ----

    private ItemStack buildNodeItem(SkillNode node, PlayerSkillData data) {
        boolean unlocked = data.hasUnlocked(node.id);
        boolean prereqMet = node.requires.stream().allMatch(data::hasUnlocked);
        boolean affordable = data.pointsAvailable >= node.cost;

        boolean gated = node.minEarned > 0 && data.pointsEarned < node.minEarned;
        int chain = VanillaSkills.PLAYERS.chainCost(player, node.id); // total a left-click would charge
        boolean affordableChain = data.pointsAvailable >= chain;

        ItemStack stack = new ItemStack(resolveItem(node.icon));
        Guis.hideStats(stack);
        ChatFormatting nameColor = unlocked ? ChatFormatting.GREEN
                : gated ? ChatFormatting.DARK_GRAY
                : !prereqMet ? ChatFormatting.GRAY
                : affordable ? ChatFormatting.YELLOW : ChatFormatting.RED;
        stack.set(DataComponents.CUSTOM_NAME, styled(node.title + (unlocked ? " ✔" : ""), nameColor));

        List<Component> lore = new ArrayList<>();
        for (String line : node.description) lore.add(styled(line, ChatFormatting.GRAY));
        lore.add(Component.literal(""));
        if (unlocked) {
            lore.add(styled("Unlocked", ChatFormatting.GREEN));
            lore.add(styled("Right-click: refund (+ skills above it)", ChatFormatting.DARK_GRAY));
        } else if (gated) {
            lore.add(styled("🔒 Locked", ChatFormatting.RED)); // requirement intentionally hidden
        } else {
            lore.add(styled("Cost: " + chain, affordableChain ? ChatFormatting.YELLOW : ChatFormatting.RED));
            if (!prereqMet) lore.add(styled("Buys this + the skills below it", ChatFormatting.DARK_GRAY));
            else lore.add(styled("Left-click to unlock", ChatFormatting.DARK_GRAY));
            if (!affordableChain) lore.add(styled("Not enough Skill Shards", ChatFormatting.RED));
        }
        stack.set(DataComponents.LORE, new ItemLore(lore));
        if (unlocked) stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    private ItemStack buildEditItem(SkillNode node) {
        boolean isSelected = node.id.equals(selected);
        ItemStack stack = new ItemStack(resolveItem(node.icon));
        Guis.hideStats(stack);
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

    private ItemStack buildCounter(PlayerSkillData data) {
        ItemStack stack = new ItemStack(Items.EXPERIENCE_BOTTLE);
        stack.set(DataComponents.CUSTOM_NAME, styled("Skill Shards: " + data.pointsAvailable, ChatFormatting.AQUA));
        stack.set(DataComponents.LORE, new ItemLore(List.of(styled("Click to see how to earn Skill Shards", ChatFormatting.GRAY))));
        return stack;
    }

    private ItemStack buildStatsHead() {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.set(DataComponents.CUSTOM_NAME, styled("Your Stats", ChatFormatting.AQUA));
        stack.set(DataComponents.LORE, new ItemLore(List.of(styled("Click to view your current stats", ChatFormatting.GRAY))));
        return stack;
    }

    private ItemStack backButton() {
        ItemStack stack = new ItemStack(Items.ARROW);
        stack.set(DataComponents.CUSTOM_NAME, styled("Back to Lanes", ChatFormatting.YELLOW));
        return stack;
    }

    private ItemStack buildEditInfo(boolean laneSelect) {
        ItemStack stack = new ItemStack(Items.WRITABLE_BOOK);
        stack.set(DataComponents.CUSTOM_NAME, styled("Edit Mode", ChatFormatting.GOLD));
        List<Component> lore = new ArrayList<>();
        if (laneSelect) {
            lore.add(styled("Left-click a lane to edit its skills.", ChatFormatting.GRAY));
            lore.add(styled("Left-click a blank slot to add a lane.", ChatFormatting.GRAY));
            lore.add(styled("Manage lanes: /skill edit category ...", ChatFormatting.DARK_GRAY));
        } else {
            lore.add(styled("Left-click a node to pick up / move / swap.", ChatFormatting.GRAY));
            lore.add(styled("Right-click a node to delete it.", ChatFormatting.GRAY));
            lore.add(styled("Left-click a blank slot to add a skill here.", ChatFormatting.GRAY));
            lore.add(styled("Set cost/effects: /skill edit ...", ChatFormatting.DARK_GRAY));
        }
        stack.set(DataComponents.LORE, new ItemLore(lore));
        return stack;
    }

    // ---- edit handling (within a lane) ----

    private void handleNodeEdit(int slotId, int button) {
        SkillTree tree = VanillaSkills.TREE.tree();
        SkillNode atSlot = tree.nodeInCategoryAtSlot(category, slotId);

        if (button == 1) {
            if (atSlot != null && !atSlot.id.equals(SkillTree.ROOT_ID)) {
                tree.nodes.remove(atSlot);
                if (atSlot.id.equals(selected)) selected = null;
                VanillaSkills.TREE.touchAndSave();
            }
            return;
        }
        if (selected == null) {
            if (atSlot != null) selected = atSlot.id;
            else promptAddSkill(slotId);
            return;
        }
        SkillNode sel = tree.byId(selected);
        if (sel == null) {
            selected = null;
            return;
        }
        if (atSlot == null) {
            sel.slot = slotId;
            sel.category = category;
            VanillaSkills.TREE.touchAndSave();
            selected = null;
        } else if (atSlot.id.equals(selected)) {
            selected = null;
        } else {
            int tmp = sel.slot;
            sel.slot = atSlot.slot;
            atSlot.slot = tmp;
            VanillaSkills.TREE.touchAndSave();
            selected = null;
        }
    }

    private void promptAddSkill(int slotId) {
        String command = "/skill edit add newskill " + category + " " + slotId + " 1 minecraft:stone";
        suggest("Click to add a skill to this lane at slot " + slotId, command);
    }

    private void promptAddCategory(int slotId) {
        String command = "/skill edit category add newlane " + slotId + " minecraft:book";
        suggest("Click to add a lane at slot " + slotId, command);
    }

    private void suggest(String text, String command) {
        player.sendSystemMessage(Component.literal(text + " (then edit the id/icon)")
                .withStyle(s -> s.withColor(0x55FF55).withItalic(false).withClickEvent(new ClickEvent.SuggestCommand(command))));
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
            if (category == null) {
                if (handleLaneSelectClick(sp, slotId)) return;
            } else {
                if (handleLaneViewClick(sp, slotId, button)) return;
            }
        }
        populate();
        sendAllDataToRemote();
    }

    /** @return true if a sub-screen was opened (this menu is being replaced). */
    private boolean handleLaneSelectClick(ServerPlayer sp, int slotId) {
        if (!editMode && slotId == POINTS_SLOT) {
            PointsScreen.open(sp);
            return true;
        }
        if (!editMode && slotId == STATS_SLOT) {
            StatsScreen.open(sp);
            return true;
        }
        SkillCategory cat = VanillaSkills.TREE.tree().categoryAtSlot(slotId);
        if (cat != null) {
            if (!editMode && isLaneLocked(cat, VanillaSkills.PLAYERS.get(sp.getUUID()))) {
                sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("🔒 That path is still locked.")
                        .withStyle(ChatFormatting.RED));
                return false;
            }
            openCategory(sp, cat.id, editMode);
            return true;
        }
        if (editMode) promptAddCategory(slotId);
        return false;
    }

    private boolean handleLaneViewClick(ServerPlayer sp, int slotId, int button) {
        if (slotId == BACK_SLOT) {
            openInternal(sp, editMode, null);
            return true;
        }
        if (!editMode && slotId == POINTS_SLOT) {
            PointsScreen.open(sp);
            return true;
        }
        if (!editMode && slotId == STATS_SLOT) {
            StatsScreen.open(sp);
            return true;
        }
        if (editMode) {
            handleNodeEdit(slotId, button);
        } else {
            SkillNode node = VanillaSkills.TREE.tree().nodeInCategoryAtSlot(category, slotId);
            if (node != null) {
                if (button == 1) {
                    VanillaSkills.PLAYERS.refundChain(sp, node.id);   // right-click: refund this + dependents
                } else {
                    VanillaSkills.PLAYERS.unlockChain(sp, node.id);   // left-click: buy up to this node
                }
            }
        }
        return false;
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
