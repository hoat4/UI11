package ui11.layout;

import ui11.Widget;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.row;
import static ui11.layout.multichild.LinearLayout.withWeight;

// ez egyszer failolt "Missed to refresh"-sel, de nem tudtam reprodukálni
public class LLChangeWeightTest {

    void main() throws InterruptedException {
        MutableObservable<Double> w1 = MutableObservable.withInitial(1.0);
        MutableObservable<Double> w2 = MutableObservable.withInitial(1.0);
        Window.open(row(
                new WidgetWithChangeableWeight(w1, new ColorFill(Color.RED)),
                new WidgetWithChangeableWeight(w2, new ColorFill(Color.GREEN))
        ));
        Thread.sleep(5000);
        w2.set(2.0);
        System.out.println("most meg kellett nőnie a zöldnek és kicsinyülnie a pirosnak");
    }

    static class WidgetWithChangeableWeight extends Widget {

        final Observable<Double> weight;
        final Widget content;

        public WidgetWithChangeableWeight(Observable<Double> weight, Widget content) {
            this.weight = weight;
            this.content = content;
        }

        @Override
        protected Widget build() {
            return withWeight(weight.get(), content);
        }
    }
}
