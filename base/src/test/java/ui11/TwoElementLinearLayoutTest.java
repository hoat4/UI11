package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.row;

public class TwoElementLinearLayoutTest {

    public static void main(String[] args) {
        Window.open(row(new ColorFill(Color.RED), new ColorFill(Color.GREEN)));
    }
}
