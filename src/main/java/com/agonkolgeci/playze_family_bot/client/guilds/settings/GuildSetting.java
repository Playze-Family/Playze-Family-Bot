package com.agonkolgeci.playze_family_bot.client.guilds.settings;

import com.agonkolgeci.playze_family_bot.utils.components.ObjectType;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public enum GuildSetting {

    GUILD_MULTIPLIER_XP_GAIN(ObjectType.INTEGER, "Multiplicateur de gain d'éxperience", 1),
    GUILD_INTERVAL_XP_LEVEL_UP(ObjectType.INTEGER, "Intervalle d'annonce de niveau supérieur", 1),

    GUILD_PUBLIC_VOICE_CHANNELS_CREATOR(ObjectType.GUILD_VOICE_CHANNEL, "Salon destiné à la création de salon public"),
    GUILD_PUBLIC_VOICE_CHANNELS_NAME(ObjectType.STRING, "Nom du salon vocal public", "Salon vocal"),

    GUILD_MEMBERS_CUSTOM_ROLE(ObjectType.GUILD_ROLE, "Rôle du grade personnalisé"),

    GUILD_COMMANDS_CHANNEL(ObjectType.GUILD_TEXT_CHANNEL, "Salon où les commandes sont exécutées"),
    GUILD_MEMBERS_CHANNEL(ObjectType.GUILD_TEXT_CHANNEL, "Salon des nouveaux arrivants / membres qui partent"),
    GUILD_LEVELS_ANNOUNCEMENTS(ObjectType.GUILD_TEXT_CHANNEL, "Salon des annonces de niveaux"),

    GUILD_BOT_SPEAKING_RECEIVER(ObjectType.GUILD_TEXT_CHANNEL, "Salon où le robot interceptera les messages"),
    GUILD_BOT_SPEAKING_TARGET(ObjectType.GUILD_TEXT_CHANNEL, "Salon où le robot redirigera les messages interceptés");

    @Getter @NotNull private final ObjectType objectType;
    @Getter @NotNull private final String displayName;
    @Getter @Nullable private final Object defaultValue;

    GuildSetting(@NotNull ObjectType objectType, @NotNull String displayName, @Nullable Object defaultValue) {
        this.objectType = objectType;
        this.displayName = displayName;

        if(defaultValue != null) {
            if(!objectType.isAssignableFrom(defaultValue)) {
                throw new IllegalArgumentException("The default value of the parameter is not correct.");
            }
        }

        this.defaultValue = defaultValue;
    }

    GuildSetting(@NotNull ObjectType objectType, @NotNull String displayName) {
        this(objectType, displayName, null);
    }

    @Override
    @NotNull
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
    @NotNull
    public static List<Command.Choice> retrieveCommandChoices() {
        return Arrays.stream(values())
                .sorted(Comparator.comparing(setting -> setting.getObjectType().getDisplayName()))
                .map(setting -> new Command.Choice(setting.getObjectType().getDisplayName() + ": " + setting.getDisplayName(), setting.toString()))
                .collect(Collectors.toList());
    }

}