#!/usr/bin/env bash
#
# Rebuild the pushed texture pack and wire its hash into all four editions.
#
#   ./tools/build-pack.sh [release-tag]
#
# The server pushes this zip to every joining client, and the client REFUSES it if the SHA-1 in
# GameplayConfig does not match the file byte for byte. So the zip and the hash have to be produced
# together, every time — doing it by hand is how a pack ends up rejected in the wild.
#
# Uses the JDK `jar` tool, never PowerShell Compress-Archive: Compress-Archive writes backslash path
# separators into the zip entries and Minecraft cannot read the result.
#
# Run this after ANY change under resourcepack/ — new art, a lang update, a new item model.

set -euo pipefail

HERE="$(cd "$(dirname "$0")/.." && pwd)"
PACK_DIR="$HERE/resourcepack"
OUT="$HERE/curseforge-upload/VanillaSkills-TexturePack.zip"
TAG="${1:-}"

JAR="${JAVA_HOME:+$JAVA_HOME/bin/}jar"

if [ ! -f "$PACK_DIR/pack.mcmeta" ]; then
    echo "error: $PACK_DIR/pack.mcmeta not found" >&2
    exit 1
fi

# Keep the pack's lang copies identical to the mod's. A vanilla client has no mod jar, so the NAMES of
# every custom item come from the pack — a stale copy ships raw translation keys to players.
cp "$HERE/src/main/resources/assets/vanillaskills/lang/"*.json \
   "$PACK_DIR/assets/vanillaskills/lang/"

mkdir -p "$(dirname "$OUT")"
rm -f "$OUT"
# -C so pack.mcmeta lands at the zip ROOT, which is what Minecraft requires.
(cd "$PACK_DIR" && "$JAR" --create --file "$OUT" --no-manifest .)

SHA1=$(sha1sum "$OUT" | cut -d' ' -f1)
SIZE=$(wc -c < "$OUT")

echo "built  $OUT"
echo "size   $SIZE bytes"
echo "sha1   $SHA1"

# Patch the hash into every edition so they cannot drift apart.
for ED in "$HERE" \
          "$HERE/../../26.1.2/vanillaskills" \
          "$HERE/../vanillaskills-neoforge" \
          "$HERE/../../26.1.2/vanillaskills-neoforge"; do
    CFG="$ED/src/main/java/io/github/andrewwwwwwwwwwwwwww/vanillaskills/config/GameplayConfig.java"
    [ -f "$CFG" ] || { echo "skip (no config): $ED"; continue; }
    perl -pi -e "s/(DEFAULT_RP_SHA1 = \")[0-9a-f]{40}(\")/\${1}$SHA1\${2}/" "$CFG"
    if [ -n "$TAG" ]; then
        # ONLY the DEFAULT_RP_URL line. The same URL shape appears in SUPERSEDED_RP_URLS, which is a
        # historical record of packs we have shipped — rewriting those flattens them all onto the current
        # tag, and the auto-upgrade for servers pinned to an old pack silently becomes a no-op.
        perl -0pi -e "s{(DEFAULT_RP_URL =\s*\n\s*\"[^\"]*releases/download/)[^/]+(/VanillaSkills-TexturePack\.zip\")}{\${1}$TAG\${2}}" "$CFG"
    fi
    echo "patched $(basename "$(dirname "$(dirname "$ED")")")/$(basename "$ED")"
done

echo
echo "Next: upload $OUT to the GitHub release${TAG:+ $TAG}, then rebuild all four editions."
