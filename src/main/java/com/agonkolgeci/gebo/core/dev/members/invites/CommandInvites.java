package com.agonkolgeci.gebo.core.dev.members.invites;

import com.agonkolgeci.gebo.core.commands.SlashCommandAdapter;
import com.agonkolgeci.gebo.api.messages.PEmbedBuilder;
import com.agonkolgeci.gebo.core.dev.GuildCache;
import com.agonkolgeci.gebo.core.dev.GuildComponent;
import com.agonkolgeci.gebo.core.dev.invites.GuildInvite;
import com.agonkolgeci.gebo.core.dev.members.MemberCache;
import com.agonkolgeci.gebo.core.dev.members.MembersController;
import com.agonkolgeci.gebo.utils.common.ObjectUtils;
import com.agonkolgeci.gebo.utils.common.ui.EmojiUtils;
import com.agonkolgeci.gebo.utils.common.ui.MessageUtils;
import com.agonkolgeci.gebo.utils.common.objects.IntegerUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;


public class CommandInvites extends GuildComponent implements SlashCommandAdapter {

    @NotNull private final MembersController membersController;

    public CommandInvites(@NotNull GuildCache guildCache, @NotNull MembersController membersController) {
        super(guildCache);

        this.membersController = membersController;
    }

    @NotNull
    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("invites", "Visualiser les informations sur ses invitations sur le serveur.")
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("view", "Visualiser son nombre d'invitations sur le serveur.").addOptions(
                                new OptionData(OptionType.USER, "membre", "Visualiser le nombre d'invitations d'un membre spécifique", false)
                        ),

                        new SubcommandData("info", "Visualiser les informations de ses invitations sur le serveur.").addOptions(
                                new OptionData(OptionType.USER, "membre", "Visualiser le informations des invitations d'un membre spécifique", false)
                        )
                );
    }

    @Override
    public void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) {
        @Nullable final Guild guild = event.getGuild();
        if(guild == null) return;

        @Nullable final OptionMapping optionMember = event.getOption("membre");
        @Nullable final Member member = optionMember != null ? optionMember.getAsMember() : event.getMember();
        if(member == null) return;

        Checks.check(!member.getUser().isBot(), MessageUtils.ERROR_MEMBER_MUST_BE_HUMAN);

        @Nullable final String subCommandName = event.getSubcommandName();
        if(subCommandName == null) return;

        @NotNull final MemberCache memberCache = membersController.retrieveMemberCache(member);
        @NotNull final MemberInvitesCache memberInvitesCache = memberCache.retrieveInvitesCache();

        @NotNull final List<Invite> memberInvites = memberInvitesCache.getInvites();
        @NotNull final Map<Member, GuildInvite> memberInvitesHistory = memberInvitesCache.getHistory();

        switch (subCommandName) {
            case "view" -> {
                @NotNull final PEmbedBuilder embedBuilder = new PEmbedBuilder(guild, "Invitations de " + member.getUser().getName(), EmojiUtils.PAPERCLIP, Color.PINK);

                embedBuilder.setDescription("ℹ️ Les invitations dont les membres sont parties sont soustraites aux invitations globales pour obtenir le nombre de **vraies invitations**.");

                embedBuilder.addField("VRAIES invitations \uD83E\uDD2F", (IntegerUtils.formatWithEmojis(memberInvitesCache.getTrues()) + " **vraies invitations**"), false);
                embedBuilder.addField("Invitations globales \uD83C\uDF0D", String.format("**%d** invitations globales", memberInvitesCache.getTotal()), true);
                embedBuilder.addField("Membres parties \uD83E\uDD15", String.format("**%d** membres parties", memberInvitesCache.getLefts()), true);

                event.replyEmbeds(embedBuilder.build()).queue();
            }

            case "info" -> {
                Checks.check(!memberInvites.isEmpty() || !memberInvitesHistory.isEmpty(), "Impossible d'afficher les informations des invitations de ce membre puisque celui-ci n'a créé aucune invitations ou n'a aucun membres provenant de ses invitations.");

                @NotNull final PEmbedBuilder embedBuilder = new PEmbedBuilder(guild, "Informations des invitations de " + member.getUser().getName(), Emoji.fromUnicode("\uD83D\uDCCE"), Color.PINK);

                @Nullable final Invite favoriteInvite = memberInvites.stream().max(Comparator.comparingInt(Invite::getUses)).orElse(null);
                @NotNull final String favoriteInviteURL = favoriteInvite != null ? String.format(ObjectUtils.DISCORD_INVITE_FORMAT, favoriteInvite.getCode(), favoriteInvite.getUrl()) : "Aucune invitation";

                embedBuilder.setDescription(new StringJoiner("\n").add(String.format("\uD83D\uDC9D **Lien d'invitation préféré**: %s", favoriteInviteURL)).toString());

                if(!memberInvites.isEmpty()) {
                    embedBuilder.addField("Invitations créées \uD83D\uDD87️",
                            memberInvites.stream().map(anInvite -> new StringJoiner(" ").add("•")
                                    .add(String.format("[**%s**](%s)", anInvite.getCode(), anInvite.getUrl()))
                                    .add(ObjectUtils.DISCORD_TEXT_SEPARATOR)
                                    .add(String.format("**%s/%s** utilisations", anInvite.getUses(), anInvite.getMaxUses() == 0 ? "♾️" : anInvite.getMaxUses()))
                                    .add(anInvite.getMaxAge() > 0 ? String.format("(⌛ <t:%d:R>)", anInvite.getTimeCreated().plusSeconds(anInvite.getMaxAge()).toEpochSecond()) : "")
                                    .toString()).collect(Collectors.joining("\n")
                            ), false
                    );
                }

                if(!memberInvitesHistory.isEmpty()) {
                    embedBuilder.addField(String.format("Membres qui ont déja cliqués sur vos liens (%d) \uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66", memberInvitesHistory.size()), memberInvitesHistory.keySet().stream().map(Member::getAsMention).collect(Collectors.joining(", ")), false);
                }

                event.replyEmbeds(embedBuilder.build()).queue();
            }
        }
    }

}
