package ui11.layout;

import ui11.Widget;
import ui11.color.Color;
import ui11.observable.MutableObservable;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.decoration.Background.withBackground;
import static ui11.graphics.Empty.empty;
import static ui11.layout.multichild.LinearLayout.row;

public class LinearLayoutChildKeys {

    // a teszt sikeres, ha a bal és a jobb oldali widgetnek nem változik a tartalma.
    // a középsőváltozhat, de nem tudom hogy specifikált lesz-e, hogy változzon.
    public void main() throws InterruptedException {
        MutableObservable<Widget> o = MutableObservable.withInitial(empty());
        Window.open(withBackground(Color.WHITE, new Slot(o)));
        while (true) {
            Thread.sleep(1000);
            o.set(row(
                    new ElementIdentityPrintingWidget(),
                    new ElementIdentityPrintingWidget(),
                    new ElementIdentityPrintingWidget()
            ));
            Thread.sleep(1000);
            o.set(row(
                    new ElementIdentityPrintingWidget(),
                    null,
                    new ElementIdentityPrintingWidget()
            ));
        }
    }

    private static class ElementIdentityPrintingWidget extends Widget {

        private static int id;

        @Override
        protected Widget build() {
            return new Text(++id);
        }
    }
}
