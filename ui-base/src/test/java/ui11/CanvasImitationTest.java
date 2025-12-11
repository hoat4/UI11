/*
package ui11;

import ui11.geom.Path.PathBuilder;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.geom.StrokedShape;
import ui11.graphics.fill.Color;
import ui11.layout.helper.MultiChildLayout;
import ui11.layout.protocol.Sizing;
import ui11.window.Desktop;

public class CanvasImitationTest extends MultiChildLayout {
    @Override
    public Sizing sizingImpl() {
        return Sizing.ofPreferred(new Size(300, 200));
    }

    @Override
    public void layout(Size size) {
        place(Color.YELLOW, new Rect(100, 100, 100, 50));

        PathBuilder b = new PathBuilder();
        b.moveTo(200, 100);
        b.lineTo(250, 125);
        b.lineTo(200, 150);
        b.close();

        place(Color.RED, new StrokedShape(b.build()));
    }

    public static void main(String[] args) {
        Desktop.getDesktop().openWindow(new CanvasImitationTest());
    }
}
*/