package ui11;

public class UpValueInvalidationBugTest {
    public void main() {
        // TODO ez a teszt valszeg hülyeség, mert nem lehet mit kezdeni ezzel, hogy kétszer is instantiate-eli
        //      ugyanazt a widgetet más input változókkal

        // a teszt sikeres, ha nincs az stderren ilyesmi:
        // [main] WARN ui11.ObservableHelper - Observed value was invalidated, but node is in REFRESHING_SELF_BEFORE_CHILDREN state: ui11.UpValueInvalidationBugTest$1@24a35978
        //
        // de végülis talán nem is kell működnie, mert duplicate keyek meg lesznek tiltva.
        // akkor majd ki lehet törölni ezt a tesztet.

        final class W1 extends Widget {
            private final int i;

            W1(int i) {
                this.i = i;
            }

            @Override
            protected Widget build() {
                return new U2(i);
            }
        }

        WidgetTree.create(new Component() {

            @Remember private Slot2 slot;

            @Override
            protected void update() {
                // szándékosan ugyanaz a slot
                useComponent(slot.with(new W1(1)), U2.U2Request.INSTANCE);
                useComponent(slot.with(new W1(2)), U2.U2Request.INSTANCE);
            }
        }, Runnable::run);
    }

    private static class U2 extends SubstitutedWidget {

        final int i;

        U2(int i) {
            this.i = i;
        }

        static class U2Request extends PeerRequestor.Request<U2> {

            static final U2Request INSTANCE = new U2Request();

            private U2Request() {
                super(U2.class);
            }
        }
    }
}
