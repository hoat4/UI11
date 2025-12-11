package ui11.control;

import ui11.Widget;
import ui11.layout.singlechild.Align;
import ui11.observable.MutableObservable;
import ui11.text.Text;
import ui11.window.Desktop;

import static ui11.layout.multichild.LinearLayout.column;

public class ButtonCacheTest extends Widget {

    private final MutableObservable<Integer> i;

    private ButtonCacheTest(MutableObservable<Integer> i) {
        this.i = i;
    }

    @Override
    protected Widget build() {
        String s = "i=" + i.get();
        return column(
                Align.center(new Button("A", () -> {
                    System.out.println("Pressed");
                })),
                Align.center(new Text(s))
        );
    }

    public static void main(String[] args) throws InterruptedException {
        ButtonCacheTest t = new ButtonCacheTest(MutableObservable.withInitial(1));
        Desktop.getDesktop().openWindow(t);
        while (true) {
            t.i.set(t.i.get() + 1);
            Thread.sleep(1000);
        }
    }
}
