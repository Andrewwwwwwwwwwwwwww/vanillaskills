# VanillaSkills Resource Pack — Texture Progress

Working pack (populated copy of `../texturepack-template/`). Drop PNGs into
`assets/vanillaskills/textures/`. Item icons are **16×16**, worn-armor layers are **64×32**.

- Item icons go in `textures/item/<name>.png`
- Worn armor goes in `textures/entity/equipment/humanoid/<tier>.png` (layer 1: helmet/chest/boots)
  and `textures/entity/equipment/humanoid_leggings/<tier>.png` (layer 2: leggings)
- Tiers: `hardwood`, `rose_gold`, `steel`, `crystal`, `dragon`

Legend: [x] done · [ ] needed

## Worn armor (10 textures = 5 tiers × 2 layers)
- [x] hardwood (humanoid + humanoid_leggings)
- [x] rose_gold (humanoid + humanoid_leggings)
- [x] steel (humanoid + humanoid_leggings)
- [ ] crystal (humanoid + humanoid_leggings)
- [x] dragon (humanoid + humanoid_leggings)

(Pack equipment JSONs present for the 4 tiers with worn art: dragon, hardwood, rose_gold, steel.
crystal omitted until its worn texture exists — mod jar falls back to base-material look.)

## Item icons (58 total)

### Dragon — 12 of 13
- [x] dragon_helmet
- [x] dragon_chestplate
- [x] dragon_leggings
- [x] dragon_boots
- [x] dragon_scale
- [x] dragon_template  *(zip's `dragon_upgrade.png`)*
- [x] dragon_ingot
- [x] dragon_sword
- [x] dragon_pickaxe
- [x] dragon_axe
- [x] dragon_shovel
- [x] dragon_hoe
- [ ] dragon_spear  *(not yet supplied)*

### Hardwood — 4 of 10
- [x] hardwood_helmet
- [x] hardwood_chestplate
- [x] hardwood_leggings
- [x] hardwood_boots
- [ ] hardwood_sword
- [ ] hardwood_pickaxe
- [ ] hardwood_axe
- [ ] hardwood_shovel
- [ ] hardwood_hoe
- [ ] hardwood_spear

### Rose Gold — 0 of 11
- [ ] rose_gold_helmet
- [ ] rose_gold_chestplate
- [ ] rose_gold_leggings
- [ ] rose_gold_boots
- [ ] rose_gold_ingot
- [ ] rose_gold_sword
- [ ] rose_gold_pickaxe
- [ ] rose_gold_axe
- [ ] rose_gold_shovel
- [ ] rose_gold_hoe
- [ ] rose_gold_spear

### Steel — 0 of 12
- [ ] steel_helmet
- [ ] steel_chestplate
- [ ] steel_leggings
- [ ] steel_boots
- [ ] steel_ingot
- [ ] steel_shield  *(icon only — shield model is special)*
- [ ] steel_sword
- [ ] steel_pickaxe
- [ ] steel_axe
- [ ] steel_shovel
- [ ] steel_hoe
- [ ] steel_spear

### Crystalline — 0 of 11
- [ ] crystal_helmet
- [ ] crystal_chestplate
- [ ] crystal_leggings
- [ ] crystal_boots
- [ ] crystallized_diamond
- [ ] crystal_sword
- [ ] crystal_pickaxe
- [ ] crystal_axe
- [ ] crystal_shovel
- [ ] crystal_hoe
- [ ] crystal_spear

### Materials / misc — 0 of 1
- [ ] fortune_template  *(echo_shard base)*

---
**Totals:** item icons 16/58 · worn armor 8/10. Worn armor done for hardwood/rose_gold/steel/dragon
(crystal worn pending). Dragon items COMPLETE except dragon_spear. Still need: dragon_spear, crystal
worn, Hardwood/Rose Gold/Steel/Crystalline tools+ingots+materials, Rose Gold/Steel/Crystalline armor icons.
