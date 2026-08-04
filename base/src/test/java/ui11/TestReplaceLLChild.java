package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.column;

// TODO ez egyelőre failol, kéne csinálni normális child modellt SubstitutedWidgetbe hogy ne failoljon.
//      tehát az lenne a cél, hogy a peer le legyen rebuildelve, amikor a childek változnak, de a childek peerjei nem.

public class TestReplaceLLChild {
    static void main() throws InterruptedException {
        // ennek a tesztnek a futtatása előtt rakjunk be egy system.outot
        // DefaultLinearLayoutImpl.build elejére és nézzük, hogy 5 mp után kiíródik-e.
        // ha igen, akkor sikertelen a teszt.
        SlotOld s = new SlotOld(column(new ColorFill(Color.RED)));
        Window.open(s);
        Thread.sleep(5000);
        s.set(column(new ColorFill(Color.GREEN)));
    }
}
