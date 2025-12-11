package ui11;

import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Desktop;

public class SimpleColorWindowTest {

    public static void main(String[] args) {
        Desktop.getDesktop().openWindow(new ColorFill(Color.YELLOW));
    }
}
