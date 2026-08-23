package ui11.layout.opt;

import ui11.Widget;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.column;

public class FlattenLLTest {
    static void main() {
        // To verify that the flattening is working, turn on render tree printing in AWTWindow.redraw
        Window.open(column(new RedRect(), column(new GreenRect(), new BlueRect())));
    }

    // azért külön osztályok, mert debuggerben így olvashatóbb, mint hogy csak annyi lenne kiírva hogy ColorFill
    static class RedRect extends Widget {

        @Override
        protected Widget build() {
            return new ColorFill(Color.RED);
        }
    }

    static class GreenRect extends Widget {

        @Override
        protected Widget build() {
            return new ColorFill(Color.GREEN);
        }
    }

    static class BlueRect extends Widget {

        @Override
        protected Widget build() {
            return new ColorFill(Color.BLUE);
        }
    }
}
