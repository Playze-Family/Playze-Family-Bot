package fr.jielos.playzefamilybot.client.guilds.settings.adapters;

import fr.jielos.playzefamilybot.api.commands.SlashCommandAdapter;
import fr.jielos.playzefamilybot.api.messages.PEmbedBuilder;
import fr.jielos.playzefamilybot.client.guilds.GuildCache;
import fr.jielos.playzefamilybot.client.guilds.GuildComponent;
import fr.jielos.playzefamilybot.client.guilds.settings.SettingsController;
import fr.jielos.playzefamilybot.utils.common.ObjectUtils;
import fr.jielos.playzefamilybot.utils.common.ui.EmojiUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

public class CommandStats extends GuildComponent implements SlashCommandAdapter {

    @NotNull private final SettingsController settingsController;

    public CommandStats(@NotNull GuildCache guildCache, @NotNull SettingsController settingsController) {
        super(guildCache);

        this.settingsController = settingsController;
    }

    @NotNull
    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("stats", "Visualiser les statistiques du serveur.").setGuildOnly(true);
    }

    @Override
    public void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) throws Exception {
        @Nullable final Guild guild = event.getGuild();
        if(guild == null) return;

        @Nullable final Member guildOwner = guild.getOwner();
        if(guildOwner == null) return;

        @NotNull final PEmbedBuilder embedBuilder = new PEmbedBuilder(guild, "Statistiques de ce serveur", EmojiUtils.EMOJI_STATS);

        embedBuilder.setDescription(new StringJoiner("\n").add("Observez toutes les statistiques de ce serveur \uD83D\uDCCA").toString());

        embedBuilder.addField("Propriétaire \uD83D\uDC51", guildOwner.getAsMention(), true);
        embedBuilder.addField("Membres \uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66", String.format("**%d** membres", guild.getMembers().stream().filter(anMember -> !anMember.getUser().isBot()).count()), true);
        embedBuilder.addField("Robots \uD83D\uDD27", String.format("**%d** robots", guild.getMembers().stream().filter(anMember -> anMember.getUser().isBot()).count()), true);

        embedBuilder.addField("Salons \uD83D\uDCAC", new StringJoiner("\n").add(String.format("%s **%d** catégories", ObjectUtils.DISCORD_TEXT_SEPARATOR, guild.getCategories().size())).add(String.format("**%d** salons textuels", guild.getTextChannels().size())).add(String.format("**%d** salons vocaux", guild.getVoiceChannels().size())).toString(), true);
        embedBuilder.addField("Émojis \uD83C\uDF1F", new StringJoiner("\n").add(String.format("%s **%d** émojis", ObjectUtils.DISCORD_TEXT_SEPARATOR, guild.getEmojis().size())).add(String.format("**%d** non animés", guild.getEmojis().stream().filter(emote -> !emote.isAnimated()).count())).add(String.format("**%d** animés", guild.getEmojis().stream().filter(RichCustomEmoji::isAnimated).count())).toString(), true);
        embedBuilder.addField("Boosts \uD83C\uDF8A", new StringJoiner("\n").add(String.format("%s Niveau **%d**", ObjectUtils.DISCORD_TEXT_SEPARATOR, guild.getBoostTier().getKey())).add(String.format("**%d** boosts", guild.getBoostCount())).add(String.format("**%d** boosteurs", guild.getBoosters().size())).toString(), true);

        embedBuilder.setFooter("\uD83C\uDF80 Création du serveur le");
        embedBuilder.setTimestamp(guild.getTimeCreated());

        event.replyEmbeds(embedBuilder.build()).queue();
    }

}
