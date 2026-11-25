package ui11.decoration;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.layout.singlechild.Align;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.geom.Length.px;

public class CenteredBorderTest {
    static void main() {
        Window.open(Align.center(
                new Border(px(10), Color.RED, new Text("Hello world!"))
        ));
    }
}
