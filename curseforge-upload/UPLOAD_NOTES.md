# CurseForge upload — VanillaSkills 2.1.9 (4 jars, no texture pack)

All four jars were **downloaded back from the published GitHub release** (`v2.1.9`) and byte-compared
(SHA-256) against the local builds before staging, so what goes to CurseForge is provably the same
file players get from GitHub.

> **No texture pack this time.** The pack did not change in 2.1.9 — the jars still point at the
> v2.0.1 pack asset, which stays where it is. Upload jars only.

---

## 1. Fabric jar (26.2) → **Vanilla-Skills** (projectID `1570558`, slug `vanilla-skills`)
**File:** `vanillaskills-2.1.9+mc26.2.jar` (this folder)
- Game version **26.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 2. NeoForge jar (26.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.9+mc26.2-neoforge.jar`
(in `../../vanillaskills-neoforge/curseforge-upload/`)
- Game version **26.2**, loader **NeoForge**
- Release type: Release

## 3. Fabric jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.9+mc26.1.2.jar`
(in `../../vanillaskills-mc26.1.2/curseforge-upload/`)
- Game version **26.1.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 4. NeoForge jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.9+mc26.1.2-neoforge.jar`
(in `../../vanillaskills-neoforge-mc26.1.2/curseforge-upload/`)
- Game version **26.1.2**, loader **NeoForge**
- Release type: Release

---

## Changelog to paste

The CurseForge page jumps 2.0.1 → 2.1.9, so this covers all eleven releases since:

```
Everything since 2.0.1: anvils and infusing re-priced around finite shards, a rare
shard trickle from ordinary play, richer advancement payouts, repair materials for
the repair-less items, a self-climbing crate ladder, and a batch of fixes.

Added
- Hard work pays: mining or placing a block and harvesting crops (berries included)
  each carry a small chance — 0.2% by default — of shaking an Unstable Skill Shard
  loose on the spot. A payout starts a 4-minute per-player cooldown, so the trickle
  rewards ordinary play without ever becoming a farm. Creative earns nothing. Your
  luck attribute (Fortune Finder, Luck potions) sweetens the odds: +10% per point.
  Options: taskShardChance (0 disables), taskShardCooldownSeconds, taskShardLuckBonus.
  Listed on the Earning Skill Shards screen with rates read live from the config.
- Repair materials for the items vanilla ships combine-only: Trident <- Prismarine,
  Bow / Crossbow / Fishing Rod <- String, Flint and Steel / Shears <- Iron Ingot,
  Brush <- Copper Ingot. Toggle: vanillaRepairMaterials.
- The config files explain themselves: gameplay.json and points.json carry an
  #option-guide block describing every option, always current with the installed
  version.
- Every crate has a small chance of an Unboxing book, tiered to the crate: Wooden ->
  Unboxing I (3%), Copper -> II (3%), Iron (5%) / Diamond (7%) / biome crates (4%) ->
  III — so the humblest crate offers a rung up the ladder, and a crate can never drop
  two identical Unboxing books.

Changed
- Anvils now price by what the operation actually consumes, not vanilla's level formula:
  one shard per repair material and one per enchantment level on the sacrificed item,
  with renames free by default. The prior-work penalty no longer compounds, so gear
  never becomes "too expensive". Dragon scale repair drops from 20 shards to 2.
  Options: anvilMaterialPricing, anvilRepairCostPerMaterial, anvilEnchantCostPerLevel,
  anvilRenameCost.
- Infusing drops to 2 shards per enchantment level (was 3) — a level V book now costs
  10, a full sword loadout about 28.
- Advancements pay more: tasks 5 (was 2), goals 20 (was 12), challenges 50 (was 45).
- Luck of the Sea (and your luck attribute) reels in a few more crates: ~+10% relative
  crate odds per point of fishing luck. Unboxing still upgrades which crate bites.
- Existing worlds pick up the price changes automatically on update, and every player
  is repriced once on their next login — no config edits, no /skill recalc needed.

Fixed
- Respawning restores your whole health bar: a player with Vitality hearts used to come
  back at base health. An End-portal return still keeps the health you left with.
- A free anvil result (like the now-free rename) can actually be taken.
- Repairing by combine charges a flat 2 shards for the durability restored — feeding an
  enchanted tool a plain spare is no longer free (anvilCombineRepairCost).
- Renaming VanillaSkills gear sticks instead of silently reverting.
- The Satchel keeps its name through placing, the block entity, and mining.

The texture pack is unchanged; servers keep pulling it from the v2.0.1 release
automatically.
```

Full detail: <https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/blob/master/CHANGELOG.md>
Documentation: <https://andrewwwwwwwwwwwwwww.github.io/modhub/mods/vanillaskills/>

---

## After upload

CurseForge issues a new fileID once the files are approved. Repoint any modpack manifest that pins
VanillaSkills (the VSP Player/Server packs) from the 2.0.1 fileID to the 2.1.9 one and bump the pack
version.
