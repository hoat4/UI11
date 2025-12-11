package ui11.observable;

import java.util.function.Supplier;

public class ObserverHolder {

    @SuppressWarnings("AnonymousHasLambdaAlternative") // TeaVM nem tudja ThreadLocal.withInitialt
    private static final ThreadLocal<ObserverHolder> threadLocal = new ThreadLocal<>(){
        @Override
        protected ObserverHolder initialValue() {
            return new ObserverHolder();
        }
    };

    private static ObserverHolder cache;

    public final Thread thread = Thread.currentThread();
    // ezeket try-finally-ban módosítsuk
    public ObserverCollection obsC;
    public int obsI;

    public static ObserverHolder current() {
        ObserverHolder h = cache;
        if (h == null || h.thread != Thread.currentThread())
            cache = h = threadLocal.get();
        return h;
    }

    public static void withoutObserver(Runnable r) {
        ObserverHolder h = current();
        ObserverCollection prevC = h.obsC;
        int prevI = h.obsI;
        h.obsC = null;
        try {
            r.run();
        } finally {
            h.obsC = prevC;
            h.obsI = prevI;
        }
    }

    public static <R> R withoutObserver(Supplier<R> r) {
        ObserverHolder h = current();
        R result;
        ObserverCollection prevC = h.obsC;
        int prevI = h.obsI;
        h.obsC = null;
        try {
            result = r.get();
        } finally {
            h.obsC = prevC;
            h.obsI = prevI;
        }
        return result;
    }

    public static boolean hasNoObserver() {
        return current().obsC == null;
    }
}
