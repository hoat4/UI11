package ui11;

import ui11.layout.singlechild.Align;
import ui11.text.Text;
import ui11.window.Window;

public class AlignedTextTest {
    public static void main(String[] args) {
        // megnézi, hogy Text peer implementálja-e BoxLayoutProtocolt
        Window.open(Align.center(new Text("középre")));
    }
}
