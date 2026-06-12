package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.window.Window;

public class TestChangeColor {

    void main() throws InterruptedException {
        MutableObservable<Color> colorProp = MutableObservable.withInitial(Color.RED);
        Window.open(new W(colorProp));
        Thread.sleep(5000);
        colorProp.set(Color.GREEN);
    }

    private static class W extends Widget{

        private final Observable<Color> colorProp;

        public W(Observable<Color> colorProp) {
            this.colorProp = colorProp;
        }

        @Override
        protected Widget build() {
            System.out.println(colorProp.get());
            return new ColorFill(colorProp.get());
        }
    }
}
