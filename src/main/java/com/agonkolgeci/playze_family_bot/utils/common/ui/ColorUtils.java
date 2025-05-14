package com.agonkolgeci.playze_family_bot.utils.common.ui;

import com.agonkolgeci.playze_family_bot.utils.common.ObjectUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.regex.Pattern;

public class ColorUtils {

    public static final Color PRIMARY_COLOR = Color.decode("#EBB700");

    public static final Color DISCORD_PING_COLOR = Color.decode("#ed4245");
    public static final Color DISCORD_INVISIBLE_COLOR = Color.decode("#2f3136");

    @NotNull
    public static Color getRandomColor() {
        return new Color(ObjectUtils.SPLITTABLE_RANDOM.nextInt(255), ObjectUtils.SPLITTABLE_RANDOM.nextInt(255), ObjectUtils.SPLITTABLE_RANDOM.nextInt(255));
    }

    @NotNull
    public static Color getColor(@NotNull Color color, double alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255*alpha));
    }

    @NotNull
    public static Color getThemeColor(@NotNull Color origin) {
        double luminance = getLuminance(origin);

        if(luminance > 128) {
            return Color.BLACK;
        }

        return Color.WHITE;
    }

    @NotNull
    public static String toHEXColor(@NotNull Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static boolean isHEXColor(@NotNull String value) {
        return Pattern.compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$").matcher(value).matches();
    }

    public static double getLuminance(@NotNull Color color) {
        return 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
    }

    @NotNull
    public static Color getAverageColor(@NotNull BufferedImage bufferedImage) {
        final int width = bufferedImage.getWidth();
        final int height = bufferedImage.getHeight();

        long sumRed = 0, sumGreen = 0, sumBlue = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final Color pixel = new Color(bufferedImage.getRGB(x, y));

                sumRed += pixel.getRed();
                sumGreen += pixel.getGreen();
                sumBlue += pixel.getBlue();
            }
        }

        final int num = width * height;
        return new Color((int)(sumRed / num), (int)(sumGreen / num), (int)(sumBlue / num));
    }

}
