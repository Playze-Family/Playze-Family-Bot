package com.agonkolgeci.gebo.utils.graphics.shapes.builders.elements;

import com.agonkolgeci.gebo.utils.graphics.DrawableImage;
import com.agonkolgeci.gebo.utils.graphics.shapes.builders.RectangularBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.RoundRectangle2D;

@SuppressWarnings("all")
public class RectangleBuilder extends RectangularBuilder<RectangleBuilder, RoundRectangle2D> {

    public RectangleBuilder(@NotNull DrawableImage drawableImage, double x, double y, double width, double height, double arcWidth, double arcHeight) {
        super(drawableImage, x, y, width, height, arcWidth, arcHeight);
    }

    public RectangleBuilder(@NotNull DrawableImage drawableImage, double x, double y, double width, double height) {
        this(drawableImage, x, y, width, height, 0, 0);
    }

    @Override
    public @NotNull RoundRectangle2D paint() {
        super.prepare();
        super.fill();

        return super.paint();
    }
}
