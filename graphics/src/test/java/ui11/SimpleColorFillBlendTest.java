package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.graphics.effect.Overlay.overlay;

public class SimpleColorFillBlendTest {
    public void main() {
        Window.open(overlay(
                new ColorFill(Color.BLUE),
                new ColorFill(Color.GREEN.withAlpha(0.5))
        ));
    }
}
