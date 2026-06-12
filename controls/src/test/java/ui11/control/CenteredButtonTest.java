package ui11.control;

import ui11.layout.singlechild.Align;
import ui11.window.Window;

public class CenteredButtonTest {
    public static void main(String[] args) {
        Button btn = new Button("ABC", () -> {
        });
        Window.open(Align.center(btn));
    }
}
