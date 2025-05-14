package com.agonkolgeci.playze_family_bot.utils.common.fonts;

import com.agonkolgeci.playze_family_bot.PlayzeFamilyBot;
import com.agonkolgeci.playze_family_bot.utils.common.ResourceUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.nio.file.Path;

public class FontUtils {

    @NotNull private static final String ROOT_PATH = "fonts";

    @NotNull public static final Font ZABAL_REGULAR = retrieveLocalFont("zabal_regular.otf");
    @NotNull public static final Font ZABAL_EXTRA_BOLD = retrieveLocalFont("zabal_extra_bold.otf");

    @NotNull
    public static Font retrieveLocalFont(@NotNull String... objects) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, ResourceUtils.retrieveLocalInputStream(ROOT_PATH, objects));
        } catch (Exception exception) {
            PlayzeFamilyBot.getLogger().warn("Unable to create font: " + Path.of(ROOT_PATH, objects).toString());
        }

        return Font.getFont("Arial");
    }

}
