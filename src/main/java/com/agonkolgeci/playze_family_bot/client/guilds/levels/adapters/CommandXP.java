package com.agonkolgeci.playze_family_bot.client.guilds.levels.adapters;

import com.agonkolgeci.playze_family_bot.api.commands.SlashCommandAdapter;
import com.agonkolgeci.playze_family_bot.client.guilds.GuildCache;
import com.agonkolgeci.playze_family_bot.client.guilds.GuildComponent;
import com.agonkolgeci.playze_family_bot.client.guilds.levels.LevelsController;
import com.agonkolgeci.playze_family_bot.client.guilds.members.MemberCache;
import com.agonkolgeci.playze_family_bot.client.guilds.members.profile.MemberProfileCache;
import com.agonkolgeci.playze_family_bot.utils.common.ui.MessageUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CommandXP extends GuildComponent implements SlashCommandAdapter {

    @NotNull private final LevelsController levelsController;

    public CommandXP(@NotNull GuildCache guildCache, @NotNull LevelsController levelsController) {
        super(guildCache);

        this.levelsController = levelsController;
    }

    @NotNull
    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("xp", "Gérer l'expérience des membres du serveur.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("add", "Ajouter de l'expérience à un membre.").addOptions(
                                new OptionData(OptionType.USER, "membre", "Membre spécifique", true),
                                new OptionData(OptionType.INTEGER, "xp", "Valeur en expérience", true),
                                new OptionData(OptionType.INTEGER, "level", "Valeur en niveau", false)
                        ),

                        new SubcommandData("subtract", "Retirer de l'expérience à un membre.").addOptions(
                                new OptionData(OptionType.USER, "membre", "Membre spécifique", true),
                                new OptionData(OptionType.INTEGER, "xp", "Valeur en expérience", true),
                                new OptionData(OptionType.INTEGER, "level", "Valeur en niveau", false)
                        ),

                        new SubcommandData("set", "Définir l'expérience d'un membre.").addOptions(
                                new OptionData(OptionType.USER, "membre", "Membre spécifique", true),
                                new OptionData(OptionType.INTEGER, "xp", "Valeur en expérience", true),
                                new OptionData(OptionType.INTEGER, "level", "Valeur en niveau", false)
                        )
                );
    }

    @Override
    public void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) throws Exception {
        @Nullable final Guild guild = event.getGuild();
        if(guild == null) return;

        @Nullable final Member member = event.getMember();
        if(member == null) return;
        if(!member.hasPermission(Permission.ADMINISTRATOR)) return;

        @Nullable final String subCommandName = event.getSubcommandName();
        if(subCommandName == null) return;

        @Nullable final Member targetMember = Objects.requireNonNull(event.getOption("membre")).getAsMember();
        if(targetMember == null) return;

        Checks.check(!targetMember.getUser().isBot(), MessageUtils.MEMBER_MUST_BE_HUMAN);

        @Nullable final OptionMapping optionLevel = event.getOption("level");

        final int xpValue = Objects.requireNonNull(event.getOption("xp")).getAsInt();
        final int levelValue = optionLevel != null ? optionLevel.getAsInt() : 0;

        @NotNull final MemberCache targetMemberCache = guildCache.getMembersController().retrieveMemberCache(targetMember);
        @NotNull final MemberProfileCache targetMemberProfileCache = targetMemberCache.retrieveProfileCache();

        switch (subCommandName) {
            case "add" -> targetMemberProfileCache.addXP(levelValue, xpValue);
            case "subtract" -> targetMemberProfileCache.subtractXP(levelValue, xpValue);
            case "set" -> targetMemberProfileCache.setProperties(optionLevel != null ? levelValue : targetMemberProfileCache.getLevel(), xpValue);
        }

        event.replyFormat(MessageUtils.success("Vous venez de mettre à jour les niveaux du membre %s: [Niveau %d] avec [%d points d'expériences]."), targetMember.getAsMention(), targetMemberProfileCache.getLevel(), targetMemberProfileCache.getXp()).setSuppressEmbeds(true).queue();
    }

}
