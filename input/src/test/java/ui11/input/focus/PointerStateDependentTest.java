package ui11.input.focus;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.input.pointer.PointerStateDependent;
import ui11.layout.singlechild.Align;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.graphics.effect.Overlay.overlay;

public class PointerStateDependentTest {
    static void main() {
        // AWT esetén hover nincs implementálva, csak default és pressed
        // ráadásul kilóg a szöveg a háttérszínből
        Window.open(Align.center(new PointerStateDependent(
                overlay(new ColorFill(Color.RED), new Text("DEFAULT")),
                overlay(new ColorFill(Color.GREEN), new Text("HOVER")),
                overlay(new ColorFill(Color.BLUE), new Text("PRESSED"))
        )));
    }
}
