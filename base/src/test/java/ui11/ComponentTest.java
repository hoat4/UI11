/*
package ui11;

import com.flyordie.component.Component.ComponentState;
import ui11.observable.Observable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

public class ComponentTest {

    @Test
    public void testStartEmpty() {
        class C1 extends RootComponent {
        }

        C1 c1 = new C1();
        assertEquals(ComponentState.INITIAL, c1.componentState());
        c1.start(new TestContext());
        assertEquals(ComponentState.STARTED, c1.componentState());
    }

    @Test
    public void testStartEmptyWithInitializer() {
        class C2 extends RootComponent {

            private int callCount;

            @RepeatedInit
            void init() {
                assertEquals(ComponentState.STARTING, componentState());
                callCount++;
            }
        }

        C2 c = new C2();
        assertEquals(ComponentState.INITIAL, c.componentState());
        c.start(new TestContext());
        assertEquals(ComponentState.STARTED, c.componentState());
        assertEquals(1, c.callCount);
    }

    @Test
    public void testSingleChildAddedBeforeStart() {
        class C4 extends InitCounterComponent {
            @Override
            protected void customInit() {
                assertEquals(ComponentState.STARTING, componentState());
            }
        }
        class C3 extends InitCounterRootComponent {

            final C4 c4 = new C4();

            {
                addChild(c4);
            }

            @Override
            protected void customInit() {
                assertEquals(ComponentState.STARTING, componentState());
                assertEquals(ComponentState.STARTED, c4.componentState());
            }
        }

        C3 c = new C3();
        assertEquals(ComponentState.INITIAL, c.componentState());
        c.start(new TestContext());
        assertEquals(ComponentState.STARTED, c.componentState());
        assertEquals(ComponentState.STARTED, c.c4.componentState());
        assertEquals(1, c.callCount);
        assertEquals(1, c.c4.callCount);
    }

    @Test
    public void testSingleChildAddedInsideStart() {
        class C4 extends InitCounterComponent {
            @Override
            protected void customInit() {
                assertEquals(ComponentState.STARTING, componentState());
            }
        }
        class C3 extends InitCounterRootComponent {

            final C4 c4 = new C4();

            @Override
            protected void customInit() {
                assertEquals(ComponentState.STARTING, componentState());
                assertEquals(ComponentState.INITIAL, c4.componentState());
                addChild(c4);
                assertEquals(ComponentState.STARTING, componentState());
                assertEquals(ComponentState.STARTED, c4.componentState());
            }
        }

        C3 c = new C3();
        assertEquals(ComponentState.INITIAL, c.componentState());
        c.start(new TestContext());
        assertEquals(ComponentState.STARTED, c.componentState());
        assertEquals(ComponentState.STARTED, c.c4.componentState());
        assertEquals(1, c.callCount);
        assertEquals(1, c.c4.callCount);
    }

    /*
    @Test
    public void testVaryingChild() {
        class C6 extends Component {
        }
        class C5 extends RootComponent {
            final InvalidationPoint ip = new InvalidationPoint();
            @Subcomponent
            final ReadableObservable<C6> c = new ValueCacher<>(() -> {
                ip.subscribe();
                return new C6();
            }, true);
        }

        C5 c = new C5();
        c.start();
        C6 c6_1 = c.c.get();
        assertEquals(ComponentState.RUNNING, c6_1.componentState());
        assertEquals(c.c.get(), c6_1);
        c.ip.invalidate();
        C6 c6_3 = c.c.get();
        // TODO assertNotEquals(c6_1, c6_3);
        assertFalse(c6_1.equals(c6_3));
        assertEquals(ComponentState.RUNNING, c6_3.componentState());
        assertEquals(ComponentState.PAUSED, c6_1.componentState());
    }

    @Test
    public void testVaryingChild_addedAlsoAsRegularChild() {
        class C8 extends Component {
        }
        class C7 extends RootComponent {

            final InvalidationPoint ip = new InvalidationPoint();
            final C8 c8 = new C8();
            int counter;

            @Subcomponent
            final ReadableObservable<C8> c = new ValueCacher<>(() -> {
                counter++;
                ip.subscribe();
                return c8;
            }, true);

            {
                addChild(c8);
            }

            @Provider
            RootElement context() {
                return new RootElement() {
                    @Override
                    public void enqueueForRevalidation(Revalidable revalidable) {
                    }

                    @Override
                    public void removeRevalidationEntry(Revalidable revalidable) {
                    }
                };
            }
        }

        C7 c = new C7();
        c.start();
        C8 c6_1 = c.c.get();
        assertEquals(ComponentState.RUNNING, c6_1.componentState());
        assertEquals(c.c.get(), c6_1);
        assertEquals(1, c.counter);
        c.ip.invalidate();
        C8 c6_3 = c.c.get();
        assertEquals(2, c.counter);
        assertEquals(c6_1, c6_3);
        assertEquals(ComponentState.RUNNING, c6_3.componentState());
    }

     *

    // kéne olyan teszt is, ami azt nézi hogy ha hozzá van adva regular childként
    // és varyingként is, de a varyingot megváltoztatjuk, akkor a regular nem invalidálódik-e

    @Test
    public void testRemoveChildChangesGrandchildDepth() {
        class C11 extends RootComponent {
        }
        class C12 extends Component {
        }
        class C13 extends Component {
        }
        class C14 extends Component {
        }

        C11 c11 = new C11();
        C12 c12 = new C12();
        C13 c13 = new C13();
        C14 c14 = new C14();
        c11.addChild(c12);
        c11.addChild(c13);
        c12.addChild(c13);
        c13.addChild(c14);
        c11.start(new TestContext());
        assertEquals(List.of(0, 1, 2, 3), List.of(c11.depth, c12.depth, c13.depth, c14.depth));
        c12.removeChild(c13);
        assertEquals(List.of(0, 1, 1, 2), List.of(c11.depth, c12.depth, c13.depth, c14.depth));
    }

    @Test
    public void testInheritedValues() {
        TestContext ctx = new TestContext();
        Observable<Integer> o = new Observable<>(1);
        class C21 extends RootComponent {
            @Provider
            int i() {
                return o.get();
            }
        }
        class C22 extends Component {

            Integer i;

            int j() {
                return treeValue(int.class);
            }

            @RepeatedInit
            void m() {
                i = j();
            }
        }

        C21 c1 = new C21();
        C22 c2 = new C22();
        c1.addChild(c2);
        c1.start(ctx);

        assertEquals(1, c2.j());
        o.set(2);
        assertEquals(1, c2.i.intValue());
        assertEquals(2, c2.j());
        ctx.revalidate();
        assertEquals(2, c2.i.intValue());
        assertEquals(2, c2.j());
    }

    private static abstract class InitCounterComponent extends Component {

        int callCount;

        @RepeatedInit
        void init() {
            callCount++;
            customInit();
        }

        protected abstract void customInit();
    }

    private static abstract class InitCounterRootComponent extends RootComponent {

        int callCount;

        @RepeatedInit
        void init() {
            callCount++;
            customInit();
        }

        protected abstract void customInit();
    }

    private static class TestContext implements RootElement {

        final List<Revalidable> queue = new ArrayList<>();

        void revalidate() {
            while (!queue.isEmpty()) {
                queue.sort(Comparator.comparing(Revalidable::priority));
                queue.remove(0).revalidate();
            }
        }

        @Override
        public void enqueueForRevalidation(Revalidable revalidable) {
            queue.add(revalidable);
        }

        @Override
        public void removeRevalidationEntry(Revalidable revalidable) {
            if (!queue.remove(revalidable))
                throw new IllegalArgumentException();
        }
    }
}
*/