package fr.jielos.playzefamilybot.utils.graphics.shapes.builders;

import fr.jielos.playzefamilybot.utils.graphics.DImageComponent;
import fr.jielos.playzefamilybot.utils.graphics.DrawableImage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

@SuppressWarnings("all")
public abstract class ShapeBuilder<B extends ShapeBuilder<B, S>, S extends Shape> extends DImageComponent {

    @Getter protected final S shape;

    @Getter protected Shape clip = null;

    @Getter protected Color color = null;

    @Getter protected Color strokeColor = null;
    @Getter protected Stroke stroke = null;

    public ShapeBuilder(@NotNull DrawableImage drawableImage, @NotNull S shape) {
        super(drawableImage);

        this.shape = shape;
    }

    @NotNull
    public abstract B translate(double translateX, double translateY);

    @NotNull
    public B clip(@NotNull Shape clip) {
        this.clip = clip;
        return (B) this;
    }

    @NotNull
    public B color(@NotNull Color color) {
        this.color = color;
        return (B) this;
    }

    @NotNull
    public B stroke(@NotNull Color color) {
        this.color = color;
        return (B) this;
    }

    @NotNull
    public B stroke(@NotNull Stroke stroke, @NotNull Color strokeColor) {
        this.stroke = stroke;
        this.strokeColor = strokeColor;
        return (B) this;
    }

    public double getX() {
        return shape.getBounds2D().getX();
    }

    public double getY() {
        return shape.getBounds2D().getY();
    }

    public double getWidth() {
        return shape.getBounds2D().getWidth();
    }

    public double getHeight() {
        return shape.getBounds2D().getHeight();
    }

    protected void prepare() {
        graphics2D.setClip(clip);
    }

    protected void fill() {
        if(color != null) {
            graphics2D.setColor(color);
            graphics2D.fill(shape);
        }
    }

    protected void stroke() {
        if(strokeColor != null) {
            graphics2D.setColor(strokeColor);
            graphics2D.setStroke(getStroke());

            draw();
        }
    }

    protected void draw() {
        prepare();

        graphics2D.draw(shape);
    }

    @NotNull
    public S paint() {
        stroke();

        return build();
    }

    @NotNull
    public S build() {
        graphics2D.setClip(null);
        graphics2D.setColor(null);
        graphics2D.setFont(null);

        return shape;
    }

    @NotNull
    public Stroke getStroke() {
        return Objects.requireNonNullElse(stroke, graphics2D.getStroke());
    }

}