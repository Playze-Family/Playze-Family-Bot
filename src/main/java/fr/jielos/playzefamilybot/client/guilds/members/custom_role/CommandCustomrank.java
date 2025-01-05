package fr.jielos.playzefamilybot.client.guilds.members.custom_role;

import com.vdurmont.emoji.EmojiManager;
import fr.jielos.playzefamilybot.api.commands.SlashCommandAdapter;
import fr.jielos.playzefamilybot.client.guilds.GuildCache;
import fr.jielos.playzefamilybot.client.guilds.GuildComponent;
import fr.jielos.playzefamilybot.client.guilds.members.MemberCache;
import fr.jielos.playzefamilybot.client.guilds.members.MembersController;
import fr.jielos.playzefamilybot.client.guilds.settings.GuildSetting;
import fr.jielos.playzefamilybot.utils.common.ui.ColorUtils;
import fr.jielos.playzefamilybot.utils.common.ui.MessageUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class CommandCustomrank extends GuildComponent implements SlashCommandAdapter {

    @NotNull private final MembersController membersController;

    public CommandCustomrank(@NotNull GuildCache guildCache, @NotNull MembersController membersController) {
        super(guildCache);

        this.membersController = membersController;
    }

    @NotNull
    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("customrank", "Éditer son grade personnalisé sur le serveur.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("edit", "Éditer son grade personnalisé.").addOptions(
                                new OptionData(OptionType.STRING, "nom", "Nom d'affichage du grade personnalisé", true),
                                new OptionData(OptionType.STRING, "couleur", "Couleur du grade personnalisé (color picker sur google)", true),
                                new OptionData(OptionType.STRING, "émoji", "Émoji du grade personnalisé (émoji discords)", true)
                        )
                );
    }

    @Override
    public void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) throws Exception {
        @Nullable final Guild guild = event.getGuild();
        if(guild == null) return;

        @Nullable final Member member = event.getMember();
        if(member == null) return;

        @Nullable final String subCommandName = event.getSubcommandName();
        if(subCommandName == null) return;

        @NotNull final MemberCache memberCache = membersController.retrieveMemberCache(member);
        @NotNull final MemberCustomRoleCache memberCustomRoleCache = memberCache.retrieveCustomRoleCache();

        @Nullable final Role guildCustomRole = guildCache.getSettingsController().retrieveSetting(GuildSetting.GUILD_MEMBERS_CUSTOM_ROLE);
        Checks.check(guildCustomRole != null, "Les rôles personnalisés ne sont pas configurés sur ce serveur.");
        Checks.check(memberCustomRoleCache.hasPermissions(), MessageUtils.MEMBER_DOES_NOT_HAVE_PERMISSIONS);
        Checks.check(canManageRoles(selfMember, guildCustomRole), MessageUtils.BOT_INSUFFICIENT_PERMISSIONS);

        switch (subCommandName) {
            case "edit" -> {
                @Nullable final String name = Objects.requireNonNull(event.getOption("nom")).getAsString();
                @Nullable final String color = Objects.requireNonNull(event.getOption("couleur")).getAsString();
                @Nullable final String emoji = Objects.requireNonNull(event.getOption("émoji")).getAsString();

                Checks.check(name.length() <= 25, "Le taille du nom du grade personnalisé ne doit pas dépasser 25 caractères.");
                Checks.check(EmojiManager.isEmoji(emoji), "Vous devez entrer un Émoji valide ! Nous vous conseillons d'utiliser les émojis Discord ou alors de vous rendre sur [ce site répertoriant des émojis compatibles](https://getemoji.com/). Sachez aussi qu'il est possible que certains émojis ne sont pas inscrits dans ma base de données.");
                Checks.check(ColorUtils.isHEXColor(color), "Vous devez entrer une couleur correcte ! Utilisez [l'outil disponible sur votre moteur de recherche](https://www.google.com/search?q=color+picker), et copier la valeur 'HEX' de votre couleur.");

                event.deferReply().queue(interactionHook -> {
                    memberCustomRoleCache.updateRole(name, emoji, color).thenAccept(customRole -> {
                        interactionHook.editOriginalFormat(MessageUtils.success("Vous venez de vous refaire une toute nouvelle beauté: %s !"), customRole.getAsMention()).queue();
                    });
                });
            }
        }
    }

    public boolean canManageRoles(@NotNull Member member, @NotNull Role... roles) {
        return member.hasPermission(Permission.MANAGE_ROLES) && member.getRoles().stream().anyMatch(memberRole -> Arrays.stream(roles).anyMatch(targetRole -> memberRole.getPosition() > targetRole.getPosition()));
    }

}
