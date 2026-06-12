package ui11;

import ui11.geom.Path;
import ui11.geom.Rect;
import ui11.graphics.shaper.PathShaped;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.graphics.effect.Overlay.overlay;

public class SimpleClipPathTest {
    public void main() {
        Window.open(overlay(
                new ColorFill(Color.RED),
                new PathShaped(
                        new ColorFill(Color.YELLOW),
                        Path.ofRect(new Rect(100, 100, 50, 50))
                )
        ));
    }
}
