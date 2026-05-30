package io.github.andrewwwwwwwwwwwwwww.vanillaskills.gui;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.PlayerSkillData;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillNode;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.SkillTree;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only chest GUI for the skill tree. Each node renders as an item; clicking an
 * available, affordable node unlocks it. All item movement is blocked.
 */
public class SkillTreeMenu extends ChestMenu {
    private final ServerPlayer player;
    private final SimpleContainer container;

    public static void open(ServerPlayer player) {
        SkillTree tree = VanillaSkills.TREE.tree();
        Component title = Component.literal(tree.title == null ? "Skills" : tree.title);
        player.openMenu(new SimpleMenuProvider(
                (syncId, inv, p) -> new SkillTreeMenu(syncId, inv, (ServerPlayer) p), title));
    }

    public SkillTreeMenu(int syncId, Inventory inv, ServerPlayer player) {
        this(syncId, inv, player, new SimpleContainer(VanillaSkills.TREE.tree().slotCount()));
    }

    private SkillTreeMenu(int syncId, Inventory inv, ServerPlayer player, SimpleContainer container) {
        super(menuTypeFor(VanillaSkills.TREE.tree().rows), syncId, inv, container, clampRows(VanillaSkills.TREE.tree().rows));
        this.player = player;
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
            container.setItem(slot, node != null ? buildNodeItem(node, data) : ItemStack.EMPTY);
        }
        int counterSlot = size - 1;
        if (tree.bySlot(counterSlot) == null) {
            container.setItem(counterSlot, buildCounter(data));
        }
    }

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
            SkillNode node = VanillaSkills.TREE.tree().bySlot(slotId);
            if (node != null) {
                VanillaSkills.PLAYERS.unlock(sp, node.id);
            }
        }
        // Block all item movement; just resync the client to the authoritative state.
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
