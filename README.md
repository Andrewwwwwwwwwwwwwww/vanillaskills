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

## Planned

- Per-player skill tree (chest-based GUI, importable server-defined trees)
- New tool / weapon / armor tiers (hardwood, deepslate, rose gold, steel, etc.)
- Custom smithing template for upgrades and repairs
- Tier-specific combat effects (bleed, poison, burn, dash)
- Quality-of-life: longer potions, stackable potions/soups, stronger tipped arrows

## Building

Open in IntelliJ IDEA with the Fabric/Loom Gradle import, then run the `build`
task. Output jar lands in `build/libs/`.

## License

MIT
