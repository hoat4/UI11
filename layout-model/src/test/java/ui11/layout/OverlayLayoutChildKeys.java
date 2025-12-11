package ui11.layout;

import ui11.Widget;
import ui11.graphics.fill.Color;
import ui11.layout.singlechild.Alignment;
import ui11.observable.MutableObservable;
import ui11.text.Text;
import ui11.window.Desktop;

import static ui11.decoration.Background.withBackground;
import static ui11.graphics.Empty.empty;
import static ui11.layout.multichild.LinearLayout.row;
import static ui11.graphics.effect.Overlay.overlay;
import static ui11.layout.singlechild.Align.align;

public class OverlayLayoutChildKeys {

    // a teszt sikeres, ha a bal és a jobb oldali widgetnek nem változik a tartalma, és a középső villog.
    // a középső tartalma változhat, de nem tudom hogy specifikált lesz-e, hogy változzon.
    public void main() throws InterruptedException {
        MutableObservable<Widget> o = MutableObservable.withInitial(empty());
        Desktop.getDesktop().openWindow(withBackground(Color.WHITE, new Slot(o)));
        while (true) {
            Thread.sleep(1000);
            System.out.println();
            System.out.println("BE");
            System.out.println();
            o.set(overlay(
                    new ElementIdentityPrintingWidget(Alignment.LEFT_CENTER),
                    new ElementIdentityPrintingWidget(Alignment.CENTER),
                    new ElementIdentityPrintingWidget(Alignment.RIGHT_CENTER)
            ));
            Thread.sleep(1000);
            System.out.println();
            System.out.println("KI");
            System.out.println();
            o.set(overlay(
                    new ElementIdentityPrintingWidget(Alignment.LEFT_CENTER),
                    null,
                    new ElementIdentityPrintingWidget(Alignment.RIGHT_CENTER)
            ));
        }
    }

    private static final class ElementIdentityPrintingWidget extends Widget {

        private static int id;

        // ezt ne változtatgassuk, mert úgy már nem az element identity-t írja ki
        private final Alignment alignment;

        private ElementIdentityPrintingWidget(Alignment alignment) {
            this.alignment = alignment;
        }

        @Override
        protected Widget build() {
            return align(alignment, new Text(++id));
        }
    }
}
