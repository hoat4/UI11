package ui11;

import ui11.graphics.fill.Color;
import ui11.layout.singlechild.Align;
import ui11.text.Text;
import ui11.text.TextModifiers;
import ui11.text.TextStyle;
import ui11.window.Desktop;

public class TextDecorationTest {
    public static void main(String[] args) {
        Desktop.getDesktop().openWindow(
                Align.center(
                        TextModifiers.withTextStyle(TextStyle.NULL.withColor(Color.RED), new Text("piros"))
                )
        );
    }
}
