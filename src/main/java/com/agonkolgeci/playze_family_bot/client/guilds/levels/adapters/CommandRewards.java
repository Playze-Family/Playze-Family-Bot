package com.agonkolgeci.playze_family_bot.client.guilds.levels.adapters;

import com.agonkolgeci.playze_family_bot.api.commands.SlashCommandAdapter;
import com.agonkolgeci.playze_family_bot.client.guilds.GuildCache;
import com.agonkolgeci.playze_family_bot.client.guilds.GuildComponent;
import com.agonkolgeci.playze_family_bot.client.guilds.levels.LevelsController;
import com.agonkolgeci.playze_family_bot.utils.common.ui.MessageUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
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

public class CommandRewards extends GuildComponent implements SlashCommandAdapter {

    @NotNull public static final String REWARD_ALREADY_EXISTS = "Il existe déjà cette récompense; veuillez fournir un rôle ou niveau différent.";
    @NotNull public static final String REWARD_DOES_NOT_EXIST = "Impossible de trouver cette récompense; vérifiez son rôle et réessayer.";

    @NotNull private final LevelsController levelsController;

    public CommandRewards(@NotNull GuildCache guildCache, @NotNull LevelsController levelsController) {
        super(guildCache);

        this.levelsController = levelsController;
    }

    @NotNull
    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("rewards", "Configurer les récompenses des niveaux du serveur.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("add", "Permet d'ajouter un récompense à un niveau spécifique.").addOptions(
                                new OptionData(OptionType.INTEGER, "niveau", "Niveau spécifique", true),
                                new OptionData(OptionType.ROLE, "rôle", "Rôle spécifique", true)
                        ),

                        new SubcommandData("remove", "Permet de retirer une récompense présente.").addOptions(
                                new OptionData(OptionType.INTEGER, "niveau", "Niveau spécifique", false),
                                new OptionData(OptionType.ROLE, "rôle", "Rôle spécifique", false)
                        )
                );
    }

    @Override
    public void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) throws Exception {
        @Nullable final Guild guild = event.getGuild();
        if(guild == null) return;

        @Nullable final Member member = event.getMember();
        if(member == null) return;

        Checks.check(member.hasPermission(Permission.ADMINISTRATOR), MessageUtils.MEMBER_DOES_NOT_HAVE_PERMISSIONS);

        @Nullable final String subCommandName = event.getSubcommandName();
        if(subCommandName == null) return;

        switch (subCommandName) {
            case "add" -> {
                @NotNull final Role role = Objects.requireNonNull(event.getOption("rôle")).getAsRole();
                final int level = Objects.requireNonNull(event.getOption("niveau")).getAsInt();

                Checks.check(levelsController.getReward(level, role) == null, REWARD_ALREADY_EXISTS);

                levelsController.addReward(level, role);

                event.replyFormat(MessageUtils.success("Vous venez d'ajouter une nouvelle récompense: Rôle %s au [niveau %s]."), role.getAsMention(), level).setSuppressEmbeds(true).queue();
            }

            case "remove" -> {
                @Nullable  final OptionMapping optionalLevel = event.getOption("level");
                @Nullable final OptionMapping optionalRole = event.getOption("rôle");

                if(optionalRole != null && optionalLevel != null) {
                    @NotNull  final Role role = optionalRole.getAsRole();
                    final int level = optionalLevel.getAsInt();

                    Checks.check(levelsController.getReward(level, role) != null, REWARD_DOES_NOT_EXIST);

                    levelsController.removeReward(level, role);

                    event.replyFormat(MessageUtils.success("Vous venez de retirer la récompense Rôle %s au [niveau %s]."), role.getAsMention(), level).setSuppressEmbeds(true).setAllowedMentions(null).queue();
                } else if(optionalRole != null && optionalLevel == null) {
                    @NotNull final Role role = optionalRole.getAsRole();

                    Checks.check(!levelsController.getRewards(role).isEmpty(), REWARD_DOES_NOT_EXIST);

                    levelsController.removeReward(role);

                    event.replyFormat(MessageUtils.success("Vous venez de retirer le(s) récompense(s) pour le rôle %s."), role.getAsMention()).setSuppressEmbeds(true).setAllowedMentions(null).queue();
                } else if(optionalRole == null && optionalLevel != null) {
                    final int level = optionalLevel.getAsInt();

                    Checks.check(!levelsController.getRewards(level).isEmpty(), REWARD_DOES_NOT_EXIST);

                    levelsController.removeReward(level);

                    event.replyFormat(MessageUtils.success("Vous venez de retirer le(s) récompense(s) pour le niveau %d."), level).setSuppressEmbeds(true).setAllowedMentions(null).queue();
                }
            }
        }
    }

}
