package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.observable.MutableObservable;
import ui11.text.Text;
import ui11.window.Window;

public class ReplaceRootWidgetTest {

    static void main() throws InterruptedException {
        MutableObservable<Boolean> green = MutableObservable.withInitial(false);
        Window.open(new Widget() {

            @Override
            protected Widget build() {
                // ha mindkét esetben ColorFill, az nem jó, mert a root node nem változik,
                // megmarad a FillPathNode
                return green.get() ? new ColorFill(Color.GREEN) :
                        new Text("This text should disappear after 5 seconds.");
            }
        });
        Thread.sleep(5000);
        green.set(true);
    }
}
