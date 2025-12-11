package ui11;

import ui11.layout.singlechild.Align;
import ui11.text.Text;
import ui11.window.Desktop;

public class UITest4_2 {
    public static void main(String[] args) {
        Desktop.getDesktop().openWindow(Align.center(new Text("Hello world!")));
    }
}
