package ui11;

import ui11.observable.MutableObservable;
import ui11.text.Text;
import ui11.window.Window;

// volt egy olyan bug, hogy első változást jól kezelte, de másodiknál feleslegesen updateelt.
// az okozta a problémát, hogy alsoLockedModelWidget nem volt beállítva.

// A teszt sikeres, ha a "kész" felirat alőtt végig "A/..." felirat jelenik meg és nem is változik meg a végén lévő
// szám.

public class ListenerProxyTest_TwoChanges {

    private static String result;

    static void main() throws InterruptedException {
        MutableObservable<Widget> o = MutableObservable.withInitial(new W(runnable("A")));
        Window.open(new SlotOld2(o));
        Thread.sleep(3000);
        o.set(new W(runnable("B")));
        Thread.sleep(3000);
        o.set(new W(runnable("C")));
        Thread.sleep(3000);
        o.set(new Text("kész"));
    }

    private static Runnable runnable(String s) {
        return new Runnable() {
            @Override
            public void run() {
                result = s;
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

        @Override
        protected Widget build() {
            result = null;
            r.run();
            return new Text(result + "/" + Math.random());
        }
    }
}
