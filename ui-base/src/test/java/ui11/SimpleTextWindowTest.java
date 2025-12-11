package ui11;

import ui11.text.Text;
import ui11.window.Desktop;

public class SimpleTextWindowTest {
    public static void main(String[] args) {
        // szándékosan nincs háttér se
        Desktop.getDesktop().openWindow(new Text("Hello world!"));
    }
}
