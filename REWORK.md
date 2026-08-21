# VanillaSkills 2.0 — Rework Specification

Working document for the full VanillaSkills rework started 2026-07-30, against current release **1.7.6**.

Facts in this document are marked **[verified]** when confirmed against the 26.2 remapped Minecraft jar,
the Fabric API jars, the vanilla 26.2 client jar, or the VanillaSkills source itself. Anything not so
marked is a design intent or an assumption, and unresolved forks are collected in §6.

---

## 1. Locked decisions

| # | Decision | Consequence |
|---|---|---|
| 1 | **Vanilla-client compatibility is non-negotiable.** No registered items or blocks. | Custom content stays component-stamped on vanilla bases. See §2.1. |
| 2 | **Datapacks own the content**: skill tree, quests, shop, feats, gear tiers, crates. | New `SERVER_DATA` reload listener; content moves out of Java constants. |
| 3 | **The skill tree is pack-owned outright.** `/skill editor` and `/skill layout` are **removed entirely**. | `skilltree.json` stops being mutable per-world state. |
| 4 | **Packs author relative *weights*, not absolute costs.** `applyEconomy` still rescales to the earnable budget P. | Tree self-balances; a pack cannot break the economy. Night Vision's flat 75 + P/3 gate and the Quest-Shard lanes stay exempt. |
| 5 | **Player save data gets a real migration**, not a wipe. | Quest progress must move from integer indices to string ids first. |
| 6 | **Experience is removed from the game entirely.** | Anvils now cost **Skill Shards**. See §3.1. |
| 7 | **Skill Shards become withdrawable as physical items.** | New USS → USSB → SSSB chain. See §3.2. |

---

## 2. Verified technical findings

### 2.1 Why no registered items or blocks

**[verified — Fabric API 26.2 bytecode]** `RegistrySyncManager.configureClient` checks
`ServerConfigurationNetworking.canSend(...)`. When the client cannot receive the registry-sync payload and
not every registry is `OPTIONAL`, it disconnects them with a message built from the literals *"This server
requires Fabric Loader and Fabric API installed on your client!"*.

Registering any item or block therefore kicks vanilla clients at login. This is the constraint that shapes
everything else. **Do not re-propose registered items or blocks.**

### 2.2 Custom items — the component pattern

Already how VanillaSkills works: a vanilla base item stamped with a `vs_*` `custom_data` marker plus
overriding components. Three upgrades available, all confirmed in production by the reference pack (§2.5):

- **`DataComponents.ITEM_MODEL`** **[verified — exists in 26.2 as `DataComponentType<Identifier>`]**, and
  VanillaSkills does not use it. Replacing `CUSTOM_MODEL_DATA` + the 57 `assets/minecraft/items/*.json`
  `select` overrides with `ITEM_MODEL` pointing at our own namespace deletes the **root cause of two shipped
  bugs** — the 0.19.15 white-undyed-leather regression (our override dropped vanilla's dye tints) and the
  1.0.2 armour-trim-icon wipe (our override replaced vanilla's `trim_material` select) — and removes the
  hazard where an add-on pack touching one of those 57 files silently wipes our gear textures.
- **`DataComponents.ITEM_NAME`** **[verified — exists in 26.2]** is the correct component for an item's
  intrinsic name. VanillaSkills uses `CUSTOM_NAME` everywhere with a `withItalic(false)` workaround in
  `Markers.name()`. `ITEM_NAME` avoids the italics and stops the anvil treating gear as player-renamed.
- **`DataComponents.TOOL`** **[verified — `Tool` record with `rules()`, `defaultMiningSpeed()`,
  `damagePerBlock()`, `isCorrectForDrops()`]**. `ToolTiers.java`'s comment that *"each tier is a vanilla tool
  base, so its harvest tier matches that base"* describes an **implementation limit, not an approach limit**.
  VanillaSkills never sets `TOOL`. Setting it gives any tier arbitrary mining speed and harvest capability.
  Also available: `WEAPON`, `BLOCKS_ATTACKS`, `PIERCING_WEAPON`, `KINETIC_WEAPON`.

**No ceiling on tier count.** Multiple tiers can share one base item — `diamond_chestplate` already hosts
both vanilla diamond and Crystalline. Markers separate them logically, `ITEM_MODEL` separates them visually.

### 2.3 Custom blocks — the marker-entity pattern

Custom blocks **are** achievable server-side. The reference pack's "Warding Stone" is a complete, shipped,
mod-free example:

1. The **item** is a `minecraft:chicken_spawn_egg` carrying a `minecraft:entity_data` component describing an
   invisible, small, invulnerable, no-gravity **armour stand tagged `WardingStone`**, plus `item_model` and
   `item_name`. Right-clicking it spawns the tagged marker.
2. The **marker entity acts as the block entity**, carrying identity and state.
3. A ticking function runs `setblock ~ ~ ~ minecraft:lodestone` on first tick — **a vanilla block provides the
   physical presence**. Behaviour runs via `execute at @e[tag=...]`.
4. Break detection is `execute at @e[tag=...Setup] run execute unless block ~ ~ ~ minecraft:lodestone run
   function ...warding_stone_killer`.

VanillaSkills is a mod, so it can skip the armour stands and mcfunctions and track placed positions in its
own world data, ticking them directly on the server.

**The one real caveat:** the placed block looks like whichever vanilla block backs it — the reference pack
simply accepts a plain lodestone. Per-placement custom art requires either globally retexturing a sacrificial
vanilla block, or **blockstate multiplexing** (note blocks and mushroom blocks have many blockstate variants a
resource pack can map to individual models — the standard technique for visually-custom blocks on vanilla
servers). **Unresolved — see §6.1.**

### 2.4 Recipes

- **`Ingredient` cannot match components.** **[verified — in 26.2 it is a bare `HolderSet<Item>` whose codec is
  `NON_AIR_HOLDER_SET_CODEC`]**. Confirmed empirically too: **not one recipe in the entire 460-recipe reference
  pack matches components on `ingredients`, `base`, `addition` or `template`** — every input is a plain item id.
  Custom items can only ever be *outputs* in pure-datapack recipes. This is the hard wall on what a companion
  datapack can do, and the reason `ArmorCraftingRecipe`/`ToolCraftingRecipe` are hand-written `CustomRecipe`s.
- **Fabric offers an escape hatch for our own recipes:**
  `DefaultCustomIngredients.customData(Ingredient, CompoundTag)` **[verified — exists in fabric-recipe-api-v1]**
  matches exactly our `vs_*` marker. Client support is negotiated via `fabric:supported_custom_ingredients` with
  a `toVanilla()` fallback for clients that do not reply. **Not runtime-tested against a real vanilla client,
  and Fabric-specific — NeoForge has its own mechanism, so it would be an edition delta.** Test before designing
  around it.
- **All custom recipes can appear in the vanilla recipe book.** **[verified — `Recipe.display()` returns
  `List<RecipeDisplay>`; `SlotDisplay$ItemStackSlotDisplay`, `ShapedCraftingRecipeDisplay`,
  `ShapelessCraftingRecipeDisplay` and `SmithingRecipeDisplay` all exist, and `SlotDisplay` resolves to full
  `ItemStack`s with components]**. A `CustomRecipe` can therefore publish a real book entry whose grid cells are
  marked Steel Ingots rendering with their own texture. This **partially reverses the 1.0.3 decision** that the
  recipe book was impossible: display and discovery work, **autofill still does not** (that needs
  `placementInfo()`, which is item-keyed). VanillaSkills' own `RecipeBookMenu` becomes redundant except for the
  anvil-forged entries.

### 2.5 Reference pack

**"Klei's Matcha Flavoured" 1.04** — a combined data + resource pack whose stated goal is *"forever-vanilla
compatible"*, the same constraint as VanillaSkills, reaching **299 custom items with zero mod code**. Best
available reference. `CREDITS.txt` states the author's own textures and data are free to reuse.

Component usage across its recipes: `item_name` 206, `attribute_modifiers` 198, `item_model` 195,
`max_damage` 135, `tooltip_display` 134, `consumable` 119, `food` 111, `enchantments` 77, `repairable` 55,
**`tool` 52**, `use_remainder` 27, `equippable` 22, `blocks_attacks` 3, **`custom_name` only 4**. Zero vanilla
item-model overrides.

Other techniques worth borrowing:

- **Datapack enchantments granting attributes** — their `reach.json` grants `block_interaction_range` via a
  `minecraft:attributes` effect with `minecraft:linear` per-level scaling, `slots`, `supported_items` by tag,
  `anvil_cost` and `weight`. They ship 20. A second, fully vanilla-safe delivery mechanism for the kind of perk
  our skill lanes grant. Directly relevant to the Unboxing enchantment (§3.8).
- **`pack.mcmeta` `filter` blocks** vanilla content declaratively by path — they delete vanilla advancements and
  specific recipes with no mixin.
- **advancement → `rewards.function` → `advancement revoke @s only <id>`** is the datapack event loop. We do not
  need it, but it matters for API design: exposing VanillaSkills events as advancement triggers lets pack authors
  react to them.
- **Feature-per-data-namespace organisation** (`data/crafting/`, `data/blessings/`, `data/food/`, …) so a pack can
  filter or replace exactly one system.
- **Custom lang keys live in `assets/minecraft/lang/en_us.json`** under an invented prefix
  (`item.kleispack.adamant_claymore`) — how a pack with no mod namespace ships translatable names.

### 2.6 Placeholder art

Ship the model JSON pointing at a texture path that does not exist yet; the client renders the magenta/black
missing-texture checkerboard automatically. Dropping the PNG in later needs no JSON edits.

⚠ The auto-pushed resource pack **shadows jar assets even on modded clients**, so every art change still requires
rebuilding the pack, re-hosting it, and re-baking `DEFAULT_RP_SHA1` (adding the old URL to
`SUPERSEDED_RP_URLS`). That ritual does not go away.

---

## 3. Feature specification

### 3.1 Remove experience entirely

Nothing drops XP: blocks, spawners, mobs, villager trades, furnaces, breeding, fishing, bottles o' enchanting.

**Consequences that must be handled:**

- **Anvils become unpayable.** Every anvil operation costs levels. **Decision: anvil operations cost Skill
  Shards.** Affects Steel Ingot forging, Dragon repair (currently a flat 20 levels via `AnvilMenuMixin`), and
  the new Steel Shield forge. The existing `anvilTooExpensiveCap` config and the `@ModifyConstant` that removes
  the vanilla 40-level cap both become meaningless and should be retired.
  ⚠ Repairs now compete with the skill tree for the same currency — **re-price Dragon repair against the balance
  workbook** rather than carrying 20 across.
- **Remove `minecraft:experience_bottle` from the Quest Shop catalog** (two offers: ×8 for 4, ×16 for 7).
- **Advancement impact is narrow.** **[verified against the vanilla 26.2 client jar — 126 non-recipe
  advancements]**:
  - **Hard-blocked, exactly one:** `minecraft:story/enchant_item` ("Enchanter"). Its sole criterion is the
    `minecraft:enchanted_item` trigger, which only an enchanting table fires. It declares no `frame`, so it is a
    TASK worth **2 Skill Shards** under `PointsConfig` defaults. **It has no children**, so nothing is orphaned.
    *Mitigation: have the Infusing Table fire the `enchanted_item` trigger — one line, keeps it earnable.*
  - **At risk but survivable:** `husbandry/silk_touch_nest` needs a Silk-Touch tool. Silk Touch remains obtainable
    as a book from loot and librarian trades, so it survives as long as book enchantments can be applied — which
    is exactly what the Infusing Table does.
  - **No advancement requires XP levels.**
- **The economy risk here is small.** The scaled tree totals ~2115 against P = 2126, leaving ~11 Skill Shards of
  rounding slack, and `convertToSkillShards` calls `grantPoints`, which raises `pointsEarned` too — so **Quest
  Shards convert to Skill Shards 3:1 with no cap. P is a floor on lifetime earnings, not a ceiling.** The tree
  cannot become unbuyable and the Night Vision `minEarned` gate at P/3 stays satisfiable.

**Chokepoints [verified — 26.2 javap]**, and there are pleasingly few:

| Target | Signature | Covers |
|---|---|---|
| `ExperienceOrb.award` | `static void award(ServerLevel, Vec3, int)` | the main orb-spawn funnel |
| `ExperienceOrb.awardWithDirection` | `static void awardWithDirection(ServerLevel, Vec3, Vec3, int)` | directional spawns — check whether it delegates to `award`, in which case one inject covers both |
| `Player.giveExperiencePoints` | `void giveExperiencePoints(int)` | direct grants (furnace pickup, bottles) |
| `Player.giveExperienceLevels` | `void giveExperienceLevels(int)` | direct level grants |
| `MerchantOffer` | `boolean shouldRewardExp()` / `int getXp()` | villager trade XP |

Suppress both the grant and the orb spawn, so there is no visual litter.

### 3.2 Skill Shards as physical items

**Unstable Skill Shard (USS)** — a withdrawable physical form of a Skill Shard.

- **Withdraw button** on the skill-tree home screen, directly **above the bottom-left shard counter**
  (`POINTS_SLOT = 45`, so slot **36**).
- **Two-click confirmation** on withdrawal, so nobody converts their bank by accident.
- **Right-click a USS in hand to deposit it back** into the skill-tree bank.
- Obtainable additionally from: **rare chest loot**, **piglin bartering** (both low chance), **crates** (§3.8),
  and the shard ore below.

**Unstable Skill Shard Block (USSB)** — 9 USS in a crafting table.

- **Dropped by spawners when broken**, replacing their XP.
- **Generates rarely in all three dimensions.** Mineable only with **Crystalline or better**, enforced like the
  existing `DeepslateGate`. All three are deliberately *very* small chances:

  | Dimension | Where |
  |---|---|
  | Overworld | Y −10 to Y 10 |
  | Nether | Y 15 and below |
  | The End | anywhere **outside the spawn island** |

  *Implementation note for the End:* gate on biome rather than distance — the central spawn island is the
  `minecraft:the_end` biome, while the outer islands are `end_highlands` / `end_midlands` / `end_barrens` /
  `small_end_islands`. Excluding the central biome is both cheaper and more accurate than a radius check.

**Stable Skill Shard Block (SSSB)** — 1 USSB + 2 tinted glass + 4 redstone.
*Assumed layout: redstone in the four corners, tinted glass top and bottom middle, USSB centre.*

Placed behaviour:

- Damages hostile mobs in a **7×7×7** area.
- **Immune to creeper explosions.**
- **Right-click a placed SSSB with another SSSB** to merge its 7×7×7 into the existing one, expanding the area
  without extra placements. **Stacks up to 4** inside one placed SSSB.
- **Usable as a beacon base**, where it grants **triple range** (50 → 150 block radius) and **double effect
  output** — the selected effect applied at double strength.

Implementation follows §2.3: a chosen vanilla base block for physical presence, with VanillaSkills tracking
placed positions, merge counts and beacon interaction in its own world data.
**[verified — `BlockTags.BEACON_BASE_BLOCKS` exists]** and can be extended by our datapack; beacon range and
effect amplification need a mixin on the beacon block entity.

### 3.3 Datapack content system

New `SERVER_DATA` reload listener reading `data/<namespace>/vanillaskills/<type>/*.json`.
**[verified]** Fabric: `ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(...)` with
`SimpleSynchronousResourceReloadListener` (= `ResourceManagerReloadListener` + `getFabricId()`);
`FileToIdConverter.json(prefix).listMatchingResourceStacks(rm)` is the canonical scan (vanilla's `TagLoader`
uses exactly it) and `Resource.openAsReader()` reads each file.

NeoForge 26.2 uses **`AddServerReloadListenersEvent`** — *not* `AddReloadListenerEvent`, which no longer
exists **[verified — the NeoForge 26.2.0.7-beta jar ships `AddServerReloadListenersEvent extends
SortedReloadListenerEvent`, with `addListener(Identifier, PreparableReloadListener)` and
`addRetainedListener(ListenerKey<T>, T)`]**. The listener itself can be a plain vanilla
`ResourceManagerReloadListener`, so only the registration differs.

**Predicted to become an 8th divergent file — it did not.** The registration fits inside
`VanillaSkills.java`, which already differs per loader, and the loader logic itself is edition-agnostic
(it takes a `ResourceManager`). Divergence stays at the original 7 files. See §7.

Content types: `skill_lane` (lane metadata with its nodes nested), `quest`, `shop_offer`, `feat`, `gear_tier`,
`crate`.

**As built (2026-08-14):** `feat`, `crate`, `skill_category`, `skill_node`, `quest`, `shop_offer`. Lanes and
nodes ended up as **two flat types instead of one nested `skill_lane`** — a node's `category` field points at
its lane — because that keeps every content type a flat id-keyed list, so the tag-style merge and per-entry
override work identically for all of them. `gear_tier` is the one type not yet done.

The two quest boards are a single `quest` type split by a `"pool"` field (`starter` / `rotating`), not two
types: they are the same shape, and a pack moving a quest between boards should be a one-word edit.

File format follows the familiar vanilla **tag style** so one file scales from a single entry to a hundred:

```json
{ "replace": false, "values": [ { "id": "…", "…": "…" } ] }
```

Entries carry explicit `id` fields, so a later pack overrides an earlier one by re-declaring the same id, and
`"replace": true` wipes earlier contributions.

VanillaSkills' own built-in content ships as datapack files in its own jar, so there is a single code path and
packs can override the defaults.

⚠ `SkillTree.index()` auto-creates a lane for any node referencing an unknown category. Forgiving, but a typo'd
pack id currently produces a junk lane instead of an error — the loader should report it.

⚠ The reload listener runs during the initial datapack load, **before `SERVER_STARTED` assigns
`VanillaSkills.server`**. `worldDir()` returns null until then; handle it.

### 3.4 Skill tree

- `/skill editor` and `/skill layout` **removed**, along with the `/skill edit …` command subtree that mutates
  the tree, and `SkillTreeMenu`'s edit and layout modes.
- Tree content comes from the datapack; `skilltree.json` is no longer authored in-game.
- `applyEconomy` retained, now rescaling **pack-authored weights**.

### 3.5 Infusing Table

Replaces the enchanting table, reusing the **vanilla enchanting table block** so no new block is needed.

- Reads enchanted books from **nearby chiseled bookshelves**, in the same layout a vanilla enchanting table uses
  for bookshelves, rather than from plain bookshelves.
- Offers exactly the enchantments present in those books.
- The player may **select multiple enchantments at once**.
- GUI modelled on the *Enchanting Infuser* mod's presentation, built as a server-side chest menu.
- Should fire the `minecraft:enchanted_item` trigger so "Enchanter" stays earnable (§3.1).
- **Cost model is unresolved — see §6.2.**

### 3.6 Gear pass

| Tier | Change |
|---|---|
| **Hardwood** | Verify poison-on-hit actually fires (already implemented in `VanillaSkills.java` for Hardwood-marked stone swords/axes). Raise per-piece movement speed — *assumed 0.025 → 0.04, i.e. +10% → +16% for the set.* |
| **Copper** (vanilla tier) | Can now mine gold. **[verified — `BlockTags.INCORRECT_FOR_COPPER_TOOL` exists]**, so this is a datapack tag override, not a mixin. Note it applies to all copper tools globally. |
| **Copper helmet** | Emits light when worn, ~15 blocks, in the spirit of LambDynamicLights. **Approach unresolved — see §6.3.** |
| **Rose Gold** | Can mine gold and iron — via the per-stack `TOOL` component (§2.2), so no global tag change. Piglins must not be angered: already automatic, since it is golden-armour-based. No negative effects: **already implemented** — `RoseGoldSet.tick` strips all `HARMFUL` effects on the full set. **Add fire resistance.** |
| **Steel** | Ingot now **smelted: 1 iron block → 3 steel ingots** (replaces anvil forging; remove that path from `AnvilMenuMixin`). Armour movement penalty increased to offset its 18 armour — *assumed −0.01 → −0.02 per piece, i.e. −4% → −8% for the set.* |
| **Steel Shield** | Now forged in an **anvil** from a shield + a steel ingot (replaces the 6-ingot table recipe). When **held**, reduces movement speed and gains substantially more durability. |
| **Crystalline** | Crystallized Diamond recipe keeps its shape but uses **USS in the top and bottom middle** slots. Full set now grants **Strength and Resistance I** — *assumed additive alongside the existing 25% melee reflect, not replacing it.* |
| **Dragon** | Must drop **8 scales on death**, and **32 on the world's first player kill only**. Currently drops on *any* dragon death with no player check — **this must become player-kill-gated for THP compatibility**, since THP may kill the dragon once on startup. Needs a world-level persistent first-kill flag. Dragon Ingot recipe reduced to **4 scales + 1 netherite ingot** (from 8 + 1). |

### 3.7 Recipe changes summary

| Recipe | From | To |
|---|---|---|
| Steel Ingot | Anvil: iron + iron | Smelting: 1 iron block → 3 |
| Steel Shield | Table: 6 steel + shield | Anvil: shield + steel ingot |
| Crystallized Diamond | `AAA / DBD / AAA` (6 amethyst shard, 2 diamond, 1 amethyst block) → 2 | Same, with **USS** replacing the top-middle and bottom-middle amethyst shards |
| Dragon Ingot | 8 dragon scales + netherite ingot | **4** scales + netherite ingot |
| Fortune V | lapis / diamond block frame + 2× Fortune IV + template | **4 diamond blocks in the corners, SSSB top and bottom middle, Fortune IV both sides, Fortune Upgrade Template centre** |

All five need matching updates in `RecipeBook`, `GuideBook` and the lang files — those three drift easily and
have caused stale-documentation bugs before.

### 3.8 Crates

Modelled on [svcrates](https://modrinth.com/mod/svcrates) (Fabric, MC 1.21.1).

**What that mod actually does:** crates are caught **while fishing** at a `1 in crateChance` rate, not obtained
from keys or placed blocks. Tiers are Wooden (Common), Copper (Uncommon), Iron (Rare) and Diamond (Super Rare),
with biome-exclusive variants for Desert, Jungle, Frozen, Taiga and Ocean, plus End-only Chorus (Common) and
Purpur (Rare). An **"Unboxing"** fishing-rod enchantment with three levels raises crate drop rates and is itself
found in crates. The system is data-driven.

**Our version:** the same mechanics, with **USS in the crate loot tables**.

Fits existing infrastructure almost entirely:

- Crate **items** use the standard component pattern. Because crates are right-click-opened rather than placed,
  **no custom block is required.**
- **Fishing injection** reuses the loot-table hook already used by `FortuneTemplateLoot` and
  `DragonTemplateLoot` — with the known Fabric/NeoForge split on those two files.
- **Biome-exclusive variants** need no code: loot tables support `minecraft:location_check` with a biome
  predicate.
- **Crate definitions** are a datapack type in §3.3.
- **Unboxing** is a datapack enchantment, exactly as in §2.5.
- An **opening reveal animation**, if wanted, has a proven pattern in this workspace: the casino's slot machine
  drives container slots from a server tick with an idempotent settle, so closing the screen mid-reveal cannot
  strand a decided outcome.

⚠ **Licensing.** The Modrinth license field says **Apache-2.0**, but the project description also states *"Code
reuse, variants, ports, and backports require permission from the developer"* — which directly contradicts it.
Their terms additionally prohibit use in real-money systems. Since VanillaSkills ships All Rights Reserved,
**do not act on the permissive reading: request written permission from the author before using their textures.**
Mechanics and game-design ideas are not copyrightable, so the feature can be built regardless; only the art is
gated. Placeholder art (§2.6) costs nothing while waiting.

⚠ **Ordering:** crates cannot ship before USS exists.

### 3.9 Smaller items

- **Feats on by default** — `GameplayConfig.feats = true`. ⚠ Adds ~188 Quest Shards of previously dead content
  to the economy.
- **Nether roof removed** — players above the nether bedrock roof take damage as if outside the world border.
  **[verified — `DamageTypes.OUTSIDE_BORDER` exists]**, so the damage type is exact. A way to break the roof
  bedrock anyway is explicitly deferred.
- **Horse stats visible in the horse inventory.** **Approach unresolved — see §6.3.**

---

## 4. Phasing

Ordering is driven by real dependencies, not preference.

**Phase 1 — Foundations. ✅ COMPLETE on all four editions (2026-07-30), build-verified.** Datapack loader
(§3.3). Quest index → string-id migration (§5). XP removal and anvil Skill-Shard costs (§3.1).
`ITEM_MODEL` / `ITEM_NAME` component migration and deletion of the 57 vanilla item-model overrides (§2.2).
*Nothing player-facing ships alone here; it is the platform everything else stands on.*
⚠ **Not runtime-tested.** The `TOOL` component is available but per-tier harvest changes are Phase 3 work.

**Phase 2 — Skill Shard economy.** USS withdraw/deposit with two-click confirm, USSB, SSSB and its aura, merge
and beacon behaviour, ore generation, loot and barter injection (§3.2).

**Phase 3 — Gear pass.** All tier changes and the five recipe reworks (§3.6, §3.7). Depends on Phase 2 because
Crystallized Diamond and Fortune V consume USS and SSSB.

**Phase 4 — Infusing Table** (§3.5).

**Phase 5 — Crates** (§3.8). Depends on Phase 2 for USS loot.

**Phase 6 — Smaller items** (§3.9) and whichever of §6.3 is resolved.

Version target: this is a breaking rework with removed commands and changed data formats — **2.0.0**.

---

## 5. Save-data migration

Quest progress is currently stored as **integer indices into `QuestPool.ALL`** in **six** `PlayerSkillData`
fields — `questKills`, `questClaimed`, `questStatBase`, `questStatNotified`, `starterDone` and `starterKills` —
plus `QuestBoard.State.activeIndices`. `QuestPool` is explicitly documented as append-only for exactly this
reason.

Datapack authors add, remove and reorder quests, so **these must become string-keyed before any quest datapack
work is safe.** Migration maps old indices to the ids of the current `QuestPool` entries on first load, behind a
bumped data version, preserving progress.

**Quest ids reuse an existing convention:** `Lang.questKey(title)` already slugs a title into a stable key
(`"Gather 32 Emeralds"` → `gather_32_emeralds`). Using the same slug as the quest id makes ids and lang keys line
up for free. ⚠ Verify no two titles collide across `STARTER` and `ALL` before relying on it.

⚠ **Ordering constraint:** the migration maps old indices through the `QuestPool` ordering *as it exists at
migration time*. **Ship the migration before reordering or datapack-ifying quests** — doing both in one release
would corrupt the mapping.

**Resolved (2026-08-14).** Rather than depending on release ordering, the two pre-2.0 orderings are now frozen
in code as `QuestPool.LEGACY_STARTER_IDS` / `LEGACY_ALL_IDS`, and the migration reads *those* instead of the
live pools. A pack may reorder or replace quests freely without ever shifting a legacy save's mapping, and the
constraint above no longer applies to any future release. The ids matched title slugs with no collisions across
the two boards (verified: 72 quests, 72 unique ids).

### Existing items in worlds

The component migration in §2.2 changes how new items are *stamped*, not how existing ones are *recognised* —
recognition is by the `vs_*` marker, which is untouched. But an item crafted before the migration carries
`CUSTOM_MODEL_DATA`, and once the 57 vanilla item-model overrides are deleted it has nothing to render from.
Three options: keep the old overrides for one transition release; accept that pre-migration gear renders as its
vanilla base until re-crafted; or **re-stamp marked stacks in place** when they are seen (on join or inventory
tick), which is cheap and invisible. The re-stamp is the recommended route.

---

## 6. Open decisions

### 6.1 Custom-block appearance — SUPERSEDED 2026-08-19: block takeover

> **This decision was reversed in testing and the shipped 2.0 does the opposite.** The overlay is gone.
> The Unstable and Stable Skill Shard Blocks are **reinforced deepslate** and **lodestone**, taken over
> outright and retextured in the pushed pack. Ancient cities generate obsidian in their place through
> three `minecraft:rule` processor lists, and lodestone's recipe is removed, so neither block is
> obtainable except through VanillaSkills.
>
> **Why the overlay failed in practice.** Display entities are lit by the light level *at their own
> position*, which inside a solid block is zero — so every overlay rendered black. Forcing a brightness
> override fixed the colour but not the rest: the overlay flickered back to the base block on neighbour
> updates, could not be made to survive chunk reloads reliably, and the amethyst base showed through
> during the gap. The objection below — that retexturing a vanilla block "visibly rewrites two of the
> game's set pieces" — turned out to be answerable: redirecting ancient-city generation to obsidian
> costs three processor-list files and removes the conflict entirely.
>
> The analysis of the other two routes is kept because it is still correct, and still the reason
> blockstate multiplexing was not revisited.

**Originally chosen: a real `minecraft:amethyst_block` in the world, dressed with a `Display.ItemDisplay`
carrying the custom model.** VanillaSkills already tracks each placed position (it must, for merge counts and the aura),
so spawning and clearing the display alongside costs nothing extra. Amethyst is the base deliberately: if a
display ever fails to spawn, the block degrades to something that still reads as a crystal.

Three routes were investigated. The two rejected ones and why:

- **Retexturing a sacrificial vanilla block** doesn't scale. We need several distinct appearances, and the only
  truly unobtainable candidates are `reinforced_deepslate` and `budding_amethyst` — whose textures are
  load-bearing for ancient cities and geodes. Retexturing those visibly rewrites two of the game's set pieces.
- **Blockstate multiplexing** looked attractive and was very nearly chosen. `mushroom_stem` is a trap: its
  blockstate file is **multipart**, so overriding it means hand-reimplementing vanilla's per-face model logic —
  exactly the pattern that caused the 0.19.15 and 1.0.2 bugs and that §2.2 just finished deleting. `note_block`
  is cleaner (a single catch-all variant, so expanding it copies no vanilla logic) **but a note block re-derives
  its `instrument` from the block beneath it on every neighbour change**, so a reserved state would silently
  break whenever anyone changed the block below. Holding it would have meant a permanent block-update listener
  on every placed block purely to re-assert its own appearance, plus suppressing note-cycling on right-click.

The display overlay avoids fighting vanilla block behaviour entirely, reuses the entity pattern already proven
by `BountyBoards`, and is what the reference pack's Warding Stone does.

⚠ (No longer applicable — see the note above.) Trade-off accepted: displays are entities, so they add entity count and can in principle be culled or lost.
`ShardBlocks.refreshAll` exists as the repair path — it re-spawns the display for every tracked position in a
loaded chunk.
⚠ `Display.ItemDisplay.setItemStack` is private on both loaders; use the **public** `getSlot(0).set(stack)`
instead, which avoids needing a Fabric access widener and a NeoForge access transformer.

### 6.2 Infusing Table cost — RESOLVED 2026-07-31

**Books are never consumed; the table charges shards.** A shelved library is infrastructure you build once,
so putting a Silk Touch book on a shelf makes Silk Touch permanently available at that table. The price is
paid in shards and is configurable in both amount and currency — `infusingCostPerLevel` (default 3, so
Efficiency IV costs 12) and `infusingCurrency` (`"skill"` or `"quest"`), plus `infusingEnabled` to turn the
whole replacement off.

### 6.3 Server-side routes for client-rendered features
Both the copper helmet's dynamic light and the horse-stats display are client-rendered concerns. The user has
asked to investigate **server-side** approaches before deciding. Known options:

- *Copper helmet:* place and clean up temporary `minecraft:light` blocks around the wearer. Works on vanilla
  clients but risks flicker and block-update load. The alternative is an optional client mixin that degrades
  gracefully — precedented by the existing `GameNarratorMixin`.
- *Horse stats:* the horse screen is client-rendered, so server-side options are a chat readout on opening, or
  lore on an item placed in the horse inventory. Neither is clean.

### 6.4 Custom ingredients
Whether to rely on `DefaultCustomIngredients.customData` (§2.4) — needs a real vanilla-client test and a NeoForge
equivalent before anything depends on it.

---

## 7. Edition parity

Four editions must stay in lockstep: **26.2 Fabric**, **26.2 NeoForge**, **26.1.2 Fabric**, **26.1.2 NeoForge**.

Only **7 files** currently differ between Fabric and NeoForge, and every other file is byte-identical:
`VanillaSkills.java`, `client/ClientConfig.java`, `client/VanillaSkillsClient.java`,
`client/VanillaSkillsModMenu.java` (Fabric only), `creative/VanillaSkillsItemGroup.java`,
`loot/DragonTemplateLoot.java`, `loot/FortuneTemplateLoot.java`.

**The datapack loader registration adds an 8th** (§3.3). Keep the divergence to that one file — the loader logic
itself should be shared and edition-agnostic, taking a `ResourceManager`.

The 26.1.2 backport delta remains `EntityTypes` → `EntityType` and `Items.X.color()` → `Items.COLOR_X`.

---

## 8. Compatibility hazards

Things that break quietly if not watched:

- **`applyEconomy` couples `points.json` to tree pricing.** Changing what awards Skill Shards silently reprices
  every node.
- **Node effects are baked into the saved per-world tree**, so effect changes need a regeneration to reach
  existing worlds.
- **`QuestPool.STARTER` is size-locked in three places**: `QuestMenu.STARTER_SLOTS` (15 slots), the graduation
  check, and the `starterVersion = 2` migration.
- **The pushed resource pack shadows jar assets even on modded clients.** Any art or lang change needs the pack
  rebuilt, re-hosted, and `DEFAULT_RP_URL` + `DEFAULT_RP_SHA1` bumped with the old URL added to
  `SUPERSEDED_RP_URLS`.
- **`ShopMenu.ITEM_SLOTS` is a hardcoded 8** while `GameplayConfig.SHOP_SLOTS` clamps to 1–45, so offers 9+ are
  generated and never rendered. Pre-existing bug; fix during the shop rework.
- **Three new Skill Shard faucets land at once** — Feats-on (~188 QS), the shard ore, and crates. Price them
  together against the balance workbook, or the 2126-shard budget drifts unnoticed.
- **spawnmanager's `ServerExplosionMixin` cancels all explosions inside the spawn-protection radius.** This
  previously caused a withdrawn release when Wind Burst appeared broken. Suspect it first when explosion-adjacent
  mechanics misbehave near spawn — relevant to SSSB creeper immunity testing.
- **THP may kill the Ender Dragon on startup**, which is why the scale drop must be player-kill-gated (§3.6).

### Casino add-on (vscasino)

The casino stays a separate mod and must keep working. Its entire contract with VanillaSkills is:

- **`api/SkillMenuExtensions`** — `register(id, preferredSlot, icon, onClick)`, dispatched from
  `SkillTreeMenu.placeExtensions()` and `handleLaneSelectClick()`.
- **`PlayerSkillManager`**: `skillShards`, `questShards`, `spendQuestShards`, `addQuestShards`,
  `convertToSkillShards`.
- Vanilla `Attributes.LUCK`, for its luck bonus.
- Its own resource pack under a **different UUID** so the two packs stack rather than replace each other.

Keep those signatures and the extension-slot logic and the casino is unaffected. If shards change
representation, `VsCasino.java`'s three bridge methods are the single point of repair.

⚠ The casino's own pack must not override any item model VanillaSkills owns. Migrating to `ITEM_MODEL` (§2.2)
removes this hazard entirely, since VanillaSkills stops touching `assets/minecraft/items/` altogether.

⚠ svcrates' terms prohibit use in real-money systems (§3.8) — relevant if crates ever interact with the casino.
