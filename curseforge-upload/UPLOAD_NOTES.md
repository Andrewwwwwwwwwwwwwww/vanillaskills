# CurseForge upload — VanillaSkills 2.0.0 (4 jars + texture pack)

All four jars in this folder were **downloaded back from the published GitHub release** (`v2.0.0`) and
byte-compared against the local build before staging, so what goes to CurseForge is provably the same
file players get from GitHub.

> **Upload the texture pack and the jars together.** The jars have the pack's URL **and SHA-1** baked in
> as the auto-push default, and the client rejects the download if the hash does not match byte for byte.

Pack SHA-1: `8221d178584b92f055d93e9f691730dab7398f0e` (195,810 bytes)

---

## 1. Fabric jar (26.2) → **Vanilla-Skills** (projectID `1570558`, slug `vanilla-skills`)
**File:** `vanillaskills-2.0.0+mc26.2.jar` (this folder)
- Game version **26.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 2. NeoForge jar (26.2) → same project (`1570558`)
**File:** `vanillaskills-2.0.0+mc26.2-neoforge.jar`
(in `../../vanillaskills-neoforge/curseforge-upload/`)
- Game version **26.2**, loader **NeoForge**
- Release type: Release

## 3. Fabric jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.0.0+mc26.1.2.jar`
(in `../../../26.1.2/vanillaskills/curseforge-upload/`)
- Game version **26.1.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 4. NeoForge jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-2.0.0+mc26.1.2-neoforge.jar`
(in `../../../26.1.2/vanillaskills-neoforge/curseforge-upload/`)
- Game version **26.1.2**, loader **NeoForge**
- Release type: Release

## 5. Texture pack → **VSTP - Vanilla-Skills** (projectID `1585850`)
**File:** `VanillaSkills-TexturePack.zip`
- Game version tag: 26.2 (pack format range also covers 26.1.2)
- Release type: Release
- SHA-1: `8221d178584b92f055d93e9f691730dab7398f0e`

> **The pack MUST be re-uploaded for 2.0.0.** It is not cosmetic housekeeping: 2.0 takes over reinforced
> deepslate and lodestone as the two Skill Shard blocks, and the retextures and renamed block names live
> only in this pack for vanilla clients. A server still pushing the 1.7.6 pack shows those as ordinary
> vanilla blocks. The 1.7.6 pack is also actively wrong under 2.0 — it overrode 57 vanilla items through
> the old `custom_model_data` route that 2.0 replaced.

---

## Marking it a breaking release

2.0.0 is **not** a drop-in update. Say so on the file page:

- Commands were removed or renamed (`/skill points` → `/skill skillshards`).
- Save formats changed and are migrated **one way** — back up the world first.
- The pushed texture pack was replaced.
- Experience is removed by default; anvils and the new Infusing Table charge Skill Shards.

## Changelog to paste

```
The datapack-era rework. Experience is removed, the Skill Shard is a real item, and almost all of the
mod's content — the skill tree, quests, shop, crates and feats — lives in datapack files, so a server
can rewrite any of it without touching code.

THIS IS A BREAKING RELEASE. Commands were removed, save formats changed, and the pushed texture pack
was replaced. Back up your world before updating.

Added
- Skill Shards are physical: withdraw them as Unstable Skill Shards, right-click to bank them again.
  Nine compress into an Unstable Skill Shard Block, which also generates as ore in all three dimensions
  and needs a Netherite, Crystalline or Dragon pickaxe.
- Stable Skill Shard Blocks damage nearby hostiles, merge to widen that aura, work as a beacon base,
  and are immune to explosions.
- Crates fished out of the water: Wooden, Copper, Iron and Diamond, plus Frozen, Lush and Desert
  variants, with a slot-machine reel when you open one.
- The Infusing Table replaces enchanting. It offers exactly the enchantments shelved in nearby chiseled
  bookshelves, several at a time, paid in shards, and keeps the books.
- Datapack content types: skill_category, skill_node, quest, shop_offer, feat and crate.
- The wandering trader buys raw materials from you for Skill Shards, keeping vanilla's no-restock rule.
- Iron blocks smelt into Steel in a blast furnace as well as a furnace.
- Gear balance is fully configurable, and existing gear is brought onto retuned numbers on login.

Changed
- Experience is removed from the game. Anvils charge Skill Shards instead of levels, and your banked
  shards are shown on the experience bar.
- The Unstable Skill Shard is a written book; the shard blocks are reinforced deepslate and lodestone,
  taken over outright. Ancient cities generate obsidian instead, and lodestone's recipe is gone.
- Steel armour costs a flat -10% movement on the full set; Hardwood rises to +16%.
- Crystalline grants Strength and Resistance I on top of its 25% melee reflect. Rose Gold gains fire
  resistance and can mine gold and iron.
- The Ender Dragon only drops scales to a player kill, with a one-time bonus for the world's first.

Removed
- /skill open, /skill guide, /skill editor, /skill layout, /skill edit and /skill regen.
- /skill points is now /skill skillshards.

Translation
- English and Traditional Chinese are both complete at 710 keys.
```

Full detail: <https://github.com/Andrewwwwwwwwwwwwwww/vanillaskills/blob/master/CHANGELOG.md>
Documentation: <https://andrewwwwwwwwwwwwwww.github.io/modhub/mods/vanillaskills/>

---

## After upload

CurseForge issues a new fileID once the files are approved. Two things then need it:

1. **The SBSMP modpack manifests** (`projects/CF Pack Upload/SBSMP-Player.zip` and `-Server.zip`) pin
   VanillaSkills at fileID **`8491464`**, which is the 1.7.6 file. Repoint both to the 2.0.0 fileID and
   bump the pack version.
2. Any other pack that lists this mod.

**Known modpack bug to fix in the same pass:** both SBSMP zips ship
`overrides/config/vanillaskills/gameplay.json` and `points.json`. Both files are read from
`<world>/vanillaskills/`, **not** `config/`, so those overrides have never applied. They belong in the
world folder, or the settings should be applied to the server's world directly. The packs also carry a
stale `skilltree.preregen-*.bak` that should not ship.
