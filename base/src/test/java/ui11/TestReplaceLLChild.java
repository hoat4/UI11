package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.column;

public class TestReplaceLLChild {
    static void main() throws InterruptedException {
        // ennek a tesztnek a futtatása előtt rakjunk be egy debug üzenetet
        // DefaultLinearLayoutImpl.build elejére és nézzük, hogy 5 mp után kiíródik-e.
        // A teszt akkor sikeres, ha a debug üzenet nem íródik ki az 5 mp után (csak induláskor), de mégis
        // zöldre vált pirosról a tartalom.
        SlotOld s = new SlotOld(column(new ColorFill(Color.RED)));
        Window.open(s);
        Thread.sleep(5000);
        s.set(column(new ColorFill(Color.GREEN)));
    }
}
