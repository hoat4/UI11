package ui11;

import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.provide.Provider;
import ui11.text.Text;
import ui11.window.Window;

// To run this test, change WidgetTree.TRACE_CLEANUP_UNUSED_IV to true.
// The test passes if every time around "readIV = false" message appears
// on the stdout, a "Remove IVCollector ... from W1..." message also appears,
// and the text on the window alternates between "Read success" and "Not read".

public class CleanupUnusedIVCollectorTest {

    static void main() throws InterruptedException {
        MutableObservable<Boolean> readIV = MutableObservable.withInitial(true);
        System.out.println("readIV = true");
        Window.open(new Provider<>(TestIV.class, new TestIV(), new W1(readIV)));
        while (true) {
            Thread.sleep(3000);
            readIV.set(!readIV.get());
            System.out.println("readIV = " + readIV.get());
        }
    }

    static class W1 extends Widget {

        private final Observable<Boolean> readIV;

        public W1(Observable<Boolean> readIV) {
            this.readIV = readIV;
        }

        @Override
        protected Widget build() {
            return readIV.get() ? new W2() : new W3();
        }
    }

    static class W2 extends Widget {

        @Inject private TestIV i;

        @Override
        protected Widget build() {
            return new Text(i.toString());
        }
    }

    static class W3 extends Widget {

        @Override
        protected Widget build() {
            return new Text("Not read");
        }
    }

    private static class TestIV {
        @Override
        public String toString() {
            return "Read success";
        }
    }
}
