package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.column;
import static ui11.layout.multichild.LinearLayout.row;

// tests WidgetState.dispose
public class LinearLayout2x2_ReplaceChildTest extends Widget {

    private final MutableObservable<Boolean> showLightBlue = MutableObservable.withInitial(true);

    public static void main(String[] args) throws InterruptedException {
        LinearLayout2x2_ReplaceChildTest t = new LinearLayout2x2_ReplaceChildTest();
        Window.open(t);
        Thread.sleep(5000);
        t.showLightBlue.set(false);
    }

    @Override
    protected Widget build() {
        return column(
                row(new RedSquare(), new GreenSquare()),
                row(new BlueSquare(), showLightBlue.get() ? new LightBlueSquare() : new Text("Hello world!"))
        );
    }

    // ezek az osztályok azért kellenek, hogy TRACE_REFRESH-ben látszódjön hogy melyik négyzetről van szó
    private static class RedSquare extends Widget {

        @Override
        protected Widget build() {
            return new ColorFill(Color.RED);
        }
    }
    private static class GreenSquare extends Widget {

        @Override
        protected Widget build() {
            return new ColorFill(Color.GREEN);
        }
    }
    private static class BlueSquare extends Widget {

        @Override
        protected Widget build() {
            return new ColorFill(Color.BLUE);
        }
    }
    private static class LightBlueSquare extends Widget {

        @Override
        protected Widget build() {
            return new ColorFill(Color.LIGHTBLUE);
        }
    }
}
