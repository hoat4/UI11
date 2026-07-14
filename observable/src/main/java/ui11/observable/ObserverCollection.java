package ui11.observable;

import java.util.function.Supplier;

public interface ObserverCollection {

    void invalidate(Supplier<String> debugMessageSupplier);

    void subscribedTo(ObservableBase observable);
}
