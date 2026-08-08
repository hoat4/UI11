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

            @Remember private Key outerSlot1;
            @Remember private Key outerSlot2;
            @Remember private Key outerSlot3;

            @Remember private Key innerSlot;
            @Remember private Key w2Slot;

            @Override
            protected void initState() {
                outerSlot1 = Key.create();
                outerSlot2 = Key.create();
                outerSlot3 = Key.create();
                innerSlot = Key.create();
                w2Slot = Key.create();
            }

            @Override
            protected void update() {
                Widget w1 = new W1().withKey(innerSlot);
                Widget w2 = new W2(w1).withKey(w2Slot);
                useComponent(new T(w2).withKey(outerSlot1));
                useComponent(new T(w1).withKey(outerSlot2));
                useComponent(new T(w2).withKey(outerSlot3));
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
                System.out.println(result);
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
