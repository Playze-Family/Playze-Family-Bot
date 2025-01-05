package fr.jielos.playzefamilybot.utils.graphics.shapes.builders.elements;

import fr.jielos.playzefamilybot.utils.graphics.DrawableImage;
import fr.jielos.playzefamilybot.utils.graphics.shapes.builders.RectangularBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ProgressBarBuilder extends RectangularBuilder<ProgressBarBuilder, RoundRectangle2D> {

    public ProgressBarBuilder(@NotNull DrawableImage drawableImage, double x, double y, double width, double height, double arcWidth, double arcHeight) {
        super(drawableImage, x, y, width, height, arcWidth, arcHeight);
    }

    public ProgressBarBuilder(@NotNull DrawableImage drawableImage, double x, double y, double width, double height) {
        super(drawableImage, x, y, width, height);
    }

    public @NotNull ProgressBarBuilder complete(@NotNull Color completeColor, double value, double maxValue) {
        super.prepare();
        super.fill();

        new RectangleBuilder(drawableImage, getX(), getY(), (getWidth() * (value/maxValue)), getHeight(), getArcWidth(), getArcHeight()).clip(shape).color(completeColor).paint();

        return this;
    }

}
