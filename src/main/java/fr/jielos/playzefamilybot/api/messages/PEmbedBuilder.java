package fr.jielos.playzefamilybot.api.messages;

import fr.jielos.playzefamilybot.utils.common.ui.ColorUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.StringJoiner;

public class PEmbedBuilder extends net.dv8tion.jda.api.EmbedBuilder {

    @Getter @NotNull private final Guild guild;

    @Getter @NotNull private String name;
    @Getter @NotNull private Emoji emoji;
    @Getter @NotNull private Color color;
    @Getter private boolean thumbnail;

    public PEmbedBuilder(@NotNull Guild guild, @NotNull String name, @NotNull Emoji emoji, @NotNull Color color, boolean thumbnail) {
        this.guild = guild;

        this.name = name;
        this.emoji = emoji;
        this.color = color;
        this.thumbnail = thumbnail;

        setName(name);
        setColor(color);
        setThumbnail(thumbnail);
        setTimestamp(LocalDateTime.now());
    }

    public PEmbedBuilder(@NotNull Guild guild, @NotNull String name, @NotNull Emoji emoji, @NotNull Color color) {
        this(guild, name, emoji, color, true);
    }

    public PEmbedBuilder(@NotNull Guild guild, @NotNull String name, @NotNull Emoji emoji) {
        this(guild, name, emoji, ColorUtils.PRIMARY_COLOR, true);
    }

    public void setName(@NotNull String name) {
        this.name = name;

        setTitle(new StringJoiner(" ").add(name).add("—").add(emoji.getFormatted()).toString());
    }

    public void setName(@NotNull String name, @NotNull Object... objects) {
        setName(String.format(name, objects));
    }

    public void setEmoji(@NotNull Emoji emoji) {
        this.emoji = emoji;
    }

    public void setThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;

        if(thumbnail && guild.getIconUrl() != null) {
            setThumbnail(guild.getIconUrl());
        }
    }

}
