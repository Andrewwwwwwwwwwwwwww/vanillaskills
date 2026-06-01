package io.github.andrewwwwwwwwwwwwwww.vanillaskills.book;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the mod's guide as a written book and opens it on the player's screen without changing
 * their inventory: the book is sent to the client's view of the held slot, the open-book packet
 * is sent, then the real held item is re-sent to correct the client.
 */
public final class GuideBook {
    private GuideBook() {}

    private static final String[] PAGES = {
            """
            VanillaSkills

            A server-side progression overhaul.

            Use /skill to open your skill tree. Earn skill points by completing advancements.

            Turn the page for features & crafting.""",

            """
            Skill Tree

            /skill - open the tree
            /skill points - your points

            Click a node to unlock it if you can afford it and meet its requirements.

            Ops: /skill editor, /skill edit""",

            """
            Mending

            Mending is removed from the game entirely - villager trades, chest loot, and the enchanting table will never give it.""",

            """
            Fortune IV & V

            Find a Fortune Upgrade Template in Ancient City and mineshaft minecart chests.

            Duplicate it by surrounding it with glow berries, sculk, a diamond block and an emerald block.""",

            """
            Fortune IV & V (cont.)

            Upgrade books in this shape:
            lapis | diamond | lapis
            book | template | book
            lapis | diamond | lapis

            Two Fortune III books make a Fortune IV book. Anvil it onto a tool.""",

            """
            Armor Tiers

            Hardwood: craft from Wood blocks. Light and fast.

            Rose Gold: 4 gold + 4 copper = ingots. Full set = status immunity, piglins neutral.

            Steel: 1 iron + 1 coal = ingot. Tanky.

            Repair each at an anvil with its own material."""
    };

    public static ItemStack create() {
        List<Filterable<Component>> pages = new ArrayList<>();
        for (String page : PAGES) {
            pages.add(Filterable.passThrough(Component.literal(page)));
        }
        WrittenBookContent content = new WrittenBookContent(
                Filterable.passThrough("VanillaSkills Guide"), "VanillaSkills", 0, pages, false);
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        return book;
    }

    public static void open(ServerPlayer player) {
        ItemStack book = create();
        int slot = 36 + player.getInventory().getSelectedSlot();
        int containerId = player.inventoryMenu.containerId;
        player.connection.send(new ClientboundContainerSetSlotPacket(
                containerId, player.inventoryMenu.incrementStateId(), slot, book));
        player.connection.send(new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND));
        player.connection.send(new ClientboundContainerSetSlotPacket(
                containerId, player.inventoryMenu.incrementStateId(), slot, player.getMainHandItem()));
    }
}
