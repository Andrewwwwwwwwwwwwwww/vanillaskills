package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Per-player progression, stored at world/vanillaskills/players/&lt;uuid&gt;.json.
 */
public class PlayerSkillData {
    public int version = 1;
    public Set<String> unlocked = new LinkedHashSet<>();
    public int pointsAvailable = 0;
    public int pointsEarned = 0;
    public Set<String> creditedAdvancements = new LinkedHashSet<>();
    public boolean initialized = false;

    public void normalize() {
        if (unlocked == null) unlocked = new LinkedHashSet<>();
        if (creditedAdvancements == null) creditedAdvancements = new LinkedHashSet<>();
    }

    public boolean hasUnlocked(String id) {
        return unlocked.contains(id);
    }

    public void grantPoints(int amount) {
        pointsAvailable += amount;
        pointsEarned += amount;
    }
}
