package fr.jielos.playzefamilybot.client.guilds.settings.adapters;

import fr.jielos.playzefamilybot.api.commands.SlashCommandAdapter;
import fr.jielos.playzefamilybot.api.messages.PEmbedBuilder;
import fr.jielos.playzefamilybot.client.guilds.GuildCache;
import fr.jielos.playzefamilybot.client.guilds.GuildComponent;
import fr.jielos.playzefamilybot.client.guilds.settings.GuildSetting;
import fr.jielos.playzefamilybot.client.guilds.settings.SettingsController;
import fr.jielos.playzefamilybot.utils.common.ObjectUtils;
import fr.jielos.playzefamilybot.utils.common.ui.EmojiUtils;
import fr.jielos.playzefamilybot.utils.common.ui.MessageUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class CommandSettings extends GuildComponent implements SlashCommandAdapter {

    @NotNull private final SettingsController settingsController;

    public CommandSettings(@NotNull GuildCache guildCache, @NotNull SettingsController settingsController) {
        super(guildCache);

        this.settingsController = settingsController;
    }

    @NotNull
    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("settings", "Configurer les paramètres internes du serveur.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData("set", "Permet de définir un paramètre pour le serveur.").addOptions(
                                new OptionData(OptionType.STRING, "paramètre", "Nom du paramètre", true).addChoices(GuildSetting.retrieveCommandChoices()),
                                new OptionData(OptionType.STRING, "valeur", "Valeur du paramètre", true)
                        ),

                        new SubcommandData("delete", "Permet de supprimer un paramètre (remettre à zéro).").addOptions(
                                new OptionData(OptionType.STRING, "paramètre", "Nom du paramètre", true).addChoices(GuildSetting.retrieveCommandChoices())
                        ),

                        new SubcommandData("view", "Permet de visionner les paramètres actifs du serveur.")
                );
    }

    @Override
    public void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) throws Exception {
        @Nullable final Member member = event.getMember();
        if(member == null) return;

        Checks.check(member.hasPermission(Permission.ADMINISTRATOR), MessageUtils.MEMBER_DOES_NOT_HAVE_PERMISSIONS);

        @Nullable final String subCommandName = event.getSubcommandName();
        if(subCommandName == null) return;

        switch (subCommandName) {
            case "set", "delete" -> {
                @NotNull final String settingName = Objects.requireNonNull(event.getOption("paramètre")).getAsString();

                @Nullable final GuildSetting setting = ObjectUtils.fetchObject(GuildSetting.class, settingName);
                if(setting == null) throw new IllegalStateException("Ce paramètre n'existe pas.");

                switch (subCommandName) {
                    case "set" -> {
                        @NotNull final String newSettingValue = Objects.requireNonNull(event.getOption("valeur")).getAsString();
                        @NotNull final Object newSettingObject = settingsController.setSetting(setting, newSettingValue);

                        event.replyFormat(MessageUtils.success("Le paramètre [%s] vient d'être mis à jour: %s"), setting.getDisplayName(), setting.getObjectType().format(newSettingObject)).setSuppressEmbeds(true).setAllowedMentions(null).queue();
                    }

                    case "delete" -> {
                        Checks.check(settingsController.hasSetting(setting), "Le paramètre [%s] n'a pas encore été défini sur le serveur !", setting.getDisplayName());

                        settingsController.deleteSetting(setting);

                        event.replyFormat(MessageUtils.success("Le paramètre [%s] vient d'être supprimé.", "Notez que lorsqu'un paramètre n'a pas été défini sur le serveur, sa valeur par défaut est prise en compte s'il est nécessaire."), setting.getDisplayName()).setSuppressEmbeds(true).setAllowedMentions(null).queue();
                    }
                }
            }

            case "view" -> {
                @NotNull final Map<GuildSetting, Object> settings = settingsController.getSettings();
                Checks.check(!settings.isEmpty(), "Aucun paramètre n'est actif sur ce serveur.");

                @NotNull final PEmbedBuilder embedBuilder = new PEmbedBuilder(guild, "Paramètres actifs", EmojiUtils.EMOJI_SETTINGS, Color.GRAY);
                for(@NotNull final Map.Entry<GuildSetting, Object> entry : settings.entrySet()) {
                    @NotNull final GuildSetting setting = entry.getKey();
                    @NotNull final Object object = entry.getValue();

                    embedBuilder.addField(new StringJoiner(" ").add(setting.getDisplayName()).add(EmojiUtils.EMOJI_INBOX.getFormatted()).toString(), setting.getObjectType().format(object), true);
                }

                event.replyEmbeds(embedBuilder.build()).queue();
            }
        }
    }

}
