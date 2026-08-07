package ui11;

import ui11.layout.multichild.LinearLayout;
import ui11.observable.InvalidationPoint;
import ui11.text.Text;
import ui11.window.Window;

import java.awt.*;

import static ui11.layout.multichild.LinearLayout.columnBuilder;
import static ui11.layout.multichild.LinearLayout.row;

public class ImplicitKeyTest {

    private static class A extends Widget {
        private static int counter;
        @Remember private int identity;

        @Override
        protected void initState() {
            identity = ++counter;
        }

        @Override
        protected Widget build() {
            return new Text("B" + identity);
        }
    }

    private static class C extends Widget {

        private final InvalidationPoint ip = new InvalidationPoint();

        @Remember private Key slot;

        @Override
        protected void initState() {
            slot = Key.create();
        }

        @Override
        protected Widget build() {
            ip.subscribe();
            LinearLayout.Builder ll = columnBuilder();
            String s = "C" + new Object().hashCode();
            ll.add(new Text(s));
            ll.add(row(new A(), new A()));
            ll.add(row(new A().withKey(slot), new A()));
            for (int i = 0; i < 25; i++)
                ll.add(row(new A(), new A()));
            return ll.build();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        C c = new C();
        Window.open(c);
        while (true) {
            EventQueue.invokeLater(() -> c.ip.invalidate()); // TODO AWT mellőzése
            Thread.sleep(1000);
        }
    }
}
