# VanillaSkills Changelog

## [0.14.2] - 2026-06-08

### Changed
- **All set-bonus armor now shows a live "what you're missing" checklist when worn**, matching Rose
  Gold. While wearing any piece of the **Crystalline** or **Dragon** set, the tooltip shows the set
  count (n/4), a +/- list of which slots are filled, and whether the bonus is active — so you can see
  what you still need. Stored (un-worn) pieces revert to the static description. Generalised the old
  Rose Gold-only logic into a shared `ArmorSetTooltips` so every set tier behaves identically.
  (Hardwood and Steel have no full-set-gated bonus — their movement/toughness is per-piece — so they
  show no checklist.)

## [0.14.1] - 2026-06-08

### Added
- Op test/admin commands: **`/skill questshards <player> add|set <n>`** to grant or set Quest Shards,
  and **`/quests reroll`** to force a fresh set of bounties immediately.

## [0.14.0] - 2026-06-08

### Added
- **Two currencies, renamed for clarity.** Skill-tree points are now **Skill Shards**; quest bounties
  award a separate **Quest Shard** currency. Every "point(s)" label across the GUIs, commands, and
  guide book is updated. Existing balances carry over as Skill Shards; everyone starts with 0 Quest
  Shards.
- **Quest Shop** — a Shop button in the quest GUI opens a daily-rotating catalog (~105 entries,
  weighted so cheap commons appear most). Pay with **Quest Shards (left-click)** or **Skill Shards
  (right-click, auto-converted 3:1)**. A permanent **converter** slot turns 3 Quest Shards → 1 Skill
  Shard (one-way), so early-game Quest Shards buy boosts and late-game they help finish the skill tree.
- The shop selection rotates daily at UTC midnight, derived deterministically from the day (no extra
  save file).

### Changed
- **Bounty board is now holograms-style floating text** instead of a lectern. `/quests board` (op)
  spawns a native text-display ("✦ Bounty Board ✦") plus an invisible interaction entity that anyone
  can right-click to open the quest GUI. `/quests board remove` cleans up both entities.
- Quest GUI and Shop GUI use a clean framed layout (bordered, centered content, header with balances
  + timer, footer with navigation/converter) rather than loose corner icons.

### Internal
- Removed the now-unused `ArmorStandAccessor` mixin (the board no longer uses an armor stand).

## [0.13.0] - 2026-06-07

### Added
- **Physical bounty board**: ops can summon a real, interactable board with
  `/quests board` (alias `/bounty board`). It places a **lectern** where you're looking with a floating
  golden **"✦ Bounty Board ✦"** label above it. Anyone can right-click the board to open the quest GUI —
  no command needed. Remove the nearest board (within 6 blocks) with `/quests board remove`. Board
  positions persist (`world/vanillaskills/questboards.json`) and the floating label is an invisible,
  invulnerable marker armor stand that's cleaned up on removal.

### Internal
- Removed dead code: unused `AlloyRecipes` serializers (the rose gold / steel / crystallized-diamond
  recipes use vanilla `crafting_shapeless`) and their registrations, the orphaned `addLane`/`addLaneMulti`
  helpers in `SkillTreeManager`, and an unused `Map` import. No behavior change.

## [0.12.0] - 2026-06-06

### Added
- **Bounty board** (`/quests`, alias `/bounty`): 3 shared quests that re-roll every **5 hours**.
  Each is a gather-items (turn in at the board) or kill-mobs bounty worth a couple of **skill points**.
  Anyone can do each once per rotation; progress is tracked per player and resets each rotation. Kill
  progress counts toward active quests automatically; gather quests consume the items on claim. Board
  state persists (`world/vanillaskills/questboard.json`); a chat announcement fires on each re-roll.

## [0.11.1] - 2026-06-06

### Changed
- Night Vision capstone cost lowered to **150** (was 276).

## [0.11.0] - 2026-06-06

### Added
- **Night Vision** capstone lane (centred below the lane grid): one node granting **permanent Night
  Vision**, costing **276** — the surplus points a true completionist earns beyond the rest of the
  tree, so doing every advancement affords the whole tree *and* this. Status effects now refresh at
  20s (was 11s) so Night Vision never flashes.

## [0.10.0] - 2026-06-06

### Added
- **Retexturable worn armor.** Each armor tier now uses its own equipment asset
  (`vanillaskills:<tier>`) so resource packs can give each tier custom *worn* armor textures (not just
  inventory icons). Default equipment assets are bundled (worn armor still looks like its base
  material). NOTE: on a vanilla client with no resource pack, custom worn armor has no texture
  (invisible) — modpacks should ship the bundled/required resource pack.

## [0.9.3] - 2026-06-06

### Fixed
- **Skill Master never gave the 5 Dragon Ingots.** The reward was gated on the advancement's
  done-state — once the advancement got marked complete (or the ingots fell into a full inventory),
  the reward path was skipped forever. Now tracked by a flag in player data, so it reliably fires once
  on the unlock/login that completes the tree. (Completion = every node in the *current* tree; if you
  regenned and added lanes, those must be unlocked too.)

## [0.9.2] - 2026-06-06

### Fixed
- **Datapack advancements no longer give points.** Only `minecraft:` and `vanillaskills:` advancements
  are counted now — VanillaTweaks (and other datapacks) were granting points (e.g. +115). Run
  `/skill recalc <player>` to strip already-credited datapack points from existing players.
- **Advancement tab background** now renders: 26.1.2 expects a sprite path without `textures/`/`.png`,
  so it's `minecraft:block/iron_block` (tiled iron) instead of the old full texture path.

### Changed
- **Evasion** uses the centered 5+5 node layout (matching Reach/Guardian/Fortune).

## [0.9.1] - 2026-06-06

### Fixed
- **Losing bonus hearts on relog.** Vitality (and other) max-health is added via transient modifiers
  reapplied on join — but the player loads first, so vanilla clamped current health to the base 20
  before our modifiers existed. Now we record health on logout and restore it after the modifiers
  reapply, so a player who logged out with full 3 rows logs back in with them full.

## [0.9.0] - 2026-06-05

### Added
- **Hardwood swords & axes** now inflict **Poison I for 2s** on hit.

### Changed
- Advancement tab background is now a tiled **iron block** texture.
- Guide book: Steel recipe corrected to **2 iron + 1 coal**.
- Skill-tree layouts: **Fortune Finder, Guardian, Reach** now lay out as a centred **5 + 5** block;
  **Cultivator** as a centred vertical column.

### Fixed
- **Skill Master reward** now also re-checks on join, so players who completed the tree before the
  reward existed get their **5 Dragon Ingots** on next login.

## [0.8.0] - 2026-06-05

### Added
- **Skill Master** advancement (challenge): unlock every node in the skill tree → rewarded with
  **5 Dragon Ingots** (granted in-code) + points.
- **Steeled Defense** advancement: craft a Steel-Infused Shield.

### Changed
- **"Earning Points" screen** rewritten — was a wall of raw advancement IDs; now a clean curated
  summary (per-advancement, milestones, VanillaSkills goals, starting bonus, recipe note).
- **Mountaineer** is more expensive: node costs 5/10/15 (was 1/2/3).
- **Multi-node lanes** now lay out in a tidy centred 7-wide block instead of filling from the
  top-left corner of the chest.

## [0.7.0] - 2026-06-05

### Added
- **Custom mod advancements** (own tab, shown on vanilla clients) that also grant skill points:
  - **Metallurgist** — forge any custom alloy
  - **Hardwood / Rose Gold / Steel / Crystalline / Dragon Suit** — craft a full set of each tier
  - **Ancient Knowledge / Dragon's Legacy** — discover the Fortune / Dragon upgrade templates
  - **Dragon Forged** — forge a Dragon Ingot · **Winged Dragon** — fuse an Elytra onto a Dragon chestplate
  - **Specialist** — fully unlock any skill path (granted in-code on lane completion)
- Point values for all of the above added to `points.json` defaults (+~128 to the earnable total).

## [0.6.1] - 2026-06-05

### Changed
- **Points rebalance** so the full tree (~826 pts / 132 nodes) is reachable very late-game:
  `perAdvancement` 1 → **5**, `startingPoints` 0 → **5**, and **~24 progression-weighted milestone
  overrides** (nether/end/netherite/hard challenges). A near-completionist earns ~900-930.
- Existing servers: delete `config/vanillaskills/points.json` (or edit it) to pick up the new values,
  then `/skill reload` and `/skill recalc <player>` to re-tally already-earned advancements.

## [0.6.0] - 2026-06-05

### Added
- **Evasion** lane (10 nodes): +2% chance per node to completely dodge incoming **arrow** damage,
  up to **20%**. Costs ramp steeply (3→26) — it's a strong defensive perk. (`ArrowDodgeMixin` on
  `LivingEntity#hurtServer`, rolled per hit.)
- **Cultivator** lane (5 nodes): +20% chance per node for **bonus crops** when harvesting mature
  crops (wheat, carrots, potatoes, beetroot, nether wart) — up to a guaranteed bonus at 100%. Drops
  +1–2 extra of the crop's product on success.

### Note
- Run **`/skill regen`** to load the two new lanes (home page is now a 7×2 block).

## [0.5.3] - 2026-06-05

### Changed
- **Steel Ingot** now costs **2 iron + 1 coal** (was 1 iron + 1 coal).
- **Aquatic** node costs ramp up steeply (3→24) so reaching full underwater capability is a real
  point investment.

## [0.5.2] - 2026-06-05

### Fixed
- **Server crash on startup**: `addLaneNodes` used a fixed 6-entry numerals array for node titles, but
  the rescaled lanes have up to 20 nodes → `ArrayIndexOutOfBoundsException` while building the default
  tree (which crashed the server). Replaced with a Roman-numeral helper that handles any node count.

## [0.5.1] - 2026-06-04

### Fixed
- **Dragon Ingot recipe now actually crafts.** It used fragile position-based 3x3 matching;
  rewritten to the same shapeless item-counting style as the other custom alloy recipes
  (8 Dragon Scales + 1 plain Netherite Ingot, in any arrangement → 1 Dragon Ingot).

### Changed
- **Vitality** is now **+1 heart (2 HP) per node, 20 nodes** (still reaches 3 rows of hearts) — half
  the node count.
- **Rose Gold, Steel, and Crystallized Diamond** are now **data-driven recipes** with the marker baked
  into the result, so they **appear in the vanilla recipe book** (granted on join). The crafted items
  are identical (same marker), so armor/tool crafting and repair still recognize them.

### Notes
- Recipe-book display is only possible for recipes with plain vanilla ingredients. Dragon Ingot and the
  Dragon-template duplication can't (their ingredients are marked items, hidden from ingredient
  matching), and the dynamic armor/tool/Fortune recipes are "special" (computed), so none of those
  show in the book.

## [0.5.0] - 2026-06-04

### Fixed
- **Crystallized Diamond, Dragon Ingot, and Dragon-template duplication recipes now work** — they were
  missing their `data/vanillaskills/recipe/*.json` registration entries, so the RecipeManager never
  loaded them. Added the three JSONs.

### Changed
- **Skill-tree home page** reorganized into a tidy 6×2 block (was scattered).
- **Lane rescale** (longer, finer progressions; one node per step):
  - Vitality: +1 max health (½ heart)/node → 3 rows of hearts (40 nodes).
  - Fleet Foot: +2%/node → +30% (15). Fortune Finder: +0.5/node → +5 (10).
  - Warrior: +0.5 attack/node → +10 (20). Guardian: +1 armor/node → +10 (10).
  - Reach: +0.25 block & entity/node → +2.5 (10).
  - Aquatic: spread to 9 nodes (breath ×3, swim speed → full, underwater mining → full).
- Guide book rewritten into shorter pages so nothing is cut off (Dragon section split across pages).

### Note
- Run **`/skill regen`** to apply the reworked tree. Node costs are placeholders pending the points
  rework. Recipe-book display of custom recipes is still TODO (see below).

### Fixed
- **Boot crash**: `DragonSmithingMixin` tried to `@Shadow` the `player` field, which is declared on
  the superclass `ItemCombinerMenu` (Shadow only resolves fields on the target class). Replaced with
  an `ItemCombinerMenuAccessor` (`@Accessor`) to read the player. Compiled fine but crashed on launch.

## [0.4.1] - 2026-06-04

### Added
- A duplication recipe for the **Dragon Upgrade template** (output 2): chorus flowers around the
  existing template + a netherite ingot, with end rods flanking a shulker shell on the bottom row:
  `C T C / C N C / R S R`. (Vanilla template duplication can't dupe it, since the marked template is
  hidden from ingredient matching.)

## [0.4.0] - 2026-06-04

### Changed
- **Armorsmith** and **Toolsmith** are now **5-node lanes (one node per tier)**, costs scaling from
  10 (10/15/20/25/30). Each node unlocks crafting of that specific tier (per-tier flags
  `craft_armor_<tier>` / `craft_tool_<tier>`). The Dragon node also gates the Dragon smithing upgrade.
- **Brewmaster** is now a **5-node lane**; each node adds +20% beneficial-potion duration (up to
  +100%), costs scaling from 10.
- **Prospector** expanded to **5 nodes totalling +12 mining efficiency** — enough that an Efficiency V
  diamond pickaxe instamines stone (Haste II–equivalent speeds).

### Note
- Existing worlds: run **`/skill regen`** to pull in the reworked lanes (backs up the old tree).

### Added
- The combined (Elytra'd) Dragon chestplate now shows a **"+ Elytra"** line in its tooltip so players
  can tell it glides. The label is stripped when the chestplate is split back apart.

## [0.3.0] - 2026-06-04

### Changed
- **Elytra combine reworked to a drop-on-block mechanic** (modelled on the Vanilla Tweaks Armored
  Elytra datapack), replacing the unreliable anvil-GUI mixins. **Drop** a Dragon chestplate + an
  Elytra together **on top of an anvil block** to fuse them into one gliding chestplate; **drop** the
  combined chestplate **on a grindstone block** to split them back (enchants preserved). A 1/second
  server-side scan (`DragonElytraForge`) handles it — no menu mixins, fully vanilla-client safe.

### Removed
- `DragonElytraAnvilMixin`, `GrindstoneMenuMixin`, `GrindstoneResultSlotMixin`, and
  `DragonGrindstoneAccess` (the old anvil/grindstone GUI combine path).

## [0.2.3] - 2026-06-04

### Fixed
- The Dragon Upgrade template no longer shows netherite's "Applies to / Ingredients" tooltip lines.
  `DragonTemplateTooltipMixin` suppresses `SmithingTemplateItem.appendHoverText` for our marked
  template (its custom name + lore still render). Client-side: takes effect on clients that have the
  mod; a pure-vanilla client on a dedicated server still sees the netherite lines (tooltips render
  client-side, and the item must remain a real template so the smithing slot accepts it).

## [0.2.2] - 2026-06-04

### Fixed
- **Boot crash**: `PotionStackSizeMixin` targeted `getMaxStackSize` on `ItemStack`, but that method
  is a default on the `ItemInstance` interface (ItemStack doesn't declare it), so the injection
  found no target and crashed the game on startup. Now mixes into `ItemInstance` instead.

## [0.2.1] - 2026-06-04

### Added
- **`/skill regen`** (op): overwrites `config/vanillaskills/skilltree.json` with the built-in
  default tree (so servers pick up new lanes/nodes after a mod update), backing up the existing
  file to `skilltree.backup-<timestamp>.json` first, then reapplying effects to online players.

## [0.2.0] - 2026-06-04

### Added
- **Dragon armor overhaul**: Ender Dragon drops **Dragon Scales** (8/kill) → forge a **Dragon Ingot**
  (8 scales around a netherite ingot). Dragon **armor** is now upgraded from netherite in a
  **smithing table** with the **Dragon Upgrade Template** (End City treasure, ~4%) + a Dragon Ingot,
  preserving enchantments. Dragon **tools** are crafted from Dragon Ingots. Dragon armor is no longer
  table-craftable.
- Skill tree grew to **12 lanes**: Guardian (armor), Reach (interaction range), Mountaineer (step
  height), Aquatic (underwater), **Armorsmith**/**Toolsmith** (unlock crafting custom gear), and
  **Brewmaster** (+50% beneficial potion duration).
- **Skill-gated crafting**: custom armor/tools can't be taken from the crafting result until the
  matching skill is unlocked; ingots/materials are never gated.
- **Potions** stack to 16; Brewmaster extends beneficial, non-infinite potions ×1.5 (past the 8-min cap).

### Changed
- Hardwood armor movement bonus 8% → 10% at a full set.
- Dragon armor toughness 4.0 → 3.0 (the dive-dash was too strong with the higher value).

### Fixed
- **Elytra now combines onto the Dragon chestplate**: the anvil mixin injected at TAIL, which vanilla
  skips via an early return for an "invalid" armor+elytra combo. Now HEAD/cancellable, and it consumes
  the elytra (sets `repairItemCountCost`).

### Notes
- The Elytra anvil/grindstone and Dragon smithing menu interactions compile against verified game
  internals (SmithingMenu places items via RecipePropertySet, not Ingredient, so marked items fit
  the slots natively) but still warrant an in-game playtest. Custom items render as their vanilla
  bases until a resource pack adds textures.

## [0.1.2] - 2026-06-04

### Changed
- Spears now use the real vanilla spear items (`WOODEN/STONE/COPPER/IRON/GOLDEN/DIAMOND/NETHERITE_SPEAR`)
  instead of being faked with swords. The tiered spear keeps the real spear's reach and slower
  swing and just adds the normal per-tier damage/speed bonus.
- Halved the Steel-Infused Shield's thorns damage (4.0 → 2.0), so blocking a melee hit injures the
  attacker for 1 heart instead of 2.

## [0.1.1] - 2026-06-02

### Fixed
- Fixed a fatal server-boot crash: the grindstone Elytra-split mixin injected `onTake` on
  `GrindstoneMenu`, but that method lives on the result-slot inner class. Split the logic into a
  dedicated inner-class mixin so the server starts again.

## [0.1.0] - 2026-06-02

### Added
- Server-side skill tree and progression overhaul (works with vanilla clients).
- Per-player skill tree with a chest GUI and `/skill editor`; Mending removed from the game.
- New tool/weapon/armor tiers: Hardwood, Rose Gold, Steel, Crystalline (Diamond II), and Dragon
  (Netherite II), plus dragon-scale drops, fire immunity, a dive-dash, and an Elytra⇄chestplate
  anvil/grindstone combine.
- Craftable Fortune IV/V via a custom smithing template.
