package com.agonkolgeci.playze_family_bot.utils.graphics;

import com.agonkolgeci.playze_family_bot.utils.common.ResourceUtils;
import com.agonkolgeci.playze_family_bot.utils.common.ObjectUtils;
import lombok.Getter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DrawableImage {

    @Getter @NotNull private final BufferedImage image;
    @Getter @NotNull private final Graphics2D graphics2D;

    public DrawableImage(@NotNull BufferedImage backgroundImage) {
        this.image = backgroundImage;
        this.graphics2D = image.createGraphics();

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public DrawableImage(int width, int height) {
        this(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));
    }

    @NotNull
    public BufferedImage drawBackground(@NotNull BufferedImage backgroundImage) {
        final int x = (image.getWidth() - backgroundImage.getWidth()) / 2;
        final int y = (image.getHeight() - backgroundImage.getHeight()) / 2;
        graphics2D.drawImage(backgroundImage, x, y, null);

        return image;
    }

    @NotNull
    public FileUpload toFileUpload(@NotNull String name, @NotNull String formatName, @NotNull Object... attributes) {
        graphics2D.dispose();

        return ResourceUtils.getImageAsFileUpload(image, ObjectUtils.joinObjects("-", name, attributes), formatName);
    }

}
