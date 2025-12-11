package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.Button;
import ui11.layout.singlechild.Align;
import ui11.text.Text;
import ui11.window.Desktop;

import java.time.LocalDateTime;

public class ButtonTest {

    public static void main(String[] args) {
        Desktop.getDesktop().openWindow(Align.center(new Widget() {
            @Override
            protected Widget build() {
                return new Text("asdf");
            }
        }));
        Desktop.getDesktop().openWindow(Align.center(new Button(new Text("ASDF"), () -> {
            System.out.println("Megnyomva "+ LocalDateTime.now());
        })));
    }
}
