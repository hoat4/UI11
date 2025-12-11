package ui11;

import ui11.layout.singlechild.Align;
import ui11.text.Text;
import ui11.window.Desktop;

public class AlignedTextTest {
    public static void main(String[] args) {
        // megnézi, hogy Text peer implementálja-e BoxLayoutProtocolt
        Desktop.getDesktop().openWindow(Align.center(new Text("középre")));
    }
}
