package com.agonkolgeci.playze_family_bot.utils.common;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ObjectUtils {

    @NotNull public static final SplittableRandom SPLITTABLE_RANDOM = new SplittableRandom();

    @NotNull public static final String REGEX_ALL_NON_DIGIT_CHARACTERS = "[^0-9]";
    @NotNull public static final String REGEX_ALL_NON_LETTERS = "[^A-Za-z]";

    @NotNull public static final String DISCORD_INVITE_FORMAT = "[discord.gg/%s](%s)";
    @NotNull public static final String DISCORD_TEXT_SEPARATOR = "—";

    @NotNull public static final String EXTERNAL_WEBSITE_URL = "https://playze.org/";
    @NotNull public static final String EXTERNAL_STREAM_URL = "https://twitch.tv/playze_";

    public static <K, V> V retrieveObjectOrElseGet(@NotNull Map<K, V> map, @NotNull K key, @NotNull Supplier<V> supplier) {
        return Objects.requireNonNullElseGet(map.getOrDefault(key, null), () -> {
            @NotNull final V value = supplier.get();

            map.put(key, value);

            return value;
        });
    }

    @NotNull
    public static <T> T retrieveRandomObject(@NotNull List<T> objects) {
        return objects.get(SPLITTABLE_RANDOM.nextInt(objects.size()));
    }

    @Nullable
    public static <E extends Enum<E>> E fetchObject(@NotNull Class<E> anEnum, @NotNull String name) {
        return Arrays.stream(anEnum.getEnumConstants()).filter(object -> object.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @Nullable
    public static <T> T requireNonExceptionOrElse(@NotNull Supplier<T> test, @Nullable T defaultValue) {
        try {
            return test.get();
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    @Nullable
    public static <T> T requireNonExceptionOrElseGet(@NotNull Supplier<T> test, @NotNull Supplier<T> defaultValue) {
        try {
            return test.get();
        } catch (Exception exception) {
            return defaultValue.get();
        }
    }

    @NotNull
    public static String formatFile(@NotNull String name, @NotNull String format) {
        return name + "." + format;
    }

    @NotNull
    public static List<String> formatObjects(@NotNull Object... objects) {
        return Arrays.stream(objects).map(object -> {
            if(object instanceof final File file) {
                return file.getPath().split(".cache/")[1];
            }

            if(object instanceof final Guild guild) {
                return String.format("guild_%s", guild.getId());
            }

            if(object instanceof final Member member) {
                return String.format("member_%s", member.getId());
            }

            if(object instanceof final User user) {
                return String.format("user_%s", user.getId());
            }

            return object.toString();
        }).collect(Collectors.toList());
    }

    @NotNull
    public static String joinObjects(@NotNull String delimiter, @NotNull Object... objects) {
        return String.join(delimiter, formatObjects(objects));
    }

    @NotNull
    public static String joinObjects(@NotNull String delimiter, @NotNull String base, @NotNull Object... objects) {
        return base + delimiter + joinObjects(delimiter, objects);
    }

    @NotNull
    public static String cutString(@NotNull String string, int maxLength) {
        return string.substring(0, Math.min(string.length(), maxLength));
    }

}
