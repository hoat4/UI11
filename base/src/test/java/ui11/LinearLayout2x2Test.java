package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.column;
import static ui11.layout.multichild.LinearLayout.row;

public class LinearLayout2x2Test {

    public static void main(String[] args) {
        Window.open(column(
                row(new RedSquare(), new GreenSquare()),
                row(new BlueSquare(), new LightBlueSquare())
        ));
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
