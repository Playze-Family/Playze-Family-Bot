package com.agonkolgeci.playze_family_bot.utils.common.ui;

import com.agonkolgeci.playze_family_bot.utils.common.ObjectUtils;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

public class MessageUtils {

    public static final String BOT_INSUFFICIENT_PERMISSIONS = "Je ne dispose pas des permissions suffisantes pour faire cela. Veuillez contacter l'administration.";

    public static final String MEMBER_DOES_NOT_HAVE_PERMISSIONS = "Vous ne diposez pas des permissions suffisantes pour faire cela.";
    public static final String MEMBER_MUST_BE_HUMAN = "Vous devez spécifier un membre non robotique, en effet je préfère les vrais humains quant à mes concurrents.";
    public static final String MEMBER_ARE_NOT_AUTHOR = "Vous n'êtes pas %s ! Vous ne pouvez donc pas intéragir avec ce message, je vous invite à exécuter la commande vous-même.";

    @NotNull
    public static String format(@NotNull Emoji emoji, @NotNull String message, @NotNull String... details) {
        return new StringJoiner("\n")
                .add(emoji.getFormatted() + " " + MarkdownUtil.bold(message)).add("")
                .add(String.join("\n", details))
                .toString()
                .replaceAll("\\[(?!\\]\\().*?\\](?!\\()", "$0("+ ObjectUtils.EXTERNAL_WEBSITE_URL +")");
    }

    @NotNull
    public static String success(@NotNull String message, @NotNull String... details) {
        return format(Emoji.fromUnicode("✅"), message, details);
    }

    @NotNull
    public static String info(@NotNull String message, @NotNull String... details) {
        return format(Emoji.fromUnicode("ℹ️"), message, details);
    }

    @NotNull
    public static String error(@NotNull String message, @NotNull String... details) {
        return format(Emoji.fromUnicode("\uD83D\uDCDB"), message, details);
    }

    @NotNull
    public static String error(@NotNull Exception exception) {
        return error(exception.getMessage());
    }

}
