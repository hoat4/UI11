package ui11;

import ui11.observable.Observable;
import ui11.provide.Provide;
import ui11.text.Text;
import ui11.window.Desktop;

public class ProvideAnnotationTest {

    public static void main(String[] args) {
        // a teszt sikeres, ha a megjelenő ablakban az látszik, hogy "value: 5894"
        Desktop.getDesktop().openWindow(new Outer());
    }

    private static class Outer extends Widget {

        @Override
        protected Widget build() {
            return new Inner();
        }

        @Provide
        Integer i() {
            return 5894;
        }
    }

    private static class Inner extends Widget {

        @Inject private Observable<Integer> i;

        @Override
        protected Widget build() {
            return new Text("value: " + i);
        }
    }
}
