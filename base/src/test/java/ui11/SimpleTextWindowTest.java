package ui11;

import ui11.text.Text;
import ui11.window.Window;

public class SimpleTextWindowTest {
    public static void main(String[] args) {
        // szándékosan nincs háttér se
        Window.open(new Text("Hello world!"));
    }
}
