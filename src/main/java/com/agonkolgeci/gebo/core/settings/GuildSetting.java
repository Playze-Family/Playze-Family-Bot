package com.agonkolgeci.gebo.core.settings;

import com.agonkolgeci.gebo.utils.common.ObjectUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class GuildSetting<T> {

    public static final Map<String, GuildSetting<?>> ALL_SETTINGS = new HashMap<>();

    public static final GuildSetting<TextChannel> GENERAL_COMMANDS_CHANNEL = register(GuildSetting.ofTextChannel("commands_channel", Category.GENERAL, "Salon où les commandes sont exécutées"));
    public static final GuildSetting<TextChannel> GENERAL_NEW_MEMBERS_CHANNEL = register(GuildSetting.ofTextChannel("members_channel", Category.GENERAL, "Salon des nouveaux arrivants / membres qui partent"));
    public static final GuildSetting<Role> GENERAL_CUSTOM_ROLE = register(GuildSetting.ofRole("custom_role", Category.GENERAL, "Rôle du grade personnalisé"));

    public static final GuildSetting<VoiceChannel> AUTO_CHANNELS_CREATOR_CHANNEL = register(GuildSetting.ofVoiceChannel("public_voice_creator", Category.AUTO_CHANNELS, "Salon destiné à la création de salon public"));
    public static final GuildSetting<String> AUTO_CHANNELS_CREATE_NAME = register(GuildSetting.ofString("public_voice_name", Category.AUTO_CHANNELS, "Nom du salon vocal public").defaultValue("Salon vocal"));

    public static final GuildSetting<Integer> LEVELS_MULTIPLIER_XP_GAIN = register(GuildSetting.ofInteger("multiplier_xp_gain", Category.LEVELS, "Multiplicateur de gain d'expérience").defaultValue(1));
    public static final GuildSetting<Integer> LEVELS_INTERVAL_XP_LEVEL_UP = register(GuildSetting.ofInteger("interval_xp_level_up", Category.LEVELS, "Intervalle d'annonce de niveau supérieur").defaultValue(1));
    public static final GuildSetting<TextChannel> LEVELS_ANNOUNCEMENTS_CHANNEL = register(GuildSetting.ofTextChannel("levels_announcements", Category.LEVELS, "Salon des annonces de niveaux"));

    public static final GuildSetting<TextChannel> REDIRECTIONS_RECEIVER = register(GuildSetting.ofTextChannel("bot_speaking_receiver", Category.REDIRECTIONS, "Salon où le robot interceptera les messages"));
    public static final GuildSetting<TextChannel> REDIRECTIONS_SPEAKING_TARGET = register(GuildSetting.ofTextChannel("bot_speaking_target", Category.REDIRECTIONS, "Salon où le robot redirigera les messages interceptés"));

    public static <T> GuildSetting<T> register(GuildSetting<T> setting) {
        ALL_SETTINGS.put(setting.getKey(), setting);

        return setting;
    }

    public static GuildSetting<String> ofString(String key, Category category, String name) {
        return new GuildSetting<>(key, category, name) {
            @Override
            public String serialize(Guild guild, String value) {
                return value;
            }
        };
    }

    public static GuildSetting<Boolean> ofBoolean(String key, Category category, String name) {
        return new GuildSetting<>(key, category, name) {
            @Override
            public Boolean serialize(Guild guild, String value) {
                return Boolean.parseBoolean(value);
            }

            @Override
            public String deserialize(Guild guild, Boolean value) {
                return value ? "Activé" : "Désactivé";
            }
        };
    }

    public static GuildSetting<Integer> ofInteger(String key, Category category, String name) {
        return new GuildSetting<>(key, category, name) {
            @Override
            public Integer serialize(Guild guild, String value) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        };
    }

    public static GuildSetting<net.dv8tion.jda.api.entities.channel.concrete.Category> ofCategory(String key, Category category, String name) {
        return new GuildSetting<>(key, category, name) {
            @Override
            public net.dv8tion.jda.api.entities.channel.concrete.Category serialize(Guild guild, String value) {
                return guild.getCategoryById(value.replaceAll(ObjectUtils.REGEX_ALL_NON_DIGIT_CHARACTERS, ""));
            }
        };
    }

    public static GuildSetting<GuildChannel> ofGuildChannel(String key, Category category, String name) {
        return new GuildSetting<>(key, category, name) {
            @Override
            public GuildChannel serialize(Guild guild, String value) {
                return guild.getGuildChannelById(value.replaceAll(ObjectUtils.REGEX_ALL_NON_DIGIT_CHARACTERS, ""));
            }
        };
    }

    public static GuildSetting<TextChannel> ofTextChannel(String key, Category category, String name) {
        return new GuildSetting<>(key, category, name) {
            @Override
            public TextChannel serialize(Guild guild, String value) {
                return guild.getTextChannelById(value.replaceAll(ObjectUtils.REGEX_ALL_NON_DIGIT_CHARACTERS, ""));
            }
        };
    }

    public static GuildSetting<VoiceChannel> ofVoiceChannel(String key, Category category, String name) {
        return new GuildSetting<>(key, category, name) {
            @Override
            public VoiceChannel serialize(Guild guild, String value) {
                return guild.getVoiceChannelById(value.replaceAll(ObjectUtils.REGEX_ALL_NON_DIGIT_CHARACTERS, ""));
            }
        };
    }

    public static GuildSetting<Role> ofRole(String key, Category category, String name) {
        return new GuildSetting<>(key, category, name) {
            @Override
            public Role serialize(Guild guild, String value) {
                return guild.getRoleById(value.replaceAll(ObjectUtils.REGEX_ALL_NON_DIGIT_CHARACTERS, ""));
            }
        };
    }

    private final String key;
    private final Category category;
    private final String name;

    @Nullable private T defaultValue;

    public GuildSetting(String key, Category category, String name) {
        this.key = key;
        this.category = category;
        this.name = name;
    }

    public abstract T serialize(Guild guild, String value);

    public String deserialize(Guild guild, T value) {
        if(value instanceof ISnowflake snowflake) return snowflake.getId();

        return value.toString();
    }

    public String format(Guild guild, T value) {
        if(value instanceof IMentionable mentionable) return mentionable.getAsMention();

        return value.toString();
    }

    public @NotNull GuildSetting<T> defaultValue(T defaultValue) {
        this.defaultValue = defaultValue;

        return this;
    }

    public enum Category {

        GENERAL("Général"),
        AUTO_CHANNELS("Salons automatiques"),
        LEVELS("Niveaux"),
        REDIRECTIONS("Redirections"),
        ;

        @Getter @NotNull private final String label;

        Category(@NotNull String label) {
            this.label = label;
        }
    }

}
