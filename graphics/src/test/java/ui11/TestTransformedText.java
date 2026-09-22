package ui11;

import ui11.geom.Mat4;
import ui11.geom.Vec2;
import ui11.graphics.effect.Transform;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.graphics.effect.Overlay.overlay;

public class TestTransformedText {
    static void main() {
        Window.open(overlay(
                new Text("Non-translated text"),
                new Transform(new Text("Translated text"), Mat4.ofTranslation(new Vec2(100, 100)))
        ));
    }
}
