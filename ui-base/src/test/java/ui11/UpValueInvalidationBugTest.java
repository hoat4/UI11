package ui11;

import ui11.provide.UpValue;
import ui11.provide.UpValueWrapper;

public class UpValueInvalidationBugTest {
    public void main() {
        // a teszt sikeres, ha nincs az stderren ilyesmi:
        // [main] WARN ui11.ObservableHelper - Observed value was invalidated, but node is in REFRESHING_SELF_BEFORE_CHILDREN state: ui11.UpValueInvalidationBugTest$1@24a35978
        //
        // de végülis talán nem is kell működnie, mert duplicate keyek meg lesznek tiltva.
        // akkor majd ki lehet törölni ezt a tesztet.

        record U2(int i) implements UpValue {}
        final class W1 extends Widget {
            private final int i;

            W1(int i) {
                this.i = i;
            }

            @Override
            protected Widget build() {
                return new UpValueWrapper(new U2(i));
            }
        }

        new RootElement(new Component() {

            @Inject private Slot slot;

            @Override
            protected void update() {
                // szándékosan ugyanaz a slot
                slot.instantiate(new W1(1)).lookup(U2.class);
                slot.instantiate(new W1(2)).lookup(U2.class);
                System.out.println("done");
            }
        }, Runnable::run).start();
    }
}
