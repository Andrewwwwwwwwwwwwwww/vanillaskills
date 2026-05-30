package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The full skill tree definition (root of skilltree.json).
 * Nodes are stored as a list in JSON; {@link #index()} builds id/slot lookups.
 */
public class SkillTree {
    public int version = 1;
    public String title = "Skills";
    public int rows = 6;
    public List<SkillNode> nodes = new ArrayList<>();

    public static final String ROOT_ID = "root";

    private transient Map<String, SkillNode> byId = new LinkedHashMap<>();
    private transient Map<Integer, SkillNode> bySlot = new LinkedHashMap<>();

    /** Rebuild lookup maps and normalize nodes. Call after load / after edits. */
    public void index() {
        byId = new LinkedHashMap<>();
        bySlot = new LinkedHashMap<>();
        if (nodes == null) nodes = new ArrayList<>();
        for (SkillNode node : nodes) {
            if (node == null || node.id == null) continue;
            node.normalize();
            byId.put(node.id, node);
            bySlot.put(node.slot, node);
        }
    }

    public SkillNode byId(String id) {
        return byId.get(id);
    }

    public SkillNode bySlot(int slot) {
        return bySlot.get(slot);
    }

    public boolean has(String id) {
        return byId.containsKey(id);
    }

    public int size() {
        return nodes.size();
    }

    public int clampedRows() {
        return Math.max(1, Math.min(6, rows));
    }

    public int slotCount() {
        return clampedRows() * 9;
    }
}
