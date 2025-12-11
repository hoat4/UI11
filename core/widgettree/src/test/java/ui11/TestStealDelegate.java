package ui11;

import ui11.provide.UpValue;
import ui11.provide.UpValueWrapper;

public class TestStealDelegate {
    public void main() {
        // azt az esetet teszteljük, ha elvesszük egy W2 widgetnek a delegatejét (W1), majd újra refresheljük W2-t.
        // akkor sikeres a teszt, ha nem dobódnak exceptionök, hanem kiír 3 db X betűt.

        // ebben az esetben merült fel:
        // DefaultOverlayLayoutImpl ha nem kap constraintset a delegateje a natív peer, ha
        // viszont kap constraintset, akkor a UpValueWrapperbe wrappeli a natív peert.
        // a parent layout először adott neki constraintset, majd nem.
        // majd ablakátméretezéskor újra adott neki constraintset.

        new RootElement(new Component() {

            @Inject private Slot outerSlot1;
            @Inject private Slot outerSlot2;
            @Inject private Slot outerSlot3;

            @Inject private Slot innerSlot;
            @Inject private Slot w2Slot;

            @Override
            protected void update() {
                Widget w1 = innerSlot.use(new W1());
                Widget w2 = w2Slot.use(new W2(w1));
                outerSlot1.instantiate(new T(w2));
                outerSlot2.instantiate(new T(w1));
                outerSlot3.instantiate(new T(w2));
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
