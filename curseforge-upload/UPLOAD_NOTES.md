# CurseForge upload — VanillaSkills 2.0.2 (4 jars, no texture pack)

All four jars were **downloaded back from the published GitHub release** (`v2.0.2`) and byte-compared
(SHA-256) against the local builds before staging, so what goes to CurseForge is provably the same
file players get from GitHub.

> **No texture pack this time.** The pack did not change in 2.0.2 — the jars still point at the
> v2.0.1 pack asset, which stays where it is. Upload jars only.

---

## 1. Fabric jar (26.2) → **Vanilla-Skills** (projectID `1570558`, slug `vanilla-skills`)
**File:** `vanillaskills-2.0.2+mc26.2.jar` (this folder)
- Game version **26.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 2. NeoForge jar (26.2) → same project (`1570558`)
**File:** `vanillaskills-2.0.2+mc26.2-neoforge.jar`
(in `../../vanillaskills-neoforge/curseforge-upload/`)
- Game version **26.2**, loader **NeoForge**
- Release type: Release

## 3. Fabric jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.0.2+mc26.1.2.jar`
(in `../../vanillaskills-mc26.1.2/curseforge-upload/`)
- Game version **26.1.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 4. NeoForge jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.0.2+mc26.1.2-neoforge.jar`
(in `../../vanillaskills-neoforge-mc26.1.2/curseforge-upload/`)
- Game version **26.1.2**, loader **NeoForge**
- Release type: Release

---

## Changelog to paste

```
Anvils are re-priced around Skill Shards being finite.

Changed
- Anvils now price by what the operation actually consumes, not vanilla's level formula. Vanilla's
  cost curve was built around experience, which regrows; Skill Shards do not — a world holds a fixed
  number, so a late-game combine could eat 39 shards and the prior-work penalty doubled on every
  visit until a piece was unrepairable. The cost is now one shard per repair material and one per
  enchantment level on the sacrificed item, with a flat (default free) fee for a plain rename.
  New gameplay.json options: anvilMaterialPricing, anvilRepairCostPerMaterial,
  anvilEnchantCostPerLevel and anvilRenameCost; clearing the first restores vanilla's numbers.
- Dragon scale repair drops from 20 shards to 2. One scale still restores a piece completely, so it
  stays a flat fee — 20 only made sense when an ordinary combine cost 30–40.

The texture pack is unchanged; servers keep pulling it from the v2.0.1 release automatically.
```

Full detail: <https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/blob/master/CHANGELOG.md>
Documentation: <https://andrewwwwwwwwwwwwwww.github.io/modhub/mods/vanillaskills/>

---

## After upload

CurseForge issues a new fileID once the files are approved. Repoint any modpack manifest that pins
VanillaSkills (the VSP Player/Server packs) from the 2.0.1 fileID to the 2.0.2 one and bump the pack
version.
