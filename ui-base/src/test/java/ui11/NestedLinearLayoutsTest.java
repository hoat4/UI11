package ui11;

import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Desktop;

import static ui11.layout.multichild.LinearLayout.column;
import static ui11.layout.multichild.LinearLayout.row;

public class NestedLinearLayoutsTest {
    public static void main(String[] args) {
        Desktop.getDesktop().openWindow(column(
                row(new ColorFill(Color.RED), new ColorFill(Color.GREEN)),
                row(new ColorFill(Color.BLUE), new ColorFill(Color.LIGHTBLUE))
        ));
    }
}
