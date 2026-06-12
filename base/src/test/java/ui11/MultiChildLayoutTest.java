/*
package ui11;

import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.graphics.effect.Group.GroupBuilder.ChildPosition;
import ui11.text.Text;
import ui11.layout.helper.MultiChildLayout;
import ui11.layout.protocol.Sizing;
import ui11.window.Desktop;

public class MultiChildLayoutTest {

    public static void main(String[] args) {
        Element e = Element.of(new Text("a"));
        Desktop.getDesktop().openWindow(new MultiChildLayout() {
            @Override
            public Sizing sizingImpl() {
                return Sizing.ofPreferred(new Size(200, 200));
            }

            @Override
            public void layout(Size size) {
                sizingOf(e).preferredSize();
                place(e, ChildPosition.of(Rect.of(size)));
            }
        });
    }
}
*/