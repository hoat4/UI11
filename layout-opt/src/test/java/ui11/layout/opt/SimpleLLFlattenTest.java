package ui11.layout.opt;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.column;

public class SimpleLLFlattenTest {
    static void main() {
        // csak debugoláshoz
        Window.open(column(column(new ColorFill(Color.YELLOW))));
    }
}
