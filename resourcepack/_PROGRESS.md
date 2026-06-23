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

## steel_shield — custom 3D model (done)
- `models/item/steel_shield.json` is a custom 3D box model replicating the vanilla shield geometry
  (plate 12×22×1 @ texOffs 0,0; handle 2×6×6 @ texOffs 26,0; 64×64) so the artist's shield-UV texture
  maps correctly. Wired per-item via `minecraft/items/shield.json` (custom_model_data steel_shield →
  this model; fallback preserves the full vanilla `minecraft:special` shield renderer so normal
  shields still work). First-person display tuned to match vanilla footprint (scale 0.42).
