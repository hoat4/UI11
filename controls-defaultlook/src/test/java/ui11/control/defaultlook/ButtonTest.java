package ui11.control.defaultlook;

import ui11.control.Button;
import ui11.layout.singlechild.Align;
import ui11.text.Text;
import ui11.window.Window;

import java.time.LocalDateTime;

public class ButtonTest {

    public static void main(String[] args) {
        /*
        Desktop.getDesktop().openWindow(Align.center(new Widget() {
            @Override
            protected Widget build() {
                return new Text("asdf");
            }
        }));
         */
        // TODO ez most nem működik, mert PointerStateDependent MouseRegionja elfedi ClickListenert
        Window.open(Align.center(new Button(new Text("ASDF"), () -> {
            System.out.println("Megnyomva "+ LocalDateTime.now());
        })));
    }
}
