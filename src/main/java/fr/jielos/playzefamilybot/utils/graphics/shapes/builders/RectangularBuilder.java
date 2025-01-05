package fr.jielos.playzefamilybot.utils.graphics.shapes.builders;

import fr.jielos.playzefamilybot.utils.graphics.DrawableImage;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.RoundRectangle2D;

@SuppressWarnings("all")
public abstract class RectangularBuilder<B extends ShapeBuilder<B, S>, S extends RoundRectangle2D> extends ShapeBuilder<B, S> {

    public RectangularBuilder(@NotNull DrawableImage drawableImage, S shape) {
        super(drawableImage, shape);
    }

    public RectangularBuilder(@NotNull DrawableImage drawableImage, double x, double y, double width, double height, double arcWidth, double arcHeight) {
        super(drawableImage, (S) new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight));
    }

    public RectangularBuilder(@NotNull DrawableImage drawableImage, double x, double y, double width, double height) {
        this(drawableImage, x, y, width, height, 0, 0);
    }

    public double getArcWidth() {
        return shape.getArcWidth();
    }

    public double getArcHeight() {
        return shape.getArcHeight();
    }

    @Override
    public @NotNull B translate(double translateX, double translateY) {
        resize(getX()+translateX, getY()+translateY);
        return (B) this;
    }

    @NotNull
    public B transform(double translateX, double translateY) {
        translate(translateX, translateY);
        resize(getX(), getY(), getWidth()-translateX*2, getHeight()-translateY*2);

        return (B) this;
    }

    @NotNull
    public B resize(double x, double y, double width, double height, double arcWidth, double arcHeight) {
        shape.setRoundRect(x, y, width, height, arcWidth, arcHeight);
        return (B) this;
    }

    @NotNull
    public B resize(double x, double y, double width, double height) {
        return resize(x, y, width, height, getArcWidth(), getArcHeight());
    }

    @NotNull
    public B resize(double x, double y) {
        return resize(x, y, getWidth(), getHeight(), getArcWidth(), getArcHeight());
    }

    @NotNull
    public B resize() {
        return resize(getX(), getY(), getWidth(), getHeight(), getArcWidth(), getArcHeight());
    }

}
