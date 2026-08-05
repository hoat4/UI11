package ui11;

public class TestStealDelegate {
    public void main() {
        // azt az esetet teszteljük, ha elvesszük egy W2 widgetnek a delegatejét (W1), majd újra refresheljük W2-t.
        // akkor sikeres a teszt, ha nem dobódnak exceptionök, hanem kiír 3 db X betűt.

        // TODO itt valami nem stimmel, mert ez most nem írhat ki X-eket, mert UV::toStringet hívjuk meg

        // ebben az esetben merült fel:
        // DefaultOverlayLayoutImpl ha nem kap constraintset a delegateje a natív peer, ha
        // viszont kap constraintset, akkor a UpValueWrapperbe wrappeli a natív peert.
        // a parent layout először adott neki constraintset, majd nem.
        // majd ablakátméretezéskor újra adott neki constraintset.

        WidgetTree.create(new Component() {

            @Remember private Slot outerSlot1;
            @Remember private Slot outerSlot2;
            @Remember private Slot outerSlot3;

            @Remember private Slot innerSlot;
            @Remember private Slot w2Slot;

            @Override
            protected void initState() {
                outerSlot1 = new Slot();
                outerSlot2 = new Slot();
                outerSlot3 = new Slot();
                innerSlot = new Slot();
                w2Slot = new Slot();
            }

            @Override
            protected void update() {
                Widget w1 = innerSlot.with(new W1());
                Widget w2 = w2Slot.with(new W2(w1));
                useComponent(outerSlot1.with(new T(w2)));
                useComponent(outerSlot2.with(new T(w1)));
                useComponent(outerSlot3.with(new T(w2)));
            }
        }, Runnable::run);
    }

    private static class T extends Widget {

        private final Widget w;

        public T(Widget w) {
            this.w = w;
        }

        @Override
        protected Widget build() {
            return PeerRequestor.ofSingle(w, UV.UVRequest.INSTANCE, result->{
                System.out.println(result.peer());
                return Component.ComponentResult.INSTANCE;
            });
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

    static class UV extends SubstitutedWidget {

        static class UVRequest extends PeerRequestor.Request<UV> {

            static final UVRequest INSTANCE = new UVRequest();

            private UVRequest() {
                super(UV.class);
            }
        }
    }
}
