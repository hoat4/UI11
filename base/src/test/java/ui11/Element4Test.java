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

        WidgetTree.create(new Component() {
            @Override
            protected void update() {
                a[0]++;
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
            protected void update() {
                a[0]++;
                a[1] = i.get();
            }
        }

        WidgetTree.create(new Component() {

            @Remember private Key key;

            @Override
            protected void initState() {
                key = Key.create();
            }

            @Override
            protected void update() {
                useComponent(new Provider<>(Integer.class, 1347, new E()).withKey(key));
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
