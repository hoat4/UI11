package ui11;

import ui11.color.Color;
import ui11.input.gesture.ClickListener;
import ui11.layout.singlechild.Padding;
import ui11.observable.MutableObservable;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.decoration.Background.withBackground;
import static ui11.geom.Length.px;
import static ui11.graphics.Empty.empty;
import static ui11.layout.multichild.LinearLayout.column;

public class InterfaceProxyTest {

    public static void main(String[] args) throws InterruptedException {
        MutableObservable<Widget> slot = MutableObservable.withInitial(empty());
        Window.open(new SlotOld2(slot));
        int i = 1;
        while (true) {
            int j = i++;
            java.awt.EventQueue.invokeLater(() -> { // TODO
                int j2 = j;
                slot.set(column(
                        withBackground(Color.WHITE, new Text(j2)),
                        new W1(j / 5 * 5, () -> {
                            System.out.println("W1: " + j2);
                        }),
                        new W3(j / 5 * 5, () -> {
                            System.out.println("W3: " + j2);
                        })
                ));
            });
            Thread.sleep(1000);
        }
    }

    private static final class W1 extends Widget {

        private final int i;
        private final Runnable r;

        private W1(int i, Runnable r) {
            this.i = i;
            this.r = listenerProxy(r);
            System.out.println("create w1 "+i);
        }

        @Override
        @SuppressWarnings("ResultOfMethodCallIgnored")
        protected Widget build() {
            // ellenőrzi, hogy nem száll-e el StackOverflowErrorral (ld. ListenerProxyBase::toString)
            r.toString();

            System.out.println("build w1 "+i);

            return new ClickListener(
                    withBackground(Color.LIGHTCORAL,
                            Padding.allSides(px(20),
                                    new Text(i + " / " + Math.random())
                            )
                    ),
                    r
            );
        }
    }

    // TODO nem listener proxy-kkal kapcsolatos probléma, de meg kéne nézni: ha ClickListenert
    //      beljebb viszem mindkettő widgetben hogy csak Text-re vonatkozzon,
    //      akkor W1 nem kattintható, W3 viszont igen. miért?

    private static final class W3 extends Widget {

        private final int i;
        private final Runnable r;

        private W3(int i, Runnable r) {
            this.i = i;
            this.r = listenerProxy(r);
        }

        @Override
        protected Widget build() {
            return new W3Impl(this);
        }
    }

    private static class W3Impl extends Widget {

        private final W3 w3;

        public W3Impl(W3 w3) {
            System.out.println("w3 create");
            this.w3 = w3;
        }

        @Override
        protected Widget build() {
            System.out.println("w3 build");
            return new ClickListener(
                    withBackground(Color.LIGHTBLUE,
                            Padding.allSides(px(20),
                                    new Text(w3.i + " / " + Math.random())
                            )
                    ),
                    w3.r
            );
        }
    }
}
