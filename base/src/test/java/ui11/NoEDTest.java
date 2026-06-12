package ui11;

import ui11.layout.singlechild.Align;
import ui11.text.Text;
import ui11.window.Window;

public class NoEDTest {
    public static void main(String[] args) {
        Window.open(Align.center(new Text("Hello world!")));
    }
}
