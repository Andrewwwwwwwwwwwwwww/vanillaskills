# CurseForge upload — VanillaSkills 1.7.1 (+ NeoForge, + texture pack)

Three uploads across two CurseForge projects.

## 1. Fabric jar → **Vanilla-Skills** (projectID `1570558`, slug `vanilla-skills`)
**File:** `vanillaskills-1.7.1+mc26.2.jar`
- Game version **26.2**, loader **Fabric**
- Required dependency: **Fabric API** (projectID `306612`)
- Release type: Release
- Environment: server-side; vanilla clients supported

## 2. NeoForge jar → **same project** (`1570558`)
**File:** `vanillaskills-1.7.1+mc26.2-neoforge.jar`
(in `../../vanillaskills-neoforge/curseforge-upload/`)
- Game version **26.2**, loader **NeoForge**
- Release type: Release

## 3. Texture pack → **VSTP - Vanilla-Skills** (projectID `1585850`)
**File:** `VanillaSkills-TexturePack.zip`
- Game version **26.2** (pack format also covers 26.1.2)
- Release type: Release

> **The pack changed in 1.7.1 and must be re-uploaded together with the jar.**
> It now contains the language files (`assets/vanillaskills/lang/en_us.json`, `zh_tw.json`).
> Item names and tooltips are rendered by the *client*, so they come from the pack — without
> this update, translated gear names will silently stay English.
>
> If you self-host the pack for auto-push, update the server config:
> ```
> resourcePackSha1 = 4860cacdfe655949bd9a69d065174077829c4782
> ```

## MC 26.1.2 backport (optional, separate file)
`../../_archive-26.1.2/vanillaskills-mc26.1.2/curseforge-upload/vanillaskills-1.6.4+mc26.1.2.jar`
- Same project `1570558`, game version **26.1.2**, loader **Fabric**
- Currently at **1.6.4** — behind the 26.2 line (1.7.1). It does **not** include the full
  translation work. Only upload if you want the older branch refreshed.

## After upload
The modpack manifests reference vanillaskills by fileID (last known: `8384169` = 1.2.8).
Once CF approves the new file, send the new fileID so the SBSMP Player/Server pack manifests
can be repointed.
