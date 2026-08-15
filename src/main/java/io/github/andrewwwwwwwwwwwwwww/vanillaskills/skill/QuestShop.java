package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import io.github.andrewwwwwwwwwwwwwww.vanillaskills.VanillaSkills;
import io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The Quest Shop: a daily-rotating catalog of boost items bought with Quest Shards (or Skill Shards
 * at the {@link #CONVERT_RATIO} ratio). The daily selection is derived deterministically from the
 * UTC day number, so it is stable for the whole day and needs no persistence.
 */
public final class QuestShop {
    private QuestShop() {}

    /** Quest Shards per 1 Skill Shard (one-way); also the rate when paying for items in Skill Shards.
     *  Default 3 — set live from gameplay.json by {@link GameplayConfig}. */
    public static int CONVERT_RATIO = 3;
    /** How many rotating offers are shown each day. */
    public static int dailyCount() { return io.github.andrewwwwwwwwwwwwwww.vanillaskills.config.GameplayConfig.SHOP_SLOTS; }

    /** A single granted stack within an offer. */
    /**
     * One stack an offer hands over.
     *
     * <p>{@code enchantment} is normally null. When set, the granted item is enchanted with it at
     * {@code enchantLevel} — which is how the shop sells enchanted books without needing a separate offer
     * type. It is resolved against the live registry at purchase time rather than stored as a Holder,
     * because enchantments are a datapack registry and {@code vanillaskills:unboxing} does not exist until
     * the pack has loaded.
     */
    public record Grant(String itemId, int count, String enchantment, int enchantLevel) {
        public Grant(String itemId, int count) {
            this(itemId, count, null, 0);
        }
    }

    /** One shop offer: one or more stacks granted for a Quest-Shard price. */
    public record ShopOffer(String key, String label, List<Grant> grants, int price) {
        public Grant icon() { return grants.get(0); }
        /** Skill-Shard cost when paying with Skill Shards (rounded up). */
        public int skillPrice() { return Math.max(1, (price + CONVERT_RATIO - 1) / CONVERT_RATIO); }
        /** Display name: explicit label, or the icon item's name with its count. */
        public String displayName() {
            if (label != null) return label;
            Grant g = icon();
            String name = new ItemStack(Quests.item(g.itemId())).getHoverName().getString();
            return g.count() > 1 ? g.count() + "× " + name : name;
        }
    }

    private static ShopOffer one(String item, int count, int price) {
        String key = item + "x" + count;
        return new ShopOffer(key, null, List.of(new Grant(item, count)), price);
    }

    private static ShopOffer set(String key, String label, int price, Grant... grants) {
        return new ShopOffer(key, label, List.of(grants), price);
    }

    /** The full catalog (prices in Quest Shards). Editable here; the daily pick is weighted by price. */
    public static final List<ShopOffer> CATALOG = buildCatalog();

    private static List<ShopOffer> buildCatalog() {
        List<ShopOffer> c = new ArrayList<>();
        // --- Food ---
        c.add(one("minecraft:bread", 8, 1));
        c.add(one("minecraft:bread", 16, 2));
        c.add(one("minecraft:bread", 32, 4));
        c.add(one("minecraft:cooked_beef", 8, 3));
        c.add(one("minecraft:cooked_beef", 16, 5));
        c.add(one("minecraft:cooked_chicken", 12, 3));
        c.add(one("minecraft:cooked_porkchop", 12, 4));
        c.add(one("minecraft:baked_potato", 16, 2));
        c.add(one("minecraft:golden_carrot", 8, 6));
        c.add(one("minecraft:apple", 16, 2));
        c.add(one("minecraft:pumpkin_pie", 8, 3));
        c.add(one("minecraft:cookie", 16, 1));
        c.add(one("minecraft:sweet_berries", 16, 2));
        c.add(one("minecraft:honey_bottle", 4, 3));
        c.add(one("minecraft:milk_bucket", 3, 3));
        // --- Building blocks ---
        c.add(one("minecraft:cobblestone", 64, 2));
        c.add(one("minecraft:stone", 32, 2));
        c.add(one("minecraft:stone_bricks", 32, 3));
        c.add(one("minecraft:oak_log", 16, 2));
        c.add(one("minecraft:oak_log", 32, 3));
        c.add(one("minecraft:oak_planks", 32, 2));
        c.add(one("minecraft:glass", 32, 3));
        c.add(one("minecraft:sand", 32, 2));
        c.add(one("minecraft:dirt", 64, 1));
        c.add(one("minecraft:white_wool", 16, 2));
        c.add(one("minecraft:terracotta", 16, 3));
        c.add(one("minecraft:white_concrete_powder", 16, 3));
        c.add(one("minecraft:bricks", 16, 4));
        c.add(one("minecraft:quartz_block", 8, 5));
        c.add(one("minecraft:torch", 16, 1));
        c.add(one("minecraft:torch", 32, 1));
        c.add(one("minecraft:glowstone", 8, 5));
        c.add(one("minecraft:sea_lantern", 4, 6));
        c.add(one("minecraft:scaffolding", 16, 3));
        c.add(one("minecraft:ladder", 16, 2));
        c.add(one("minecraft:obsidian", 4, 4));
        c.add(one("minecraft:obsidian", 8, 7));
        // --- Resources & ingots ---
        c.add(one("minecraft:coal", 16, 1));
        c.add(one("minecraft:coal", 32, 2));
        c.add(one("minecraft:charcoal", 16, 1));
        c.add(one("minecraft:iron_ingot", 4, 3));
        c.add(one("minecraft:iron_ingot", 8, 6));
        c.add(one("minecraft:iron_ingot", 16, 11));
        c.add(one("minecraft:gold_ingot", 4, 4));
        c.add(one("minecraft:gold_ingot", 8, 7));
        c.add(one("minecraft:copper_ingot", 8, 3));
        c.add(one("minecraft:copper_ingot", 16, 5));
        c.add(one("minecraft:redstone", 16, 2));
        c.add(one("minecraft:redstone", 32, 4));
        c.add(one("minecraft:lapis_lazuli", 16, 3));
        c.add(one("minecraft:diamond", 1, 6));
        c.add(one("minecraft:diamond", 2, 11));
        c.add(one("minecraft:diamond", 4, 20));
        c.add(one("minecraft:emerald", 4, 5));
        c.add(one("minecraft:emerald", 8, 9));
        c.add(one("minecraft:amethyst_shard", 8, 4));
        c.add(one("minecraft:string", 16, 2));
        c.add(one("minecraft:leather", 8, 3));
        c.add(one("minecraft:slime_ball", 8, 4));
        c.add(one("minecraft:gunpowder", 8, 4));
        c.add(one("minecraft:bone", 16, 2));
        c.add(one("minecraft:flint", 16, 2));
        c.add(one("minecraft:blaze_rod", 4, 6));
        c.add(one("minecraft:netherite_scrap", 1, 18));
        // --- Tools ---
        c.add(one("minecraft:iron_pickaxe", 1, 5));
        c.add(one("minecraft:iron_axe", 1, 5));
        c.add(one("minecraft:iron_shovel", 1, 3));
        c.add(one("minecraft:iron_sword", 1, 5));
        c.add(one("minecraft:fishing_rod", 1, 3));
        c.add(one("minecraft:shears", 1, 3));
        c.add(one("minecraft:flint_and_steel", 1, 2));
        c.add(one("minecraft:bucket", 1, 2));
        c.add(one("minecraft:bucket", 3, 5));
        c.add(one("minecraft:bow", 1, 4));
        c.add(one("minecraft:crossbow", 1, 5));
        c.add(one("minecraft:shield", 1, 4));
        c.add(one("minecraft:spyglass", 1, 3));
        c.add(one("minecraft:compass", 1, 4));
        c.add(one("minecraft:sponge", 1, 5)); // 5 QS / 2 SS — a way to get sponges before raiding a monument
        // --- Armor ---
        c.add(one("minecraft:iron_helmet", 1, 5));
        c.add(one("minecraft:iron_chestplate", 1, 8));
        c.add(one("minecraft:iron_leggings", 1, 7));
        c.add(one("minecraft:iron_boots", 1, 5));
        c.add(set("iron_set", "Iron Armor Set", 22,
                new Grant("minecraft:iron_helmet", 1), new Grant("minecraft:iron_chestplate", 1),
                new Grant("minecraft:iron_leggings", 1), new Grant("minecraft:iron_boots", 1)));
        c.add(set("leather_set", "Leather Armor Set", 5,
                new Grant("minecraft:leather_helmet", 1), new Grant("minecraft:leather_chestplate", 1),
                new Grant("minecraft:leather_leggings", 1), new Grant("minecraft:leather_boots", 1)));
        c.add(one("minecraft:turtle_helmet", 1, 6));
        // --- Combat / utility / mobility ---
        c.add(one("minecraft:arrow", 16, 2));
        c.add(one("minecraft:arrow", 32, 3));
        c.add(one("minecraft:ender_pearl", 2, 4));
        c.add(one("minecraft:ender_pearl", 4, 7));
        c.add(one("minecraft:firework_rocket", 16, 4));
        c.add(one("minecraft:tnt", 4, 7));
        c.add(one("minecraft:golden_apple", 1, 8));
        c.add(one("minecraft:golden_apple", 2, 15));
        // (Bottles o' Enchanting were sold here until 2.0 removed experience from the game.)
        c.add(one("minecraft:saddle", 1, 5));
        c.add(one("minecraft:name_tag", 1, 4));
        c.add(one("minecraft:lead", 4, 3));
        c.add(one("minecraft:powered_rail", 8, 6));
        // --- Farming / misc ---
        c.add(one("minecraft:bone_meal", 16, 2));
        c.add(one("minecraft:wheat_seeds", 16, 1));
        c.add(one("minecraft:sugar_cane", 16, 2));
        c.add(one("minecraft:nether_wart", 8, 3));
        c.add(one("minecraft:bookshelf", 4, 5));
        c.add(one("minecraft:book", 8, 2));

        // --- Enchanted books ---
        //
        // Deliberately capped at level I–II. A shelved book is permanent: once it is in a chiseled bookshelf
        // beside an Infusing Table, that enchantment is available to the player forever. Selling high levels
        // outright would end the progression, whereas low levels are a starting point — buy two and combine
        // them at an anvil, which is exactly why book+book combining was kept when book-on-tool was removed.
        //
        // Priced well above the rest of the catalogue (which tops out at 22) for the same reason: this is
        // infrastructure, not a consumable. Fortune IV/V are absent on purpose — those are minted, and their
        // books are consumed on use.
        c.add(book("minecraft:efficiency", 1, 26));
        c.add(book("minecraft:efficiency", 2, 40));
        c.add(book("minecraft:unbreaking", 1, 26));
        c.add(book("minecraft:unbreaking", 2, 40));
        c.add(book("minecraft:protection", 1, 30));
        c.add(book("minecraft:protection", 2, 45));
        c.add(book("minecraft:sharpness", 1, 30));
        c.add(book("minecraft:sharpness", 2, 45));
        c.add(book("minecraft:feather_falling", 1, 26));
        c.add(book("minecraft:feather_falling", 2, 40));
        c.add(book("minecraft:respiration", 1, 26));
        c.add(book("minecraft:looting", 1, 35));
        c.add(book("minecraft:looting", 2, 50));
        c.add(book("minecraft:luck_of_the_sea", 1, 35));
        c.add(book("minecraft:luck_of_the_sea", 2, 50));
        c.add(book("minecraft:fortune", 1, 55));
        c.add(book("minecraft:silk_touch", 1, 55));
        c.add(book("vanillaskills:unboxing", 1, 45));
        return c;
    }

    /** An enchanted book offer. The key includes the level so the two tiers are distinct offers. */
    private static ShopOffer book(String enchantment, int level, int price) {
        String shortName = enchantment.substring(enchantment.indexOf(':') + 1);
        String label = Character.toUpperCase(shortName.charAt(0))
                + shortName.substring(1).replace('_', ' ')
                + " " + "I".repeat(Math.max(1, level));
        return new ShopOffer("book:" + enchantment + ":" + level, label,
                List.of(new Grant("minecraft:enchanted_book", 1, enchantment, level)), price);
    }

    /** Selection weight by price tier — cheaper commons appear more often than premiums. */
    private static int weight(ShopOffer o) {
        int p = o.price();
        if (p <= 2) return 6;
        if (p <= 4) return 4;
        if (p <= 7) return 3;
        if (p <= 11) return 2;
        return 1;
    }

    /** The UTC day number used as the rotation seed. */
    public static long currentDay() {
        return System.currentTimeMillis() / GameplayConfig.SHOP_REFRESH_MS; // rotation period
    }

    /** Milliseconds until the next daily rotation (UTC midnight). */
    public static long msUntilRotation() {
        long period = GameplayConfig.SHOP_REFRESH_MS;
        return period - (System.currentTimeMillis() % period);
    }

    /** Today's rotating offers (weighted, distinct, stable for the whole UTC day). */
    public static List<ShopOffer> dailyOffers() {
        Random rng = new Random(currentDay() * 0x9E3779B97F4A7C15L);
        List<ShopOffer> pool = new ArrayList<>(CATALOG);
        List<ShopOffer> chosen = new ArrayList<>();
        int count = Math.min(dailyCount(), pool.size());
        for (int n = 0; n < count; n++) {
            int total = 0;
            for (ShopOffer o : pool) total += weight(o);
            int r = rng.nextInt(total);
            int idx = 0;
            for (int i = 0; i < pool.size(); i++) {
                r -= weight(pool.get(i));
                if (r < 0) { idx = i; break; }
            }
            chosen.add(pool.remove(idx));
        }
        return chosen;
    }

    /** Attempt to buy an offer with the chosen currency; messages the player and returns success. */
    public static boolean purchase(ServerPlayer player, ShopOffer offer, boolean paySkillShards) {
        if (paySkillShards) {
            int cost = offer.skillPrice();
            if (!VanillaSkills.PLAYERS.spendSkillShards(player, cost)) {
                player.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,"vanillaskills.msg.need_skill","Not enough Skill Shards (need %d).", cost))
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        } else {
            if (!VanillaSkills.PLAYERS.spendQuestShards(player, offer.price())) {
                player.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,"vanillaskills.msg.need_quest","Not enough Quest Shards (need %d).", offer.price()))
                        .withStyle(ChatFormatting.RED));
                return false;
            }
        }
        for (Grant g : offer.grants()) {
            ItemStack stack = new ItemStack(Quests.item(g.itemId()), g.count());
            applyEnchantment(player, stack, g);
            player.getInventory().placeItemBackInInventory(stack);
        }
        String paid = paySkillShards
                ? io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,
                        "vanillaskills.msg.amount_skill", "%d Skill Shards", offer.skillPrice())
                : io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,
                        "vanillaskills.msg.amount_quest", "%d Quest Shards", offer.price());
        player.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,"vanillaskills.msg.purchased","Purchased %s for %s.", offer.displayName(), paid))
                .withStyle(ChatFormatting.GREEN));
        return true;
    }

    /**
     * Stamp a granted stack with its offer's enchantment, if it has one.
     *
     * <p>An {@code enchanted_book} carries its enchantment in {@code stored_enchantments} rather than
     * {@code enchantments}, and {@code EnchantmentHelper.updateEnchantments} already routes to the right one
     * based on the item — so this works for both a book and, if ever wanted, a directly-enchanted tool.
     *
     * <p>Silently leaves the stack plain if the enchantment cannot be resolved. A shop offer must never be
     * able to fail a purchase the player has already paid for.
     */
    private static void applyEnchantment(ServerPlayer player, ItemStack stack, Grant grant) {
        if (grant.enchantment() == null) return;
        var server = player.level().getServer();
        if (server == null) return;
        var id = net.minecraft.resources.Identifier.tryParse(grant.enchantment());
        if (id == null) return;
        var registry = server.registryAccess()
                .lookup(net.minecraft.core.registries.Registries.ENCHANTMENT).orElse(null);
        if (registry == null) return;
        var holder = registry.get(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.ENCHANTMENT, id)).orElse(null);
        if (holder == null) return;
        net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(stack,
                mutable -> mutable.set(holder, Math.max(1, grant.enchantLevel())));
    }

    /** Convert Quest Shards → 1 Skill Shard at the 3:1 ratio; messages the player. */
    public static boolean convertOne(ServerPlayer player) {
        if (!VanillaSkills.PLAYERS.convertToSkillShards(player, 1)) {
            player.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,"vanillaskills.msg.need_quest","Not enough Quest Shards (need %d).", CONVERT_RATIO))
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        player.sendSystemMessage(Component.literal(io.github.andrewwwwwwwwwwwwwww.vanillaskills.text.Lang.tr(player,
                "vanillaskills.msg.converted", "Converted %d Quest Shards → 1 Skill Shard.", CONVERT_RATIO))
                .withStyle(ChatFormatting.GREEN));
        return true;
    }
}
