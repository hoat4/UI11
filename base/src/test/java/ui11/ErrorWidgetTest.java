package ui11;

import ui11.window.Window;

public class ErrorWidgetTest extends Widget {

    static void main() {
        Window.open(new ErrorWidgetTest());
    }

    @Override
    protected Widget build() {
        throw new RuntimeException("teszt");
    }
}
