# VanillaSkills — Skill Tree Design (v1)

Decisions locked in: **milestone/advancement** point economy, **JSON-first** editing
(in-game GUI editor comes later), **small starter tree** to prove the system.

---

## 1. Point economy — milestones / advancements

Players earn skill points by completing advancements. Detected server-side by a mixin
on `PlayerAdvancements#award` (fires when a criterion completes an advancement).

Config: `config/vanillaskills/points.json`

```json
{
  "perAdvancement": 1,
  "ignoreRecipeAdvancements": true,
  "advancementOverrides": {
    "minecraft:story/mine_diamond": 3,
    "minecraft:nether/obtain_blaze_rod": 2,
    "minecraft:end/kill_dragon": 10,
    "minecraft:adventure/kill_a_mob": 1
  },
  "startingPoints": 0
}
```

- `perAdvancement` — default points for any advancement not in the override map.
- `ignoreRecipeAdvancements` — skip the auto-granted `minecraft:recipes/...` entries
  (otherwise unlocking recipes would spam points).
- `advancementOverrides` — per-advancement custom values; set to `0` to grant nothing.
- `startingPoints` — points every player begins with.

Retroactive grant: on first join after install (or `/skill recalc`), already-earned
advancements are tallied so existing players aren't penalized.

---

## 2. Per-player data

`world/vanillaskills/players/<uuid>.json`

```json
{
  "version": 1,
  "unlocked": ["root", "vitality_1", "mobility_1"],
  "pointsAvailable": 4,
  "pointsEarned": 7,
  "creditedAdvancements": ["minecraft:story/mine_diamond", "..."]
}
```

`creditedAdvancements` prevents double-paying if config changes or data reloads.
Attribute modifiers from unlocked nodes are (re)applied on join and on each unlock,
keyed by a stable UUID derived from the node id so they never stack or duplicate.

---

## 3. Tree definition (the importable file)

`config/vanillaskills/skilltree.json` — hand-editable, loaded on server start,
reloadable with `/skill reload`. This is the file an admin designs once and ships to
a server; every player gets this tree.

```json
{
  "version": 1,
  "title": "Skills",
  "rows": 6,
  "nodes": [
    {
      "id": "vitality_1",
      "title": "Vitality I",
      "description": ["+2 max health"],
      "icon": "minecraft:apple",
      "cost": 1,
      "requires": ["root"],
      "slot": 37,
      "effects": [
        { "type": "attribute", "attribute": "minecraft:max_health",
          "operation": "add_value", "amount": 2.0 }
      ]
    }
  ]
}
```

### Node fields
| field | meaning |
|-------|---------|
| `id` | unique key, used in player save + modifier UUID |
| `title` | display name on the item |
| `description` | lore lines (array) |
| `icon` | item id shown in the GUI |
| `cost` | skill points to unlock |
| `requires` | node ids that must be unlocked first (AND). Empty/omitted = always available |
| `slot` | chest slot 0–53 = visual position |
| `effects` | list of effects applied while unlocked |

### Effect types (v1)
- `attribute` — `{ attribute, operation, amount }`
  operations: `add_value`, `add_multiplied_base`, `add_multiplied_total`.
- `status_effect` — `{ effect, amplifier }` permanent hidden effect (e.g. regeneration,
  night_vision). Reapplied on a short timer.
- `flag` — `{ name }` named boolean for custom logic hooked later (e.g. `piglin_friendly`).

The `root` node: `cost 0`, no `requires`, auto-unlocked on first join — the anchor
everything branches from.

---

## 4. Starter tree (13 nodes, 4 lanes)

6×9 chest. Slot = `row*9 + col`. Root center-bottom, four lanes rising from it.

```
        col0 col1 col2 col3 col4 col5 col6 col7 col8
row0      .    .    .    .    .    .    .    .    .
row1      .    .    .    .    .    .    .    .    .
row2      .    V3   .    M3   .    P3   .    C3   .
row3      .    V2   .    M2   .    P2   .    C2   .
row4      .    V1   .    M1   .    P1   .    C1   .
row5      .    .    .    .   ROOT  .    .    .    .
```

| id | title | slot | cost | requires | effect |
|----|-------|------|------|----------|--------|
| `root` | Awakening | 49 | 0 | — | none (anchor) |
| `vitality_1` | Vitality I | 37 | 1 | root | +2 max_health |
| `vitality_2` | Vitality II | 28 | 2 | vitality_1 | +2 max_health (4 total) |
| `vitality_3` | Natural Toughness | 19 | 3 | vitality_2 | +2 armor_toughness |
| `mobility_1` | Fleet Foot I | 39 | 1 | root | +5% movement_speed |
| `mobility_2` | Fleet Foot II | 30 | 2 | mobility_1 | +5% movement_speed (10% total) |
| `mobility_3` | Sure Step | 21 | 3 | mobility_2 | +0.5 step_height, +3 safe_fall_distance |
| `mining_1` | Prospector I | 41 | 1 | root | +1 mining_efficiency |
| `mining_2` | Prospector II | 32 | 2 | mining_1 | +2 mining_efficiency (3 total) |
| `mining_3` | Deep Digger | 23 | 3 | mining_2 | +0.5 block_interaction_range, +submerged mining |
| `combat_1` | Warrior I | 43 | 1 | root | +1 attack_damage |
| `combat_2` | Warrior II | 34 | 2 | combat_1 | +1 attack_damage (2 total) |
| `combat_3` | Brawler | 25 | 3 | combat_2 | +2 attack_damage (4 total) |

Full clear = 24 points across 4 lanes. Numbers live in the JSON, so balancing is a
config edit, not a recompile.

GUI item states (via item + lore color):
- **Owned** — enchanted-glint item, green "Unlocked".
- **Available** — normal item, yellow "Click to unlock — cost N".
- **Locked** (prereqs unmet) — gray-dyed/barrier, red "Requires: …".
- **Unaffordable** — available styling, red cost line.

Bottom row shows a points counter (e.g. a tipped item in slot 53: "Points: 4").

---

## 5. Commands

| command | perm | effect |
|---------|------|--------|
| `/skill` or `/skills` | all | open the tree GUI |
| `/skill reload` | op | reload `skilltree.json` + `points.json` |
| `/skill points <player> add\|set <n>` | op | adjust points |
| `/skill reset <player>` | op | refund/clear unlocks |
| `/skill recalc <player>` | op | re-tally advancements into points |

Opening the GUI from the survival inventory (without a command) needs a client-side
hook, which a server-only mod can't do. So **v1 uses `/skill`**; an inventory-button
opener would require the optional client companion mod discussed earlier.

---

## 6. Implementation build order

1. Config loading (`skilltree.json`, `points.json`) + tree model classes.
2. Per-player data store (load/save, join handling).
3. Attribute/effect application engine (apply on join + unlock, keyed modifiers).
4. Advancement mixin → point granting + retroactive tally.
5. Chest GUI (render nodes, click-to-unlock, points counter).
6. Commands.
7. Ship the starter tree JSON as a bundled default.

Editor GUI and additional effect types come in a later milestone.
