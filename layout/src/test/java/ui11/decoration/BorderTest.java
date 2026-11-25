package ui11.decoration;

import ui11.color.Color;
import ui11.geom.Length;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.geom.Length.px;

public class BorderTest {
    static void main() {
        Window.open(new Border(px(50), Color.RED, new ColorFill(Color.GREEN)));
    }
}
