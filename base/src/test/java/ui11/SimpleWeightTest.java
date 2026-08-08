package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.column;
import static ui11.layout.multichild.LinearLayout.withWeight;

public class SimpleWeightTest {
    static void main() {
        // doesn't test anything, but can be used to debug why parentdata doesn't flow into DefaultLinearLayoutImpl
        Window.open(column(withWeight(2, new ColorFill(Color.RED))));
    }
}
