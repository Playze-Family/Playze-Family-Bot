package fr.jielos.playzefamilybot.utils.graphics.shapes;

import fr.jielos.playzefamilybot.utils.graphics.shapes.builders.positions.HPos;
import fr.jielos.playzefamilybot.utils.graphics.shapes.builders.positions.VPos;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Text extends RoundRectangle2D.Double implements Shape {

    @Getter private final FontMetrics fontMetrics;

    @Getter private final String text;
    @Getter private final Font font;

    @Getter private final HPos hPos;
    @Getter private final VPos vPos;

    public Text(@NotNull Graphics2D graphics2D, @NotNull String text, @NotNull Font font, @NotNull HPos hPos, @NotNull VPos vPos, double x, double y) {
        super(x, y, 0, 0, 0, 0);

        this.fontMetrics = graphics2D.getFontMetrics(font);

        this.text = text;
        this.font = font;

        this.hPos = hPos;
        this.vPos = vPos;

        resize();
    }

    private void resize() {
        this.width = fontMetrics.stringWidth(text);
        this.height = fontMetrics.getAscent() - fontMetrics.getDescent();

        switch (hPos) {
            case CENTER -> this.x = x - width/2;
            case RIGHT -> this.x = x - width;
        }

        switch (vPos) {
            case CENTER -> this.y = y - height/2;
            case BOTTOM -> this.y = y - height;
        }
    }

    public float getFontSize() {
        return font.getSize2D();
    }

}
