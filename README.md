# VanillaSkills

A **server-side** skill-tree and progression overhaul for Fabric (Minecraft 26.1.2).

Players connect with a vanilla client — no client mod required. A bundled resource
pack (added later) supplies textures for the new items.

## Current features

### Mending removal
Mending is completely removed from the game:

- **Hard guarantee (mixin):** `ItemEnchantmentsMutableMixin` hooks the single
  chokepoint every enchantment application passes through
  (`ItemEnchantments.Mutable#set` / `#upgrade`). Mending can never be written onto
  any item — covers loot tables, the enchanting table, the `/enchant` command,
  villager `createBook`, and anything else.
- **Clean rolls (datapack tags):** bundled overrides of `#minecraft:tradeable` and
  `#minecraft:on_random_loot` remove Mending from the selection pools so librarian
  trades and chest loot never *roll* it (avoiding blank enchanted books).

### Skill tree
Per-player skill tree with a chest-based GUI (no client mod needed):

- **Points** are earned from advancements (configurable per-advancement in
  `config/vanillaskills/points.json`; recipe advancements ignored; existing players
  are credited retroactively on first join).
- **Tree** is defined in `config/vanillaskills/skilltree.json` (a default 5-lane starter
  tree — Health, Speed, Mining, Luck, Damage — is written on first run). Effects support
  `attribute`, `status_effect`, and `flag` types. Attribute bonuses apply as transient
  modifiers on join/respawn, so they never duplicate.
- **Commands:**
  - `/skill` (or `/skills`) — open the tree GUI
  - `/skill points` — show your points
  - `/skill points <player> add|set <n>` — op
  - `/skill reset <player>` / `/skill recalc <player>` — op
  - `/skill reload` — op, reloads tree + points config
  - `/skill editor` — op, opens the tree GUI in **edit mode**: left-click a node to pick it
    up, click an empty slot to move it or another node to swap, right-click to delete
  - `/skill edit ...` — op, live-edit the server's tree (add/remove nodes, set
    cost/slot/icon/title/desc, requirements, attribute effects) writing back to JSON

### Craftable Fortune IV & V
Fortune past the vanilla cap of III, obtainable only by crafting:

1. **Fortune Upgrade Smithing Template** — a vanilla netherite upgrade template with a custom
   name + hidden marker tag (kept as a vanilla item so the mod stays server-side; a recolored
   texture can come via a resource pack). It is **its own thing**: a mixin blocks the marked
   template from working in the smithing table, so it can't be used as a netherite upgrade.
   - **Found** in Ancient City and minecart (abandoned mineshaft) chests at ~8.6% per chest —
     the same rarity as an Ancient City enchanted golden apple (injected via the loot API).
   - **Duplicated** in a crafting table (output 2):
     ```
     glow berries | Fortune Upgrade template | glow berries
     sculk        | diamond block            | sculk
     glow berries | emerald block            | glow berries
     ```
2. **Upgrade a Fortune book** — consumes two Fortune N books + the template:
   ```
   lapis block | diamond block | lapis block
   Fortune N book | Fortune Upgrade template | Fortune N book
   lapis block | diamond block | lapis block
   ```
   Output: one **Fortune (N+1)** book (N = 3 → IV, N = 4 → V). The template is consumed.
3. Apply the book to a tool in an **anvil**. Fortune's `max_level` stays 3, so the enchanting
   table, villagers, loot and anvil-*combining* never exceed III — IV/V are exclusive to this
   recipe. An anvil mixin un-clamps the over-level book/tool so the IV/V actually sticks.

### New armor tiers
Reskinned vanilla armor stamped with custom stats/name/marker (server-side; textures via an
optional resource pack keyed on the `custom_model_data` hook). Crafted in normal armor shapes
via one custom recipe; repaired at an anvil with the tier's material.

- **Hardwood** (leather base) — crafted from **Wood blocks** (`oak_wood` etc., stripped + hyphae
  too). Light & fast: +8% movement with a full set.
- **Rose Gold** (golden base) — crafted from **Rose Gold Ingots** (4 gold + 4 copper → 4).
  Immune to **all** negative status effects with a full set; piglins stay neutral (gold base).
- **Steel** (iron base) — crafted from **Steel Ingots** (1 iron + 1 coal → 1). High armor +
  high toughness for reliable defense; slightly slower (−4% with a full set).
- *Diamond II and Netherite II are planned but not built yet.*

Both metals are vanilla copper ingots with a name + hidden marker (no new items registered).
Anvil repair is locked to each tier's own material — only Rose Gold Ingots repair Rose Gold
armor, only Steel Ingots repair Steel armor (plain copper / the wrong alloy won't work).

## Planned

- In-game drag/click tree editor (currently editing is via `/skill edit` + JSON)
- Diamond II & Netherite II armor tiers
- New tool / weapon / armor tiers (hardwood, deepslate, rose gold, steel, etc.)
- Custom smithing template for upgrades and repairs
- Tier-specific combat effects (bleed, poison, burn, dash)
- Craftable higher-tier enchantments (Fortune IV & V)
- Quality-of-life: longer potions, stackable potions/soups, stronger tipped arrows

## Building

Open in IntelliJ IDEA with the Fabric/Loom Gradle import, then run the `build`
task. Output jar lands in `build/libs/`.

## License

MIT
