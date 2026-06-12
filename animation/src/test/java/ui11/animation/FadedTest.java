package ui11.animation;

import ui11.Widget;
import ui11.color.Color;
import ui11.graphics.effect.Overlay;
import ui11.graphics.fill.ColorFill;
import ui11.observable.MutableObservable;
import ui11.window.Window;

public class FadedTest {
    static void main() throws InterruptedException {
        MutableObservable<Boolean> b = MutableObservable.withInitial(false);
        Window.open(new Widget() {
            @Override
            protected Widget build() {
                return Overlay.overlay(
                        new ColorFill(Color.WHITE),
                        new Faded(b.get(), new ColorFill(Color.BLUE))
                );
            }
        });
        Thread.sleep(5000);
        b.set(true);
    }
}
