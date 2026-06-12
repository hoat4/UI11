package ui11;

import ui11.observable.InvalidationPoint;
import ui11.text.Text;
import ui11.window.Window;

import java.awt.*;

public class ImplicitViewKeyTest {

    private static class A extends Widget {

        private static int counter;

        @Remember private int identity;

        @Override
        protected void initState() {
            identity = ++counter;
        }

        @Override
        protected Widget build() {
            return new Text(identity);
        }
    }

    private static class C extends Widget {

        final InvalidationPoint ip = new InvalidationPoint();

        @Override
        protected Widget build() {
            ip.subscribe();
            return new A();
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
