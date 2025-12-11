package ui11.control;

import ui11.decoration.Background;
import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;
import ui11.layout.singlechild.Align;
import ui11.layout.multichild.LinearLayout;
import ui11.window.Desktop;

import static ui11.graphics.effect.Overlay.overlay;

public class TextFieldTest {

    public static void main(String[] args) {
        EditablePlainText text = new EditablePlainText();
        TextField tf1 = new TextField(text, () -> text.set("ketchup"));
        TextField tf2 = new TextField(new EditablePlainText());
        Desktop.getDesktop().openWindow(
                LinearLayout.row(
                        overlay( // withBackground helyett azért overlay, hogy additionalUpValuest teszteljük
                                new ColorFill(Color.RED),
                                Align.center(tf1)
                        ),
                        Background.withBackground(
                                Color.GREEN,
                                Align.center(tf2)
                        )
                ));
    }
}
