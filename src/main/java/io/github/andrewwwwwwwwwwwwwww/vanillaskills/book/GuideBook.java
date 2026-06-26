package io.github.andrewwwwwwwwwwwwwww.vanillaskills.book;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig;
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

            Two currencies drive everything:

            Skill Shards - from advancements.
            Quest Shards - from bounties.

            Type /help for commands.""",

            """
            Skill Tree
            (/skill)

            Spend Skill Shards on lanes of perks (health, speed, mining, combat, and more).

            Click a node to buy it and everything below it. Choose wisely - unlocks are permanent.

            Bottom-left: your Shards. Bottom-right: your stats.""",

            """
            Earning Skill Shards

            Every advancement gives Skill Shards, once each. Harder ones pay much more:

            tasks - a little
            goals - more
            challenges (purple) - a lot

            Doing them all can buy the whole tree.""",

            """
            Bounty Board
            (/quests or /bounty)

            Three bounties at a time, refreshing on a timer. Gather items or slay mobs for Quest Shards.

            New players start on a personal starter board - complete 15 quests to join the main board.""",

            """
            Quest Shop

            Open the Shop from the bounty board. A rotating set of boost items, bought with Quest Shards (or Skill Shards).

            A converter trades 3 Quest Shards for 1 Skill Shard.""",

            """
            Crafting Ladders

            Armorsmith & Toolsmith are paid in Quest Shards and gate crafting EVERY gear tier - copper, iron, diamond, and the custom tiers.

            Wood & stone stay free. Found/traded gear always works.""",

            """
            Deepslate

            Deepslate and its ores need a Steel-tier or better pickaxe (Steel, Diamond, Crystalline, Netherite, Dragon).

            Unlock Steel in the Toolsmith lane to mine the deep layer.""",

            """
            Armor Tiers

            Hardwood: from Wood blocks. Light and fast.

            Rose Gold: 4 gold + 4 copper. Full set: immune to bad effects, piglins neutral.

            Steel: 2 iron + 1 coal. Tanky.""",

            """
            Crystalline (Diamond II)

            Crystallized Diamonds: 4 diamonds + 1 amethyst block = 4.

            Between diamond and netherite. Full set reflects 25% of melee damage.""",

            """
            Dragon (Netherite II)

            Slay the Ender Dragon for 8 Dragon Scales. Surround a Netherite Ingot with them to forge a Dragon Ingot.

            Full set: immune to fire/lava/breath. Sneak midair to dive-dash.""",

            """
            Dragon Upgrade

            Find a Dragon Upgrade template in End City treasure (~4%).

            Smithing: template + netherite armor + Dragon Ingot = Dragon armor (keeps enchants). Dragon tools use Dragon Ingots.""",

            """
            Dragon Elytra

            Drop a Dragon chestplate and an Elytra on top of an anvil to fuse them into a gliding chestplate.

            Drop it on a grindstone to split them.""",

            """
            Fortune IV & V

            Find a Fortune Upgrade Template in Ancient City / mineshaft chests.

            Combine two Fortune III books + the template (lapis & diamond blocks) for a Fortune IV book. IV+IV = V.""",

            """
            Potions & Mending

            Brewmaster (5 nodes): beneficial potions last up to +50% longer. Potions stack to 16.

            Mending is removed - it never appears anywhere.

            See all recipes via the Recipes icon on the skill screen."""
    };

    public static ItemStack create() {
        List<Filterable<Component>> pages = new ArrayList<>();
        for (String page : PAGES) {
            // Keep the Mending line accurate to the current server setting.
            String text = page.replace("Mending is removed - it never appears anywhere.",
                    GameplayConfig.MENDING_ENABLED
                            ? "Mending is enabled on this server and works normally."
                            : "Mending is removed - it never appears anywhere.");
            pages.add(Filterable.passThrough(Component.literal(text)));
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
