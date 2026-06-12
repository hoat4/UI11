/*
package ui11;

import ui11.geom.AffineTransformation;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.color.Color;
import ui11.text.Text;
import ui11.layout.singlechild.Align;
import ui11.layout.multichild.Grid;
import ui11.layout.helper.MultiChildLayout;
import ui11.layout.protocol.Sizing;
import ui11.window.Desktop;

public class BoxTest extends MultiChildLayout {

    public static void main(String[] args) {
        Desktop.getDesktop().openWindow(new BoxTest());
    }

    private static final double SIDE_SIZE = 500;

    @Override
    public Sizing sizingImpl() {
        return Sizing.ofPreferred(new Size(SIDE_SIZE * 2, SIDE_SIZE * 2));
    }

    @Override
    public void layout(Size size) {
        double xOffset = SIDE_SIZE * 0.35;
        double yOffset = SIDE_SIZE * 0.4;
        place(side(Color.RED),
                new AffineTransformation(
                        1, 0, xOffset,
                        0, 1, yOffset + SIDE_SIZE * 0.3
                ),
                new Rect(0, 0, SIDE_SIZE, SIDE_SIZE));
        place(side(Color.GREEN),
                new AffineTransformation(
                        0.3, 0, xOffset + SIDE_SIZE,
                        -0.3, 1, yOffset + SIDE_SIZE * 0.3
                ),
                new Rect(0, 0, SIDE_SIZE, SIDE_SIZE));
        place(side(Color.LIGHTBLUE),
                new AffineTransformation(
                        1, -0.3, xOffset + SIDE_SIZE * 0.3,
                        0, 0.3, yOffset
                ),
                new Rect(0, 0, SIDE_SIZE, SIDE_SIZE));
    }

    private static Object side(Color color) {
        return new Grid(Align.center(new Text("ASDF"))).background(color);
    }
}
*/