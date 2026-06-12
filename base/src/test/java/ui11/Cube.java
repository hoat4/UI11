/*
package ui11;

import ui11.geom.AffineTransformation;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.graphics.fill.Color;
import ui11.layout.multichild.Grid;
import ui11.layout.helper.MultiChildLayout;
import ui11.layout.protocol.Sizing;

public class Cube extends MultiChildLayout {

    private static final double SIDE_SIZE = 500;
    public static final Rect SIDE_RECT = new Rect(0, 0, SIDE_SIZE, SIDE_SIZE);

    public final Object front, top, right;

    public Cube(Element front, Element top, Element right) {
        this.front = new Grid(front).background(Color.LIGHTBLUE);
        this.top = new Grid(top).background(Color.LIGHTBLUE);
        this.right = new Grid(right).background(Color.LIGHTBLUE);
    }

    @Override
    public Sizing sizingImpl() {
        return Sizing.ofPreferred(new Size(SIDE_SIZE * 2, SIDE_SIZE * 2));
    }

    @Override
    public void layout(Size size) {
        double xOffset = SIDE_SIZE * 0.35;
        double yOffset = SIDE_SIZE * 0.4;
        place(Color.WHITE, Rect.of(size));
        place(front, new AffineTransformation(
                1, 0, xOffset,
                0, 1, yOffset + SIDE_SIZE * 0.3
        ), SIDE_RECT);
        place(right,
                new AffineTransformation(
                        0.3, 0, xOffset + SIDE_SIZE,
                        -0.3, 1, yOffset + SIDE_SIZE * 0.3
                ),
                SIDE_RECT);
        place(top,
                new AffineTransformation(
                        1, -0.3, xOffset + SIDE_SIZE * 0.3,
                        0, 0.3, yOffset
                ),
                SIDE_RECT);
    }
}
*/