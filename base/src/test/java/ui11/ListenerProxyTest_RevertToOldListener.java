package ui11;

import ui11.observable.MutableObservable;
import ui11.text.Text;
import ui11.window.Window;

import java.util.Timer;
import java.util.TimerTask;

// A: 1	    A=1
// A: 2     A=1, 1.replacement=2, 2.replacement=1
// A: 1	    A=1

// A teszt sikeres, ha először 1-esek, majd "CHANGE TO 2" után kettesek, majd "CHANGE TO 1" után megint egyesek
// jelennek meg a kimeneten.

public class ListenerProxyTest_RevertToOldListener {

    private static String result;

    static void main() throws InterruptedException {
        W w1 = new W(runnable("1"));
        W w2 = new W(runnable("2"));

        MutableObservable<Widget> o = MutableObservable.withInitial(w1);
        Window.open(new SlotOld2(o));
        Thread.sleep(3000);
        System.out.println("CHANGE TO 2");
        o.set(w2);
        Thread.sleep(3000);
        System.out.println("CHANGE TO 1");
        o.set(w1);
        Thread.sleep(30000);
        System.out.println("DONE");
        o.set(new Text("kész"));

        // TODO olyan bug is megjelent, hogy "kész" szövegre átváltás után tovább ment a timer
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
        protected void onResume() {
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    result = null;
                    r.run();
                    System.out.println(result);
                }
            }, 0, 1000);
            untilPause().onClose(t::cancel);
        }

        @Override
        protected Widget build() {
            result = null;
            r.run();
            return new Text(result + "/" + Math.random());
        }
    }
}
