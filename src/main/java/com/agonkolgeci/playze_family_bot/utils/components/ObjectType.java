package com.agonkolgeci.playze_family_bot.utils.components;

import com.agonkolgeci.playze_family_bot.utils.common.ObjectUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public enum ObjectType {

    STRING("Texte", String.class) {
        @NotNull
        @Override
        public Object parse(@NotNull Guild guild, @NotNull String value) {
            return value;
        }
    },

    INTEGER("Valeur numérique", Integer.class) {
        @Nullable
        @Override
        public Object parse(@NotNull Guild guild, @NotNull String value) {
            return ObjectUtils.requireNonExceptionOrElse(() -> Integer.parseInt(value), null);
        }
    },

    BOOLEAN("Booléan", Boolean.class) {
        @Nullable
        @Override
        public Object parse(@NotNull Guild guild, @NotNull String value) {
            return ObjectUtils.requireNonExceptionOrElse(() -> Boolean.parseBoolean(value), null);
        }

        @NotNull
        @Override
        public String format(@NotNull Object object) {
            return ((boolean) object) ? "Activé" : "Désactivé";
        }
    },

    GUILD_CHANNEL("Salon", GuildChannel.class) {
        @Nullable
        @Override
        public Object parse(@NotNull Guild guild, @NotNull String value) {
            return guild.getGuildChannelById(value.replaceAll(ObjectUtils.REGEX_ALL_NON_DIGIT_CHARACTERS, ""));
        }
    },

    GUILD_CATEGORY("Catégorie", Category.class) {
        @Nullable
        @Override
        public Object parse(@NotNull Guild guild, @NotNull String value) {
            return guild.getCategoryById(value.replaceAll(ObjectUtils.REGEX_ALL_NON_DIGIT_CHARACTERS, ""));
        }
    },

    GUILD_TEXT_CHANNEL("Salon textuel", TextChannel.class) {
        @Nullable
        @Override
        public Object parse(@NotNull Guild guild, @NotNull String value) {
            return guild.getTextChannelById(value.replaceAll(ObjectUtils.REGEX_ALL_NON_DIGIT_CHARACTERS, ""));
        }
    },

    GUILD_VOICE_CHANNEL("Salon vocal", VoiceChannel.class) {
        @Nullable
        @Override
        public Object parse(@NotNull Guild guild, @NotNull String value) {
            return guild.getVoiceChannelById(value.replaceAll(ObjectUtils.REGEX_ALL_NON_DIGIT_CHARACTERS, ""));
        }
    },

    GUILD_ROLE("Rôle", Role.class) {
        @Nullable
        @Override
        public Object parse(@NotNull Guild guild, @NotNull String value) {
            return guild.getRoleById(value.replaceAll(ObjectUtils.REGEX_ALL_NON_DIGIT_CHARACTERS, ""));
        }
    };

    @Getter @NotNull private final String displayName;
    @Getter @NotNull private final Class<?> required;

    ObjectType(@NotNull String displayName, @NotNull Class<?> required) {
        this.required = required;
        this.displayName = displayName;
    }

    @Override
    @NotNull
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean isAssignableFrom(@NotNull Object object) {
        return required.isAssignableFrom(object.getClass());
    }

    @Nullable public abstract Object parse(@NotNull Guild guild, @NotNull String value);

    @NotNull
    public String format(@NotNull Object object) {
        if(object instanceof @NotNull final IMentionable mentionable) {
            return mentionable.getAsMention();
        }

        return object.toString();
    }

    @NotNull
    public String toRegistrable(@NotNull Object object) {
        if(object instanceof @NotNull final ISnowflake snowflake) {
            return snowflake.getId();
        }

        return object.toString();
    }

    @NotNull
    public static List<Command.Choice> getCommandChoices() {
        return Arrays.stream(values())
                .sorted(Comparator.comparing(ObjectType::getDisplayName))
                .map(input -> new Command.Choice(input.getDisplayName(), input.toString()))
                .collect(Collectors.toList());
    }

}
