# VanillaSkills Resource Pack — Texture Progress

Working pack (populated copy of `../texturepack-template/`). Item icons **16×16**, worn-armor layers
**64×32**, spear in-hand textures **32×32**.

- Item icons: `textures/item/<name>.png`
- Worn armor: `textures/entity/equipment/humanoid/<tier>.png` (helmet/chest/boots) +
  `.../humanoid_leggings/<tier>.png` (leggings)
- Spears (vanilla-style 2-part): GUI icon (16×16) = `<tier>_spear.png`; 3D in-hand (32×32) =
  `<tier>_spear_in_hand.png`. Item override on the base spear selects by `display_context`.
- Tiers: `hardwood`, `rose_gold`, `steel`, `crystal`, `dragon`

## STATUS: essentially COMPLETE (57/58 icons · 10/10 worn)

### Worn armor — 10/10 ✅
- [x] hardwood · [x] rose_gold · [x] steel · [x] crystal · [x] dragon  (humanoid + leggings each)
- Pack ships `equipment/<tier>.json` for all 5 tiers (override the mod jar's base-material fallback).

### Item icons — 57/58
All armor (4×5), all tools incl. spears (6×5), all ingots/materials, dragon scale+template, steel_shield.
- [x] Dragon (13) · [x] Hardwood (10) · [x] Rose Gold (11) · [x] Steel (12) · [x] Crystalline (11)
- [ ] **fortune_template** — NOT yet supplied. Wiring removed for now so the Fortune Upgrade template
  shows as a plain echo_shard (not magenta). Drop `fortune_template.png` (16×16) in `textures/item/`
  and restore `minecraft/items/echo_shard.json` + `models/item/fortune_template.json` from
  `../texturepack-template/` to finish.

## Naming notes (for future batches)
- `rosegold` → tier id `rose_gold`; `crystalline` → tier id `crystal`; `crystalline_ingot` →
  `crystallized_diamond.png`; `dragon_upgrade` → `dragon_template.png`.
- Spear files: `<tier>_spear_icon.png` (16×16) → `<tier>_spear.png`; `<tier>_spear.png` (32×32) →
  `<tier>_spear_in_hand.png`.
- Spear base items: hardwood→stone, rose_gold→golden, steel→iron, crystal→diamond, dragon→netherite.

## steel_shield — vanilla 3D held + flat steel icon (FINAL, user decision 2026-06-23)
- A resource pack CANNOT match vanilla's hardcoded `minecraft:shield` special renderer via a custom
  model (endless display-transform tuning, never converged). User chose: **held = real vanilla shield,
  inventory = steel icon.**
- `minecraft/items/shield.json`: select on custom_model_data; for `vanillaskills:steel_shield`, a nested
  select on `display_context` → the four hand contexts (first/third person L/R) render the FULL vanilla
  shield (condition→`minecraft:special` shield, with blocking animation, wooden); all other contexts
  (gui/ground/fixed/...) → `vanillaskills:item/steel_shield` (flat `generated` model). Normal-shield
  fallback = full vanilla shield (so all other shields render correctly — this also fixed the earlier
  blank-shield regression).
- `textures/item/steel_shield_icon.png` (24×24) = the shield front face cropped from the 64×64 UV
  texture `steel_shield.png` (front face was at px 1,1 size 12×22), centered for a clean flat icon.
- The old custom 3D box model is gone; `steel_shield.json` is now just the flat generated icon.
- NET: steel shield shows a steel icon in inventory; when held it's a perfect 3D shield but wood-colored.
  Achieving a steel-colored 3D held shield would need a client-side mod (breaks vanilla-client support).
