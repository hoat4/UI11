package ui11;

public class TestStealDelegate {
    public void main() {
        // azt az esetet teszteljük, ha elvesszük egy W2 widgetnek a delegatejét (W1), majd újra refresheljük W2-t.
        // akkor sikeres a teszt, ha nem dobódnak exceptionök, hanem kiír 3 db X betűt.

        // ebben az esetben merült fel:
        // DefaultOverlayLayoutImpl ha nem kap constraintset a delegateje a natív peer, ha
        // viszont kap constraintset, akkor a UpValueWrapperbe wrappeli a natív peert.
        // a parent layout először adott neki constraintset, majd nem.
        // majd ablakátméretezéskor újra adott neki constraintset.

        WidgetTree.create(new Component<>() {

            @Inject private Slot outerSlot1;
            @Inject private Slot outerSlot2;
            @Inject private Slot outerSlot3;

            @Inject private Slot innerSlot;
            @Inject private Slot w2Slot;

            @Override
            protected Void update() {
                Widget w1 = new W1().withSlot(innerSlot);
                Widget w2 = new W2(w1).withSlot(w2Slot);
                useComponent(outerSlot1, new T(w2));
                useComponent(outerSlot2, new T(w1));
                useComponent(outerSlot3, new T(w2));
                return null;
            }
        }, Runnable::run);
    }

    private static class T extends Component<Void> {

        private final Widget w;

        @Inject private Slot slot;

        public T(Widget w) {
            this.w = w;
        }

        @Override
        protected Void update() {
            System.out.println(useWidget(slot, w, UV.class));
            return null;
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
            return new UV();
        }
    }

    private static class UV extends EndingWidget {
    }
}
