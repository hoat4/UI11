package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.column;
import static ui11.layout.multichild.LinearLayout.row;

public class NestedLinearLayouts2Test {

    public static void main(String[] args) {
        // ugyanannak kell lennie a két LL mainaxis-ának, hogy előjöjjön a hiba
        Window.open(row(row(new Red(), new Green())));
    }

    private static class Red extends Widget {

        @Override
        protected Widget build() {
            return new ColorFill(Color.RED);
        }
    }

    private static class Green extends Widget {

        @Override
        protected Widget build() {
            return new ColorFill(Color.GREEN);
        }
    }
}
