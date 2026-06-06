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

            Find a Fortune Upgrade Template in Ancient City and mineshaft chests.

            Duplicate it with glow berries, sculk, a diamond block and an emerald block.""",

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

            Steel: 2 iron + 1 coal = ingot. Tanky.

            Repair each at an anvil with its own material.""",

            """
            Crystalline (Diamond II)

            Craft Crystallized Diamonds: 4 diamonds + 1 amethyst block = 4.

            Armor and tools sit between diamond and netherite. Full armor set reflects 25% of melee damage.""",

            """
            Dragon (Netherite II)

            Slay the Ender Dragon for Dragon Scales (8 drop).

            Full set: immune to fire, lava and dragon's breath.

            Hold sneak in midair to dive-dash.""",

            """
            Dragon Ingot

            Surround a Netherite Ingot with 8 Dragon Scales in a crafting table to forge a Dragon Ingot.""",

            """
            Dragon Upgrade

            Find a Dragon Upgrade template in End City treasure (~4%).

            Smithing table: template + netherite armor + Dragon Ingot = Dragon armor (keeps enchants).""",

            """
            Dragon (cont.)

            Dragon tools are crafted from Dragon Ingots.

            Duplicate the template: chorus flowers around it, a netherite ingot below, end rods + a shulker shell on the bottom row.""",

            """
            Dragon Elytra

            Drop a Dragon chestplate and an Elytra on top of an anvil to fuse them into a gliding chestplate.

            Drop it on a grindstone to split them again.""",

            """
            Crafting Unlocks

            Armorsmith & Toolsmith have a node per tier - unlock a tier to craft its armor/tools.

            Ingots and materials are never locked.""",

            """
            Potions

            Brewmaster (5 nodes): beneficial potions last up to +100% longer.

            Potions stack to 16."""
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
