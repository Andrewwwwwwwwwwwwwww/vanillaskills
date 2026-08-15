# CurseForge upload — VanillaSkills 1.7.6 (4 jars + texture pack)

All 4 jars are byte-verified against the hosted GitHub release (v1.7.6) before being staged here —
each was re-downloaded fresh and checked (SkillMenuExtensions class present, no stale copies).

## 1. Fabric jar (26.2) → **Vanilla-Skills** (projectID `1570558`, slug `vanilla-skills`)
**File:** `vanillaskills-1.7.6+mc26.2.jar` (in this folder)
- Game version **26.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 2. NeoForge jar (26.2) → same project (`1570558`)
**File:** `vanillaskills-1.7.6+mc26.2-neoforge.jar`
(in `../../vanillaskills-neoforge/curseforge-upload/`)
- Game version **26.2**, loader **NeoForge**
- Release type: Release

## 3. Fabric jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-1.7.6+mc26.1.2.jar`
(in `../../../26.1.2/vanillaskills/curseforge-upload/`)
- Game version **26.1.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release

## 4. NeoForge jar (26.1.2) → same project (`1570558`)
**File:** `vanillaskills-1.7.6+mc26.1.2-neoforge.jar`
(in `../../../26.1.2/vanillaskills-neoforge/curseforge-upload/`)
- Game version **26.1.2**, loader **NeoForge**
- Release type: Release

## 5. Texture pack → **VSTP - Vanilla-Skills** (projectID `1585850`)
**File:** `VanillaSkills-TexturePack.zip`
- Game version tag: 26.2 (pack format range also covers 26.1.2)
- Release type: Release
- SHA-1: `bb0b9b587593e5335906bc7d3a9c54199eec781e`

> **What changed in 1.7.6:** a new **Crystallized Diamond** texture (user-supplied art). The pack
> must be re-uploaded alongside the jars — item textures are rendered client-side from the pack on
> vanilla clients, so without this update players would keep seeing the old diamond regardless of
> the jar version. All 4 jars have this pack's URL + SHA-1 baked in as the auto-push default, and
> auto-migrate any server still pinned to the v1.7.5 pack.

## New in 1.7.6: `SkillMenuExtensions` API
This release adds a public extension hook (`api/SkillMenuExtensions`) that lets an add-on register a
button on the skill-tree home screen. It's inert on its own — nothing changes for players unless an
add-on is installed. **VanillaSkills Casino requires this version or newer.**

## Changelog to paste
```
### Changed
- New Crystallized Diamond texture.
```

## After upload
Once CF approves the new files, update the modpack manifest fileIDs (last known: `8384169`, which
was already stale at 1.2.8 — CF is presently at 1.6.4). Send the new fileID(s) so SBSMP/VSP pack
manifests can be repointed.
