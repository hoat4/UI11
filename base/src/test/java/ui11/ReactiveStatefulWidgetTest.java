package ui11;

import ui11.color.Color;
import ui11.input.gesture.ClickListener;
import ui11.layout.Gap;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Padding;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.provide.Provider;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.decoration.Background.withBackground;
import static ui11.geom.Length.em;
import static ui11.geom.Length.px;
import static ui11.graphics.Empty.empty;
import static ui11.layout.multichild.LinearLayout.column;

// TODO kéne arra is teszt, hogy az RSW változik, de inputmezői ugyanazok maradnak
//      (egyszer crashelt ilyenkor, mert listeners.keySet() null volt)

public class ReactiveStatefulWidgetTest {

    interface IntSupplier {
        int value();
    }

    static class RSW1 extends Widget {

        private final int i;

        @Inject private Observable<Integer> i1;
        @Inject private IntSupplier i2;

        @Remember private MutableObservable<Integer> j;

        public RSW1(int i) {
            System.out.println("CREATE " + Integer.toUnsignedString(System.identityHashCode(this), 16));
            this.i = i;
        }

        @Override
        protected void initState() {
            System.out.println("INIT " + Integer.toUnsignedString(System.identityHashCode(this), 16));
            j = MutableObservable.withInitial(1);
        }

        @Override
        protected Widget build() {
            String identity = Integer.toUnsignedString(System.identityHashCode(this), 16);
            System.out.println("BUILD " + Integer.toUnsignedString(System.identityHashCode(this), 16));
            return Align.center(
                    new ClickListener(
                            withBackground(Color.YELLOW,
                                    Padding.allSides(px(8),
                                            column(
                                                    new Text("Input: " + i),
                                                    new Text("Inherited (observable): " + i1.get()),
                                                    new Text("Inherited (interface proxy): " + i2.value()),
                                                    new Text("State: " + j.get()),
                                                    new Text("Identity: 0x" + identity),
                                                    Gap.vertical(em(1)),
                                                    new Text("Click to increment state var")
                                            )
                                    )
                            ),
                            () -> {
                                j.set(j.get() + 1);
                                System.out.println("INCREMENT");
                            }
                    )
            );
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final class W extends Widget {
            private final MutableObservable<? extends Widget> content;
            private final MutableObservable<Integer> i1;
            private final MutableObservable<Integer> i2;

            W(MutableObservable<? extends Widget> content,
              MutableObservable<Integer> i1,
              MutableObservable<Integer> i2) {
                this.content = content;
                this.i1 = i1;
                this.i2 = i2;
            }

            @Override
            protected Widget build() {
                int i2val = i2.get();
                return new Provider<>(Integer.class, i1.get(),
                        new Provider<>(IntSupplier.class, () -> i2val,
                                content.get()
                        )
                );
            }
        }

        MutableObservable<Widget> slot = MutableObservable.withInitial(empty());
        MutableObservable<Integer> inheritedInt = MutableObservable.withInitial(0);
        MutableObservable<Integer> inheritedInt2 = MutableObservable.withInitial(0);
        Window.open(new W(slot, inheritedInt, inheritedInt2));

        for (int i = 0; ; i++) {
            Thread.sleep(1000);
            slot.set(new RSW1(i));
            Thread.sleep(1000);
            inheritedInt.set(inheritedInt.get() + 1);
            Thread.sleep(1000);
            inheritedInt.set(inheritedInt.get() + 1);
            Thread.sleep(1000);
            inheritedInt2.set(inheritedInt2.get() + 1);
        }
    }
}
