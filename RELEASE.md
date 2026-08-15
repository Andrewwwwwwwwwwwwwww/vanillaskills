# VanillaSkills 2.0.0 — release checklist

Four editions ship together and must stay in lockstep: **26.2 Fabric**, **26.2 NeoForge**,
**26.1.2 Fabric**, **26.1.2 NeoForge**. The 26.2 Fabric tree is the source of truth; the other three
are kept in parity from it.

---

## Done

- [x] All four editions at `mod_version=2.0.0`, building clean.
- [x] All six datapack content types live: `skill_category`, `skill_node`, `quest`, `shop_offer`,
      `feat`, `crate`.
- [x] Localization complete — en_us and zh_tw both 689 keys, no gaps in either.
- [x] Texture-pack lang kept in sync by a build step (`syncPackLang`), so a vanilla client can no
      longer be served raw translation keys.
- [x] Gear balance in `gameplay.json`; existing gear is brought onto retuned numbers on login.
- [x] Steel at a flat -10% on the full set.
- [x] Desert crate, with the 1% enchanted golden apple.
- [x] CHANGELOG entry written.
- [x] **Art complete** — all 10 remaining textures supplied 2026-08-15 and in the pack. No placeholders left.
- [x] Release-candidate test server at `projects/_test-server/vs-2.0.0/`.
- [x] All four editions committed. 26.2 Fabric and 26.2 NeoForge are on `2.0-rework`; both 26.1.2
      editions have their own repository now, with the 2.0 tree as their first commit.

---

## Blocking — must happen before the tag

### 1. Playtest
Nothing below has been exercised in a real game:

- the crate slot-machine reel, and crates as a system
- recipe-book autofill
- horse stats in the inventory screen
- Dragon scale repair
- enchanted books in the Quest Shop
- the five gear tiers at their 2.0 numbers
- **migration from a real 1.7.6 world** — highest consequence, and the one that can eat player data.
  Run it against a *copy* of the live world before it ever runs against the live one.

### 2. Repositories for the 26.1.2 editions
Both have local repos and an initial commit, but no remote. Two public repos need creating and
pushing, following the `vscasino` / `vscasino-mc26.1.2` naming already in use:

```bash
gh repo create vanillaskills-mc26.1.2 --public --source=. --remote=origin --push
```

```bash
gh repo create vanillaskills-neoforge-mc26.1.2 --public --source=. --remote=origin --push
```

### 3. Texture pack
`tools/build-pack.sh v2.0.0` rebuilds the zip and patches its SHA-1 into all four editions. Already
run with the finished art — current hash `0b6314361c10262155132b1d4a539ede9c3bd20b`, 193673 bytes.
Upload **that exact zip** to the `v2.0.0` GitHub release; re-run the script if anything under
`resourcepack/` changes again, because a client refuses a pack whose hash does not match.

⚠ `DEFAULT_RP_URL` already points at `.../download/v2.0.0/VanillaSkills-TexturePack.zip`, which does
not exist until the release is cut. Between now and then, servers on this build push a 404. Cut the
release with the asset attached before deploying anywhere real.

---

## Release ritual

1. `./tools/build-pack.sh v2.0.0` (after art).
2. Build all four editions.
3. Archive the previous jars to `projects/_jar-archive/vanillaskills/`, prune `build/libs/` to the
   current jar.
4. Refresh `curseforge-upload/` and `projects/CF Pack Upload/`.
5. Deploy to the instances.
6. Merge `2.0-rework` into `master` on both 26.2 repos; push all four.
7. `gh release create v2.0.0` with the four jars **and the texture pack zip**.
8. Update the ModHub page.
9. Update memory.

---

## Known gaps, shipping as-is

- **Copper helmet light** (`copperHelmetLight`) is client-side rendering — only players with the mod
  installed see it. Vanilla clients get nothing. Blocked on 26.2 renderer hooks.
- `gear_tier` is the one designed datapack content type not implemented; tier *numbers* are in
  gameplay.json instead, and tier *structure* (base items, materials, repair rules) stays in code.
- Four balance numbers are implemented but never play-verified: Hardwood +16%, Steel -10%,
  Crystalline's additive Strength/Resistance, and the Fortune V recipe layout. All four are now
  config knobs, so they can be tuned without a rebuild.
