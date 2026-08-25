# CurseForge upload — VanillaSkills 2.1.1 (4 jars, no texture pack)

All four jars were **downloaded back from the published GitHub release** (`v2.1.1`) and byte-compared
(SHA-256) against the local builds before staging, so what goes to CurseForge is provably the same
file players get from GitHub.

> **No texture pack this time.** The pack did not change in 2.1.1 — the jars still point at the
> v2.0.1 pack asset, which stays where it is. Upload jars only.

---

## 1. Fabric jar (26.2) → **Vanilla-Skills** (projectID `1570558`, slug `vanilla-skills`)
**File:** `vanillaskills-2.1.1+mc26.2.jar` (this folder)
- Game version **26.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 2. NeoForge jar (26.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.1+mc26.2-neoforge.jar`
(in `../../vanillaskills-neoforge/curseforge-upload/`)
- Game version **26.2**, loader **NeoForge**
- Release type: Release

## 3. Fabric jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.1+mc26.1.2.jar`
(in `../../vanillaskills-mc26.1.2/curseforge-upload/`)
- Game version **26.1.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 4. NeoForge jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.1.1+mc26.1.2-neoforge.jar`
(in `../../vanillaskills-neoforge-mc26.1.2/curseforge-upload/`)
- Game version **26.1.2**, loader **NeoForge**
- Release type: Release

---

## Changelog to paste

```
Advancements pay more, and luck sweetens the task-shard odds.

Changed
- Advancements pay more. With enchanting and the anvil both charging Skill Shards, the old rates
  were too stingy: tasks now pay 5 (was 2), goals 20 (was 12) and challenges 50 (was 45). New worlds
  get the new numbers automatically; an existing world keeps its points.json — raise valueTask /
  valueGoal / valueChallenge there and run /skill recalc <player> to re-credit everyone.
- Luck raises the task-shard odds. The 2.1.0 work-trickle now reads your luck attribute: each point
  (Fortune Finder gives +0.5 per node; Luck potions count too) multiplies the base chance by another
  +10%, so a maxed Fortune Finder lane rolls at +50% odds. New option taskShardLuckBonus.

The texture pack is unchanged; servers keep pulling it from the v2.0.1 release automatically.
```

Full detail: <https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/blob/master/CHANGELOG.md>
Documentation: <https://andrewwwwwwwwwwwwwww.github.io/modhub/mods/vanillaskills/>

---

## After upload

CurseForge issues a new fileID once the files are approved. Repoint any modpack manifest that pins
VanillaSkills (the VSP Player/Server packs) from the 2.0.1 fileID to the 2.1.1 one and bump the pack
version.
