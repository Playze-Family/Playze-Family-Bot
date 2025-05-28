package com.agonkolgeci.gebo.core.dev.members;

import com.agonkolgeci.gebo.core.dev.GuildComponent;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

public abstract class MemberComponent extends GuildComponent {

    @Getter @NotNull protected final Member member;
    @Getter @NotNull protected final MemberCache memberCache;

    public MemberComponent(@NotNull MemberCache memberCache) {
        super(memberCache.getGuildCache());

        this.member = memberCache.getMember();
        this.memberCache = memberCache;
    }

}
