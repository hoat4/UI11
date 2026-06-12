package ui11;

import ui11.color.Color;
import ui11.decoration.Box;
import ui11.graphics.effect.Overlay;
import ui11.graphics.fill.ColorFill;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.singlechild.Align;
import ui11.observable.MutableObservable;
import ui11.window.Window;

import java.util.List;

import static ui11.geom.Length.px;

public class UITest4 extends Widget {

    private final MutableObservable<Integer> i = MutableObservable.withInitial(0);

    @Override
    protected Widget build() {
        Color color = i.get() == 0 ? Color.BLUE : Color.GREEN;
        if (false)
            return new Overlay(List.of(new ColorFill(color)));
        if (true) {
            return LinearLayout.column(
                    LinearLayout.row(new ColorFill(Color.RED), new ColorFill(color)),
                    LinearLayout.row(new ColorFill(color), new ColorFill(Color.YELLOW))
            );
        } else {
            return Align.center(
                    new Box(new ColorFill(color)).withFixedSize(px(30), px(30))
            );
        }
    }

    public static void main(String[] args) throws InterruptedException {
        UITest4 e = new UITest4();
        Window.open(e);

        int i = 0;
        while (true) {
            Thread.sleep(1000);
            e.i.set(1);
            Thread.sleep(1000);
            e.i.set(0);
            /*
            if (i++ % 5 == 4)
                Desktop.getDesktop().openWindow(e);
             */
        }
    }
}
