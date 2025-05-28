package com.agonkolgeci.gebo.core.commands.settings;

import com.agonkolgeci.gebo.GeboBot;
import com.agonkolgeci.gebo.core.commands.SlashCommandAdapter;
import com.agonkolgeci.gebo.core.settings.GuildSetting;
import com.agonkolgeci.gebo.core.settings.GuildSettings;
import com.agonkolgeci.gebo.utils.common.ui.EmojiUtils;
import com.agonkolgeci.gebo.utils.common.ui.MessageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unchecked")
public class CommandSettings implements SlashCommandAdapter {

    private final GeboBot instance;

    public CommandSettings(GeboBot instance) {
        this.instance = instance;
    }

    @NotNull
    @Override
    public SlashCommandData getSlashCommandData() {
        final List<Command.Choice> SETTINGS_CHOICES = GuildSetting.ALL_SETTINGS.values().stream()
                .sorted(Comparator.comparing(guildSetting -> guildSetting.getCategory().getLabel()))
                .map(setting -> new Command.Choice(setting.getCategory().getLabel() + " : " + setting.getName(), setting.getKey()))
                .toList();

        return Commands.slash("settings", "Configurer les paramètres internes du serveur.")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .setContexts(InteractionContextType.GUILD)
                .addSubcommands(
                        new SubcommandData("set", "Permet de définir un paramètre pour le serveur.").addOptions(
                                new OptionData(OptionType.STRING, "paramètre", "Nom du paramètre", true).addChoices(SETTINGS_CHOICES),
                                new OptionData(OptionType.STRING, "valeur", "Valeur du paramètre", true)
                        ),

                        new SubcommandData("remove", "Permet de supprimer/réinitialiser un paramètre.").addOptions(
                                new OptionData(OptionType.STRING, "paramètre", "Nom du paramètre", true).addChoices(SETTINGS_CHOICES)
                        ),

                        new SubcommandData("view", "Permet de visionner les paramètres actifs du serveur.")
                );
    }

    @Override
    public void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) throws RuntimeException {
        @Nullable final Guild guild = event.getGuild();
        if(guild == null) return;

        @Nullable final Member member = event.getMember();
        if(member == null) return;

        @Nullable final String subCommandName = event.getSubcommandName();
        if(subCommandName == null) return;

        @NotNull final GuildSettings settings = instance.getSettingsManager().getSettings(guild);

        switch (subCommandName) {
            case "set", "remove" -> {
                @Nullable final GuildSetting<?> setting = GuildSetting.ALL_SETTINGS.getOrDefault(Objects.requireNonNull(event.getOption("paramètre")).getAsString(), null);
                if(setting == null) throw new IllegalStateException("Ce paramètre n'existe pas.");

                switch (subCommandName) {
                    case "set" -> {
                        @NotNull final Object newValue = setting.serialize(guild, Objects.requireNonNull(event.getOption("valeur")).getAsString());
                        if(newValue == null) throw new IllegalStateException("Valeur incorrecte pour le paramètre: " + setting.getName());

                        settings.setSetting(setting, newValue);

                        event.replyFormat(MessageUtils.success("Le paramètre **%s** vient d'être mis à jour: **%s**"), setting.getName(), ((GuildSetting<Object>) setting).format(guild, newValue)).setSuppressEmbeds(true).setAllowedMentions(null).queue();
                    }

                    case "remove" -> {
                        Checks.check(settings.getSetting(setting) != null, "Le paramètre **%s** n'a pas encore été défini sur le serveur !", setting.getName());

                        settings.removeSetting(setting);

                        event.replyFormat(MessageUtils.success("Le paramètre **%s** vient d'être supprimé."), setting.getName()).setSuppressEmbeds(true).setAllowedMentions(null).queue();
                    }
                }
            }

            case "view" -> {
                CompletableFuture.runAsync(() -> {
                    event.deferReply().queue(interactionHook -> {
                        @NotNull final EmbedBuilder embed = new EmbedBuilder().setTitle("Paramètres du serveur " + EmojiUtils.SETTINGS.getFormatted()).setTimestamp(Instant.now()).setThumbnail(guild.getIconUrl());

                        final AtomicReference<GuildSetting.Category> lastCategory = new AtomicReference<>(null);

                        GuildSetting.ALL_SETTINGS.values().stream().sorted(Comparator.comparing(s -> s.getCategory().getLabel())).forEach(setting -> {
                            // TODO : page system?
                            if(embed.getFields().size() >= MessageEmbed.MAX_FIELD_AMOUNT) return;

                            final Object value = settings.getSetting(setting).join();
                            if(value == null) return;

                            final GuildSetting.Category lc = lastCategory.getAndSet(setting.getCategory());
                            if(lc != null && lc != setting.getCategory()) {
                                embed.addBlankField(false);
                            }

                            embed.addField(setting.getName() + " " + EmojiUtils.INBOX.getFormatted(), ((GuildSetting<Object>) setting).format(guild, value) + " " + (setting.getDefaultValue() == value ? "(par défaut)" : ""), true);
                        });

                        interactionHook.editOriginalEmbeds(embed.build()).queue();
                    });
                });
            }
        }
    }

}
