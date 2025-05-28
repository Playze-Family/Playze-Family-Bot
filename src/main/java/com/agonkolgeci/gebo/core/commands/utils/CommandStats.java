package com.agonkolgeci.gebo.core.commands.utils;

import com.agonkolgeci.gebo.GeboBot;
import com.agonkolgeci.gebo.core.commands.SlashCommandAdapter;
import com.agonkolgeci.gebo.utils.common.ObjectUtils;
import com.agonkolgeci.gebo.utils.common.ui.EmojiUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

public class CommandStats implements SlashCommandAdapter {

    @NotNull private final GeboBot instance;

    public CommandStats(@NotNull GeboBot instance) {
        this.instance = instance;
    }

    @NotNull
    @Override
    public SlashCommandData getSlashCommandData() {
        return Commands.slash("stats", "Visualiser les statistiques du serveur.")
                .setContexts(InteractionContextType.GUILD);
    }

    @Override
    public void onSlashCommandComplete(@NotNull SlashCommandInteractionEvent event) throws RuntimeException {
        @Nullable final Guild guild = event.getGuild();
        if(guild == null) return;

        @Nullable final Member guildOwner = guild.getOwner();
        if(guildOwner == null) return;

        @NotNull final EmbedBuilder embed = new EmbedBuilder().setTitle("Statistiques de ce serveur " + EmojiUtils.STATS.getFormatted()).setThumbnail(guild.getIconUrl());

        embed.addField("Propriétaire \uD83D\uDC51", guildOwner.getAsMention(), true);
        embed.addField("Membres \uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC66", String.format("**%d** membres", guild.getMembers().stream().filter(anMember -> !anMember.getUser().isBot()).count()), true);
        embed.addField("Robots \uD83D\uDD27", String.format("**%d** robots", guild.getMembers().stream().filter(anMember -> anMember.getUser().isBot()).count()), true);

        embed.addField("Salons \uD83D\uDCAC", new StringJoiner("\n").add(String.format("%s **%d** catégories", ObjectUtils.DISCORD_TEXT_SEPARATOR, guild.getCategories().size())).add(String.format("**%d** salons textuels", guild.getTextChannels().size())).add(String.format("**%d** salons vocaux", guild.getVoiceChannels().size())).toString(), true);
        embed.addField("Émojis \uD83C\uDF1F", new StringJoiner("\n").add(String.format("%s **%d** émojis", ObjectUtils.DISCORD_TEXT_SEPARATOR, guild.getEmojis().size())).add(String.format("**%d** non animés", guild.getEmojis().stream().filter(emote -> !emote.isAnimated()).count())).add(String.format("**%d** animés", guild.getEmojis().stream().filter(RichCustomEmoji::isAnimated).count())).toString(), true);
        embed.addField("Boosts \uD83C\uDF8A", new StringJoiner("\n").add(String.format("%s Niveau **%d**", ObjectUtils.DISCORD_TEXT_SEPARATOR, guild.getBoostTier().getKey())).add(String.format("**%d** boosts", guild.getBoostCount())).add(String.format("**%d** boosteurs", guild.getBoosters().size())).toString(), true);

        embed.setFooter("\uD83C\uDF80 Création du serveur le");
        embed.setTimestamp(guild.getTimeCreated());

        event.replyEmbeds(embed.build()).queue();
    }

}
