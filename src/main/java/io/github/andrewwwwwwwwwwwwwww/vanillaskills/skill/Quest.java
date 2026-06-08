package io.github.andrewwwwwwwwwwwwwww.vanillaskills.skill;

/**
 * One bounty-board quest: either gather a quantity of an item (turned in at the board) or kill a
 * number of a mob type. Rewards skill points.
 *
 * @param target item id (GATHER) or entity-type id (KILL), or the literal "any_hostile".
 */
public record Quest(Type type, String target, int amount, int reward, String title) {
    public enum Type { GATHER, KILL }

    public static final String ANY_HOSTILE = "any_hostile";
}
