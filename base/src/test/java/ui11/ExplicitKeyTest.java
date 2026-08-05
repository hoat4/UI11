package ui11;

import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.column;

public class ExplicitKeyTest {

    private static class W extends Widget {

        private final Observable<Boolean> reverse;

        @Remember private Slot2 fa, fb;

        W(Observable<Boolean> reverse) {
            this.reverse = reverse;
        }

        @Override
        protected void initState() {
            fa = new Slot2();
            fb = new Slot2();
        }

        @Override
        protected Widget build() {
            System.out.println("reverse.get() = " + reverse.get());
            if (reverse.get())
                return column(fb(), fa());
            else
                return column(fa(), fb());
        }

        private Widget fa() {
            return fa.with(new ElementIdentityPrintingWidget());
        }

        private Widget fb() {
            return fb.with(new ElementIdentityPrintingWidget());
        }
    }

    private static final class ElementIdentityPrintingWidget extends Widget {

        private static int id;

        @Override
        protected Widget build() {
            return new Text(++id);
        }
    }

    public static void main() throws InterruptedException {
        // 1 és alatta 2 jelenik meg, majd 5 másodperc múlva 2 és alatta 1-nek kell megjelennie

        MutableObservable<Boolean> reverse = MutableObservable.withInitial(false);
        Window.open(new W(reverse));
        Thread.sleep(5000);
        reverse.set(!reverse.get());
    }
}
