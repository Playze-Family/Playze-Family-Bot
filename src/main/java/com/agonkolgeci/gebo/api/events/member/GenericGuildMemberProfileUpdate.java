package com.agonkolgeci.gebo.api.events.member;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import org.jetbrains.annotations.NotNull;

public class GenericGuildMemberProfileUpdate extends GenericGuildMemberEvent {

    @Getter protected final int oldLevel;
    @Getter protected final int oldXP;
    @Getter protected final int newLevel;
    @Getter protected final int newXP;

    public GenericGuildMemberProfileUpdate(@NotNull JDA api, @NotNull Member member, int oldLevel, int oldXP, int newLevel, int newXP) {
        super(api, -1, member);

        this.oldLevel = oldLevel;
        this.oldXP = oldXP;
        this.newLevel = newLevel;
        this.newXP = newXP;
    }

}
