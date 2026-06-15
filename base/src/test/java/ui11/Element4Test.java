package ui11;

import ui11.observable.Observable;
import ui11.provide.Provider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class Element4Test {

    private ExecutorService executor;

    @Before
    public void startExecutor() {
        executor = Executors.newSingleThreadExecutor();
    }

    @Test
    public void testStartSimpleRoot() {
        int[] a = {0};

        WidgetTree.create(new Component<>() {
            @Override
            protected Void update() {
                a[0]++;
                return null;
            }
        }, executor);

        assertEquals(1, a[0]);
    }

    @Test
    public void testSimpleInheritedValue() {
        int[] a = {0, 0};
        class E extends Component {

            @Inject private Observable<Integer> i;

            @Override
            protected Void update() {
                a[0]++;
                a[1] = i.get();
                return null;
            }
        }

        WidgetTree.create(new Component<>() {

            @Inject private Slot slot;

            @Override
            protected Void update() {
                useWidget(slot, new Provider<>(Integer.class, 1347,
                        new E()), ComponentResultRequest.instance());
                return null;
            }
        }, executor);

        assertEquals(1, a[0]);
        assertEquals(1347, a[1]);
    }

    @After
    public void stopExecutor() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.SECONDS))
            throw new RuntimeException();
    }
}
