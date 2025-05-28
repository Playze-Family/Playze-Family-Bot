package com.agonkolgeci.gebo.core.dev.members.profile.level;

import lombok.Getter;

public enum MemberProfileLevel {

    MESSAGE(5),
    VOICE(20),
    REACTION(10),
    INVITE(800);

    @Getter private final int gain;

    MemberProfileLevel(int gain) {
        this.gain = gain;
    }

    public static final int DEFAULT_VALUE = 100;
    public static final int STEP_MULTIPLIER = 10;

    public static int retrieveCompleteXP(int level) {
        int multiplier = level / STEP_MULTIPLIER; // Augmente tous les X steps
        int increase = multiplier * DEFAULT_VALUE;

        return DEFAULT_VALUE + increase;
    }

    public static int retrieveTotalXP(int level, int xp) {
        int totalXP = xp;
        for (int i = 0; i < level; i++) {
            totalXP += retrieveCompleteXP(i);
        }

        return totalXP;
    }
}
