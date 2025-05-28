package com.agonkolgeci.gebo.utils.common.ui;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmojiUtils {

    @NotNull public static Emoji YES = Emoji.fromUnicode("✅");
    @NotNull public static Emoji NO = Emoji.fromUnicode("❌");
    @NotNull public static Emoji GREEN_SQUARE = Emoji.fromUnicode("\uD83D\uDFE9");
    @NotNull public static Emoji RED_SQUARE = Emoji.fromUnicode("\uD83D\uDFE5");
    @NotNull public static Emoji INFORMATION = Emoji.fromUnicode("ℹ️");
    @NotNull public static Emoji SOUND = Emoji.fromUnicode("\uD83D\uDD0A");
    @NotNull public static Emoji SETTINGS = Emoji.fromUnicode("⚙️");
    @NotNull public static Emoji INBOX = Emoji.fromUnicode("\uD83D\uDCE5");
    @NotNull public static Emoji LINK = Emoji.fromUnicode("\uD83D\uDD17");
    @NotNull public static Emoji PAPERCLIP = Emoji.fromUnicode("\uD83D\uDCCE");
    @NotNull public static Emoji STATS = Emoji.fromUnicode("\uD83D\uDCCA");
    @NotNull public static Emoji RIBBON = Emoji.fromUnicode("\uD83C\uDF80");
    @NotNull public static Emoji TWISTED_RIGHTWARDS_ARROW = Emoji.fromUnicode("\uD83D\uDD00");
    @NotNull public static Emoji FAMILY = Emoji.fromUnicode("\uD83D\uDC68\u200D\uD83D\uDC66\u200D\uD83D\uDC66");
    @NotNull public static Emoji REJECT_MEMBER = Emoji.fromUnicode("\uD83D\uDE45\u200D♂️");
    @NotNull public static Emoji DETECTIVE = Emoji.fromUnicode("\uD83D\uDD75️\u200D♂️");

    @NotNull public static Emoji EXTERN_TWITCH = Emoji.fromFormatted("<:twitch:1118499643723092048>");
    @NotNull public static Emoji EXTERN_INVISIBLE = Emoji.fromFormatted("<:invisible:896435045244473385>");
    @NotNull public static Emoji EXTERN_WAVE_ANIMATED = Emoji.fromFormatted("<a:wave_animated:896435045244473385>");
    @NotNull public static Emoji EXTERN_TADA_ANIMATED = Emoji.fromFormatted("<a:tada_animated:896435045244473385>");

    @Nullable
    public static Emoji extractEmojiFromString(@NotNull String string) {
        @Nullable final String emoji = EmojiParser.extractEmojis(string).stream().findFirst().orElse(null);
        if(emoji == null) return null;

        return Emoji.fromUnicode(emoji);
    }

}
