package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.graphics.effect.Overlay.overlay;

public class NestedOverlaysTest {
    public static void main(String[] args) {
        Window.open(overlay(overlay(new ColorFill(Color.GREEN))));
    }
}
