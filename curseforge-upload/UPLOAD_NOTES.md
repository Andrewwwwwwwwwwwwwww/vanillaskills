# CurseForge upload — VanillaSkills 2.1.0 (4 jars, no texture pack)

All four jars were **downloaded back from the published GitHub release** (`v2.1.0`) and byte-compared
(SHA-256) against the local builds before staging, so what goes to CurseForge is provably the same
file players get from GitHub.

> **No texture pack this time.** The pack did not change in 2.1.0 — the jars still point at the
> v2.0.1 pack asset, which stays where it is. Upload jars only.

---

## 1. Fabric jar (26.2) → **Vanilla-Skills** (projectID `1570558`, slug `vanilla-skills`)
**File:** `vanillaskills-2.1.0+mc26.2.jar` (this folder)
- Game version **26.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 2. NeoForge jar (26.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.0+mc26.2-neoforge.jar`
(in `../../vanillaskills-neoforge/curseforge-upload/`)
- Game version **26.2**, loader **NeoForge**
- Release type: Release

## 3. Fabric jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.0+mc26.1.2.jar`
(in `../../vanillaskills-mc26.1.2/curseforge-upload/`)
- Game version **26.1.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 4. NeoForge jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.0+mc26.1.2-neoforge.jar`
(in `../../vanillaskills-neoforge-mc26.1.2/curseforge-upload/`)
- Game version **26.1.2**, loader **NeoForge**
- Release type: Release

---

## Changelog to paste

```
Hard work pays: a rare Skill Shard trickle from ordinary play.

Added
- Mining or placing a block and harvesting crops (berries and glow berries included) each carry a
  small chance — 0.2% by default — of shaking an Unstable Skill Shard loose on the spot. A payout
  starts a per-player cooldown (4 minutes by default) during which nothing further drops, so the
  trickle rewards ordinary play without ever becoming a farm. Creative mode earns nothing.
- New gameplay.json options: taskShardChance (0 disables the mechanic) and taskShardCooldownSeconds.
- The new source is listed on the Earning Skill Shards screen, with rates read live from the config.

The texture pack is unchanged; servers keep pulling it from the v2.0.1 release automatically.
```

Full detail: <https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/blob/master/CHANGELOG.md>
Documentation: <https://andrewwwwwwwwwwwwwww.github.io/modhub/mods/vanillaskills/>

---

## After upload

CurseForge issues a new fileID once the files are approved. Repoint any modpack manifest that pins
VanillaSkills (the VSP Player/Server packs) from the 2.0.1 fileID to the 2.1.0 one and bump the pack
version.
