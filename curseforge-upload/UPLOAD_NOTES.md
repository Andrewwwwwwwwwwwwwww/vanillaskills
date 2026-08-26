# CurseForge upload — VanillaSkills 2.1.3 (4 jars, no texture pack)

All four jars were **downloaded back from the published GitHub release** (`v2.1.3`) and byte-compared
(SHA-256) against the local builds before staging, so what goes to CurseForge is provably the same
file players get from GitHub.

> **No texture pack this time.** The pack did not change in 2.1.3 — the jars still point at the
> v2.0.1 pack asset, which stays where it is. Upload jars only.

---

## 1. Fabric jar (26.2) → **Vanilla-Skills** (projectID `1570558`, slug `vanilla-skills`)
**File:** `vanillaskills-2.1.3+mc26.2.jar` (this folder)
- Game version **26.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 2. NeoForge jar (26.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.3+mc26.2-neoforge.jar`
(in `../../vanillaskills-neoforge/curseforge-upload/`)
- Game version **26.2**, loader **NeoForge**
- Release type: Release

## 3. Fabric jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.3+mc26.1.2.jar`
(in `../../vanillaskills-mc26.1.2/curseforge-upload/`)
- Game version **26.1.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 4. NeoForge jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.3+mc26.1.2-neoforge.jar`
(in `../../vanillaskills-neoforge-mc26.1.2/curseforge-upload/`)
- Game version **26.1.2**, loader **NeoForge**
- Release type: Release

---

## Changelog to paste

The CurseForge page jumps 2.0.1 → 2.1.3, so this covers all five releases since:

```
Everything since 2.0.1: anvils and infusing re-priced around finite shards, a rare
shard trickle from ordinary play, richer advancement payouts, and a respawn health fix.

Fixed
- Respawning restores your whole health bar: a player with Vitality hearts used to come
  back at base health. An End-portal return still keeps the health you left with.

Changed
- Infusing drops to 2 shards per enchantment level (was 3) — a level V book now costs
  10, a full sword loadout about 28. Existing worlds: set infusingCostPerLevel to 2 in
  gameplay.json and /skill reload.

Changed
- Anvils now price by what the operation actually consumes, not vanilla's level formula:
  one shard per repair material and one per enchantment level on the sacrificed item,
  with renames free by default. The prior-work penalty no longer compounds, so gear
  never becomes "too expensive". Dragon scale repair drops from 20 shards to 2.
  Options: anvilMaterialPricing, anvilRepairCostPerMaterial, anvilEnchantCostPerLevel,
  anvilRenameCost.
- Advancements pay more: tasks 5 (was 2), goals 20 (was 12), challenges 50 (was 45).
  New worlds get the new numbers automatically; on an existing world raise valueTask /
  valueGoal / valueChallenge in points.json and run /skill recalc <player>.

Added
- Hard work pays: mining or placing a block and harvesting crops (berries included)
  each carry a small chance — 0.2% by default — of shaking an Unstable Skill Shard
  loose on the spot. A payout starts a 4-minute per-player cooldown, so the trickle
  rewards ordinary play without ever becoming a farm. Creative earns nothing.
- Your luck attribute sweetens those odds: each point (Fortune Finder, Luck potions)
  multiplies the chance by another +10%, so a maxed Fortune Finder rolls at +50%.
  Options: taskShardChance (0 disables), taskShardCooldownSeconds, taskShardLuckBonus.
- The new source is listed on the Earning Skill Shards screen, rates read live from
  the config.

The texture pack is unchanged; servers keep pulling it from the v2.0.1 release
automatically.
```

Full detail: <https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/blob/master/CHANGELOG.md>
Documentation: <https://andrewwwwwwwwwwwwwww.github.io/modhub/mods/vanillaskills/>

---

## After upload

CurseForge issues a new fileID once the files are approved. Repoint any modpack manifest that pins
VanillaSkills (the VSP Player/Server packs) from the 2.0.1 fileID to the 2.1.3 one and bump the pack
version.
