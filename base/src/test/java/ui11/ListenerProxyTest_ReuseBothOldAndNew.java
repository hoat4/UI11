package ui11;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.input.gesture.ClickListener;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.singlechild.Align;
import ui11.observable.MutableObservable;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.graphics.Empty.empty;
import static ui11.graphics.effect.Overlay.overlay;

// A: 1	    A=1
// A: 2	    A=1, 1.replacement=2, 2.replacement=1
// B: 1     A=1, B=2
// C: 2     A=1, B=2, C=1

// A teszt sikeres, ha a 3 színes paca megjelenése során nem változik egyiknek se a színe utólag,
// a feliratok "1", "1", "2", és ha rájuk kattintunk, akkor a felső és alsó 2-t ír a konzolra, a középső pedig 1-et.

public class ListenerProxyTest_ReuseBothOldAndNew {

    private static String result;

    static void main() throws InterruptedException {
        W w1 = new W(runnable("1"));
        W w2 = new W(runnable("2"));

        MutableObservable<Widget> o = MutableObservable.withInitial(
                LinearLayout.column(w1, empty(), empty())
        );
        System.out.println("A: 1");
        Window.open(new SlotOld2(o));
        Thread.sleep(3000);

        System.out.println("A: 2");
        o.set(LinearLayout.column(w2, empty(), empty()));
        Thread.sleep(3000);

        System.out.println("B: 1");
        o.set(LinearLayout.column(w2, w1, empty()));
        Thread.sleep(3000);

        System.out.println("C: 2");
        o.set(LinearLayout.column(w2, w1, w2));
    }

    private static Runnable runnable(String s) {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println(result = s);
            }

            @Override
            public String toString() {
                return s;
            }
        };
    }

    static class W extends Widget {

        private final Runnable r;

        public W(Runnable r) {
            this.r = listenerProxy(r);
        }

        @SuppressWarnings("Convert2MethodRef")
        @Override
        protected Widget build() {
            r.run();
            return new ClickListener(
                    overlay(
                            new ColorFill(Color.sRGB(.5 + Math.random() / 2, .5 + Math.random() / 2, .5 + Math.random() / 2)),
                            Align.center(
                                    new Text(result)
                            )
                    ),
                    () -> r.run()
            );
        }
    }
}
