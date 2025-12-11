package ui11.platform.awt.j2d;

import ui11.geom.Vec2;
import ui11.graphics.fill.Color;

import java.awt.*;

public record J2DColorPrimitive(java.awt.Color awtColor) implements J2DPrimitive {

    public J2DColorPrimitive(Color color) {
        this(J2DUtil.color(color));
    }

    @Override
    public void draw(Graphics2D g, Rectangle bounds) {
        g.setColor(awtColor);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    @Override
    public PickResult findInputRegion(Vec2 p) {
        return null;
    }
}
