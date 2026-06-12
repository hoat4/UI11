package ui11;

import ui11.provide.UpValue;
import ui11.provide.UpValueWrapper;

public class TestStealDelegate2 {
    public void main() {
        // ezt az esetet teszteljük:

        // 1.
        // root
        //    |- W3
        //        |- W2
        //            |- W1

        // 2. (elvesszük W2-től a delegateet, azaz W1-et)
        // root
        //    |- W1


        // 3. (újra visszarakjuk az egészet, így W2-nek a tőle már elvett W1-et vissza kéne raknia)
        // root
        //    |- W3
        //        |- W2
        //            |- W1

        // TODO végig kéne gondolni hogy mi van ha nem delegateekről van szó, hanem sima childekről

        // akkor sikeres a teszt, ha nem dobódnak exceptionök, hanem kiír 3 db X betűt.

        // lásd még TestStealDelegate osztály

        new RootElement(new Component() {

            @Inject private Slot outerSlot1;
            @Inject private Slot outerSlot2;
            @Inject private Slot outerSlot3;

            @Inject private Slot innerSlot;
            @Inject private Slot w3Slot;

            @Override
            protected void update() {
                Widget w1 = innerSlot.use(new W1());
                Widget w3 = w3Slot.use(new W3(new W2(w1)));
                outerSlot1.instantiate(new T(w3));
                outerSlot2.instantiate(new T(w1));
                outerSlot3.instantiate(new T(w3));
            }
        }, Runnable::run).start();
    }

    private static class T extends Component {

        private final Widget w;

        @Inject private Slot slot;

        public T(Widget w) {
            this.w = w;
        }

        @Override
        protected void update() {
            System.out.println(slot.instantiate(w).lookup(UV.class));
        }
    }

    private static class W3 extends Widget {

        private final Widget w;

        public W3(Widget w) {
            this.w = w;
        }

        @Override
        protected Widget build() {
            return w;
        }
    }

    private static class W2 extends Widget {

        private final Widget widgetThatWillBeDelegate; // key-jel wrappelt

        public W2(Widget widgetThatWillBeDelegate) {
            this.widgetThatWillBeDelegate = widgetThatWillBeDelegate;
        }

        @Override
        protected Widget build() {
            return widgetThatWillBeDelegate;
        }
    }

    private static class W1 extends Widget {
        @Override
        protected Widget build() {
            return new UpValueWrapper(UV.X);
        }
    }

    static enum UV implements UpValue {X, Y}
}
