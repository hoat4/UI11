package ui11.observable;

import java.util.function.Supplier;

public class ObserverHolder {

    @SuppressWarnings("AnonymousHasLambdaAlternative") // TeaVM nem tudja ThreadLocal.withInitialt
    private static final ThreadLocal<ObserverHolder> threadLocal = new ThreadLocal<>() {
        @Override
        protected ObserverHolder initialValue() {
            return new ObserverHolder();
        }
    };

    private static ObserverHolder cache;

    public final Thread thread = Thread.currentThread();
    // ezt try-finally-ban módosítsuk
    ObserverCollection obsC;

    public static ObserverHolder current() {
        ObserverHolder h = cache;
        if (h == null || h.thread != Thread.currentThread())
            cache = h = threadLocal.get();
        return h;
    }

    public static void withoutObserver(Runnable r) {
        ObserverHolder h = current();
        ObserverCollection prevC = h.obsC;
        h.obsC = null;
        try {
            r.run();
        } finally {
            h.obsC = prevC;
        }
    }

    public static <R> R withoutObserver(Supplier<R> r) {
        ObserverHolder h = current();
        R result;
        ObserverCollection prevC = h.obsC;
        h.obsC = null;
        try {
            result = r.get();
        } finally {
            h.obsC = prevC;
        }
        return result;
    }

    public static boolean hasNoObserver() {
        return current().obsC == null;
    }

    public void setObserver(ObserverCollection observer) {
        if (obsC != null)
            throw new IllegalStateException("thread already has an observer");
        obsC = observer;
    }

    public void clearObserver(ObserverCollection observer) {
        if (obsC != observer)
            throw new IllegalStateException("thread has different observer than " + observer + ": " + obsC);
        obsC = null;
    }

    public boolean hasObserver() {
        return obsC != null;
    }

    public void ensureNoCurrentObserver() {
        if (hasObserver())
            throw new RuntimeException("thread already has an observer: " + obsC);
    }

    public ObserverCollection pushObserver(ObserverCollection observer) {
        ObserverCollection prev = obsC;
        obsC = observer;
        return prev;
    }

    public void popObserver(ObserverCollection curr, ObserverCollection prev) {
        if (obsC != curr)
            throw new IllegalStateException("thread has different observer than " + curr + ": " + obsC);
        obsC = prev;
    }
}
