package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

public class SimpleColorWindowTest {

    public static void main(String[] args) {
        Window.open(new ColorFill(Color.YELLOW));
    }
}
