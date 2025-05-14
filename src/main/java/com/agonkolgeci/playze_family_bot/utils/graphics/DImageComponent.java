package com.agonkolgeci.playze_family_bot.utils.graphics;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public abstract class DImageComponent {

    @Getter protected final DrawableImage drawableImage;
    @Getter protected final Graphics2D graphics2D;
    public DImageComponent(@NotNull DrawableImage drawableImage) {
        this.drawableImage = drawableImage;
        this.graphics2D = drawableImage.getGraphics2D();
    }

}
