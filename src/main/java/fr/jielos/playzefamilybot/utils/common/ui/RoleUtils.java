package fr.jielos.playzefamilybot.utils.common.ui;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class RoleUtils {

    @NotNull
    public static String getRoleName(@NotNull Role role) {
        @Nullable final Emoji roleEmoji = EmojiUtils.extractEmojiFromString(role.getName());

        return role.getName()
                .replaceAll("•", "")
                .replaceAll(roleEmoji != null ? roleEmoji.getFormatted() : "", "")
                .trim();
    }

    @NotNull
    public static List<Role> addRolesToMember(@NotNull Member member, @NotNull List<Role> roles) {
        @NotNull final List<Role> newRoles = roles.stream().filter(role -> !member.getRoles().contains(role)).distinct().toList();
        for(@NotNull final Role newRole : newRoles) {
            member.getGuild().addRoleToMember(member, newRole).queue();
        }

        return newRoles;
    }

    @NotNull
    public static List<Role> addRolesToMember(@NotNull Member member, @NotNull Role... roles) {
        return addRolesToMember(member, Arrays.asList(roles));
    }

}
