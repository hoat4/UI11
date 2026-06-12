package ui11.observable;

import java.util.function.Supplier;

public interface ObserverCollection {

    void invalidate(int observerMask, Supplier<String> debugMessageSupplier);

    void subscribedTo(ObservableBase observable);

    void checkObserver(int mask);
}
