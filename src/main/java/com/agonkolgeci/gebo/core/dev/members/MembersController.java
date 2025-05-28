package com.agonkolgeci.gebo.core.dev.members;

import com.agonkolgeci.gebo.api.managers.Controller;
import com.agonkolgeci.gebo.core.dev.GuildCache;
import com.agonkolgeci.gebo.core.dev.GuildComponent;
import com.agonkolgeci.gebo.core.dev.members.custom_role.CommandCustomrank;
import com.agonkolgeci.gebo.core.dev.members.invites.CommandInvites;
import com.agonkolgeci.gebo.utils.common.ObjectUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MembersController extends GuildComponent implements Controller<MembersController> {

    @Getter @NotNull private final Map<String, MemberCache> members;

    public MembersController(@NotNull GuildCache guildCache) {
        super(guildCache);

        this.members = new HashMap<>();
    }

    @NotNull
    @Override
    public MembersController load() {
        instance.getCommandsController().registerCommandAdapter(new CommandInvites(guildCache, this));
        instance.getCommandsController().registerCommandAdapter(new CommandCustomrank(guildCache, this));

        return this;
    }

    @NotNull
    public MemberCache retrieveMemberCache(@NotNull Member member) {
        return ObjectUtils.retrieveObjectOrElseGet(members, member.getId(), () -> {
            try {
                return new MemberCache(guildCache, clientCache.getUsersController().retrieveUserCache(member.getUser()), member.getId());
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            return retrieveMemberCache(member);
        });
    }

}