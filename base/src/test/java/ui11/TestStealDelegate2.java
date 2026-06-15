package ui11;

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

        WidgetTree.create(new Component<>() {

            @Inject private Slot outerSlot1;
            @Inject private Slot outerSlot2;
            @Inject private Slot outerSlot3;

            @Inject private Slot innerSlot;
            @Inject private Slot w3Slot;

            @Override
            protected Void update() {
                Widget w1 = new W1().withSlot(innerSlot);
                Widget w3 = new W3(new W2(w1)).withSlot(w3Slot);
                useComponent(outerSlot1, new T(w3));
                useComponent(outerSlot2, new T(w1));
                useComponent(outerSlot3, new T(w3));
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
            System.out.println(useWidget(slot, w, TestStealDelegate.UV.UVRequest.INSTANCE));
            return null;
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
            return new TestStealDelegate.UV();
        }
    }
}
