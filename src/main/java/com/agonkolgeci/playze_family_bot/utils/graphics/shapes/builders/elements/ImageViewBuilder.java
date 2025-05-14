package com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.elements;

import com.agonkolgeci.playze_family_bot.utils.graphics.DrawableImage;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.RectangularBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ImageViewBuilder extends RectangularBuilder<ImageViewBuilder, RoundRectangle2D> {

    @Getter private final Image image;
    public ImageViewBuilder(@NotNull DrawableImage drawableImage, @NotNull Image image, double x, double y, double width, double height, double arcWidth, double arcHeight) {
        super(drawableImage, x, y, width, height, arcWidth, arcHeight);

        this.image = image.getScaledInstance((int) width, (int) height, Image.SCALE_SMOOTH);
    }

    public ImageViewBuilder(@NotNull DrawableImage drawableImage, @NotNull Image image, double x, double y, double width, double height) {
        this(drawableImage, image, x, y, width, height, 0, 0);
    }

    @Override
    public @NotNull RoundRectangle2D paint() {
        super.prepare();
        super.stroke();

        graphics2D.setClip(shape);
        graphics2D.drawImage(image, (int) getX(), (int) getY(), (int) getWidth(), (int) getHeight(), null);

        return super.paint();
    }
}
