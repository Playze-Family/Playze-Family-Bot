package com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.elements;

import com.agonkolgeci.playze_family_bot.utils.graphics.DrawableImage;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.Text;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.RectangularBuilder;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.positions.HPos;
import com.agonkolgeci.playze_family_bot.utils.graphics.shapes.builders.positions.VPos;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@SuppressWarnings("all")
public class TextBuilder extends RectangularBuilder<TextBuilder, Text> {

    public TextBuilder(@NotNull DrawableImage drawableImage, @NotNull String text, @NotNull Font font, @NotNull HPos hPos, @NotNull VPos vPos, double x, double y) {
        super(drawableImage, new Text(drawableImage.getGraphics2D(), text, font, hPos, vPos, x, y));
    }

    @NotNull
    public TextBuilder attachImage(@NotNull Image image, @NotNull HPos hPos, int width, int height, int padding) {
        switch (hPos) {
            case LEFT -> {
                this.translate(width+padding, 0);
                new ImageViewBuilder(drawableImage, image, getX()-width-padding, getY(), width, height).paint();
            }

            case RIGHT -> {
                this.translate(-width-padding, 0);
                new ImageViewBuilder(drawableImage, image, shape.getMaxX()+padding-width, getY(), width, height).paint();
            }
        }

        return this;
    }

    @NotNull
    public TextBuilder attachText(@NotNull String text, @NotNull Font font, @NotNull VPos vPos, @NotNull Color color, int padding) {
        switch (vPos) {
            case TOP -> new TextBuilder(drawableImage, text, font, shape.getHPos(), VPos.BOTTOM, (shape.getHPos() == HPos.RIGHT ? shape.getMaxX() : getX()), shape.getMinY()-padding).color(color).paint();
            case CENTER -> new TextBuilder(drawableImage, text, font, shape.getHPos(), VPos.BOTTOM, (shape.getHPos() == HPos.RIGHT ? getX()-padding : shape.getMaxX()+padding), shape.getMaxY()).color(color).paint();
            case BOTTOM -> new TextBuilder(drawableImage, text, font, shape.getHPos(), VPos.TOP, (shape.getHPos() == HPos.RIGHT ? shape.getMaxX() : getX()), shape.getMaxY()+padding).color(color).paint();
        }

        return this;
    }

    @Override
    public @NotNull Text paint() {
        super.prepare();

        graphics2D.setFont(shape.getFont());
        graphics2D.setColor(color);
        graphics2D.drawString(shape.getText(), (int) getX(), (int) (getY()+getHeight()));

        return super.paint();
    }

}
