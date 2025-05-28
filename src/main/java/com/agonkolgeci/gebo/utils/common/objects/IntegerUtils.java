package com.agonkolgeci.gebo.utils.common.objects;

import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

public class IntegerUtils {

    public static final NumberFormat NUMBER_FORMATTER;

    static {
        NUMBER_FORMATTER = NumberFormat.getCompactNumberInstance(Locale.FRENCH, NumberFormat.Style.SHORT);
        NUMBER_FORMATTER.setMaximumFractionDigits(2);
    }

    public static int parseInt(@Nullable String value) {
        try {
            Checks.notNull(value, "Cannot parse null value to integer !");

            return Integer.parseInt(value);
        } catch (Exception exception) {
            return 0;
        }
    }

    @NotNull
    public static String formatWithEmojis(int integer) {
        @NotNull final String[] strings = String.valueOf(integer).split("");
        @NotNull final StringBuilder stringBuilder = new StringBuilder();

        for(@NotNull final String string : strings) {
            stringBuilder.append(string
                    .replaceAll("0", ":zero:")
                    .replaceAll("1", ":one:")
                    .replaceAll("2", ":two:")
                    .replaceAll("3", ":three:")
                    .replaceAll("4", ":four:")
                    .replaceAll("5", ":five:")
                    .replaceAll("6", ":six:")
                    .replaceAll("7", ":seven:")
                    .replaceAll("8", ":eight:")
                    .replaceAll("9", ":nine:")
            );
        }

        return stringBuilder.toString();
    }
}
