package ui11;

import ui11.color.Color;
import ui11.layout.singlechild.Align;
import ui11.text.Text;
import ui11.text.TextModifiers;
import ui11.window.Window;

public class TextDecorationTest {
    public static void main(String[] args) {
        Window.open(Align.center(
                TextModifiers.withTextColor(Color.RED, new Text("piros"))
        ));
    }
}
