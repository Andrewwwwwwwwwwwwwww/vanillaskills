package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Per-player progression, stored at world/vanillaskills/players/&lt;uuid&gt;.json.
 */
public class PlayerSkillData {
    public int version = 1;
    public Set<String> unlocked = new LinkedHashSet<>();
    public int pointsAvailable = 0;
    public int pointsEarned = 0;
    public float lastHealth = -1f; // health at last logout, restored after max-health modifiers reapply
    public boolean completionRewarded = false; // got the full-tree Dragon Ingot reward
    public Set<String> creditedAdvancements = new LinkedHashSet<>();
    public boolean initialized = false;

    // Bounty board progress for the current rotation.
    public long questRotation = -1;
    public Map<Integer, Integer> questKills = new HashMap<>();
    public Set<Integer> questClaimed = new LinkedHashSet<>();

    public void normalize() {
        if (unlocked == null) unlocked = new LinkedHashSet<>();
        if (creditedAdvancements == null) creditedAdvancements = new LinkedHashSet<>();
        if (questKills == null) questKills = new HashMap<>();
        if (questClaimed == null) questClaimed = new LinkedHashSet<>();
    }

    public boolean hasUnlocked(String id) {
        return unlocked.contains(id);
    }

    public void grantPoints(int amount) {
        pointsAvailable += amount;
        pointsEarned += amount;
    }
}
