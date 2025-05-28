package com.agonkolgeci.gebo.utils.common;

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

    @NotNull public static final String REGEX_ALL_NON_DIGIT_CHARACTERS = "[^0-9]";
    @NotNull public static final String REGEX_ALL_NON_LETTERS = "[^A-Za-z]";

    @NotNull public static final String DISCORD_INVITE_FORMAT = "[discord.gg/%s](%s)";
    @NotNull public static final String DISCORD_TEXT_SEPARATOR = "—";

    public static <K, V> V retrieveObjectOrElseGet(@NotNull Map<K, V> map, @NotNull K key, @NotNull Supplier<V> supplier) {
        return Objects.requireNonNullElseGet(map.getOrDefault(key, null), () -> {
            @NotNull final V value = supplier.get();

            map.put(key, value);

            return value;
        });
    }

    @NotNull
    @Deprecated
    public static String formatFile(@NotNull String name, @NotNull String format) {
        return name + "." + format;
    }

    @NotNull
    @Deprecated
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
    @Deprecated
    public static String joinObjects(@NotNull String delimiter, @NotNull Object... objects) {
        return String.join(delimiter, formatObjects(objects));
    }

    @NotNull
    @Deprecated
    public static String joinObjects(@NotNull String delimiter, @NotNull String base, @NotNull Object... objects) {
        return base + delimiter + joinObjects(delimiter, objects);
    }

    @NotNull
    public static String cutString(@NotNull String string, int maxLength) {
        return string.substring(0, Math.min(string.length(), maxLength));
    }

}
