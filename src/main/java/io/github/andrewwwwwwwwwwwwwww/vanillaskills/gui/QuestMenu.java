package io.github.andrewwwwwwwwwwwwwww.vanillaskills.gui;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Quest;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill.Quests;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

/** The bounty board: shows the 3 active quests, the player's progress, and lets them claim. */
public class QuestMenu extends ChestMenu {
    private static final int[] SLOTS = {11, 13, 15};
    private static final int INFO_SLOT = 4;

    private final ServerPlayer player;
    private final SimpleContainer container;

    public static void open(ServerPlayer player) {
        Quests.sync(player);
        player.openMenu(new SimpleMenuProvider(
                (syncId, inv, p) -> new QuestMenu(syncId, inv, (ServerPlayer) p),
                Component.literal("Bounty Board")));
    }

    private QuestMenu(int syncId, Inventory inv, ServerPlayer player) {
        super(MenuType.GENERIC_9x3, syncId, inv, new SimpleContainer(27), 3);
        this.player = player;
        this.container = (SimpleContainer) getContainer();
        populate();
    }

    private void populate() {
        for (int i = 0; i < container.getContainerSize(); i++) container.setItem(i, ItemStack.EMPTY);
        container.setItem(INFO_SLOT, infoItem());
        List<Quest> active = VanillaSkills.QUESTS.active();
        for (int i = 0; i < active.size() && i < SLOTS.length; i++) {
            container.setItem(SLOTS[i], questItem(i, active.get(i)));
        }
    }

    private ItemStack infoItem() {
        long remaining = VanillaSkills.QUESTS.nextRotationMs() - System.currentTimeMillis();
        String when = remaining <= 0 ? "any moment" : (remaining / 3_600_000) + "h " + (remaining % 3_600_000 / 60_000) + "m";
        ItemStack stack = new ItemStack(Items.CLOCK);
        stack.set(DataComponents.CUSTOM_NAME, styled("Bounty Board", ChatFormatting.GOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                styled("Three bounties for everyone.", ChatFormatting.GRAY),
                styled("New bounties in " + when, ChatFormatting.YELLOW),
                styled("Click a bounty to claim its reward.", ChatFormatting.DARK_GRAY))));
        return stack;
    }

    private ItemStack questItem(int index, Quest q) {
        boolean claimed = Quests.isClaimed(player, index);
        int progress = Quests.progress(player, index);
        boolean ready = progress >= q.amount();

        ItemStack stack = new ItemStack(iconFor(q));
        ChatFormatting nameColor = claimed ? ChatFormatting.DARK_GRAY : ready ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
        stack.set(DataComponents.CUSTOM_NAME, styled(q.title() + (claimed ? " ✔" : ""), nameColor));

        List<Component> lore = new ArrayList<>();
        String verb = q.type() == Quest.Type.KILL ? "Slain" : "Gathered";
        lore.add(styled(verb + ": " + progress + "/" + q.amount(), ChatFormatting.GRAY));
        lore.add(styled("Reward: +" + q.reward() + " skill point" + (q.reward() == 1 ? "" : "s"), ChatFormatting.AQUA));
        lore.add(Component.literal(""));
        if (claimed) {
            lore.add(styled("Claimed — come back next rotation", ChatFormatting.DARK_GRAY));
        } else if (ready) {
            lore.add(styled(q.type() == Quest.Type.GATHER ? "Click to turn in & claim" : "Click to claim", ChatFormatting.GREEN));
        } else {
            lore.add(styled(q.type() == Quest.Type.GATHER ? "Bring the items here" : "Keep hunting", ChatFormatting.GRAY));
        }
        stack.set(DataComponents.LORE, new ItemLore(lore));
        if (claimed) stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    private static net.minecraft.world.item.Item iconFor(Quest q) {
        if (q.type() == Quest.Type.GATHER) return Quests.item(q.target());
        if (q.target().equals(Quest.ANY_HOSTILE)) return Items.IRON_SWORD;
        net.minecraft.world.item.Item egg = Quests.item(q.target() + "_spawn_egg");
        return egg == Items.PAPER ? Items.IRON_SWORD : egg;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player clicker) {
        if (clicker instanceof ServerPlayer sp) {
            for (int i = 0; i < SLOTS.length; i++) {
                if (slotId == SLOTS[i]) {
                    Quests.claim(sp, i);
                    populate();
                    sendAllDataToRemote();
                    return;
                }
            }
        }
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

    private static Component styled(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color).withStyle(s -> s.withItalic(false));
    }
}
