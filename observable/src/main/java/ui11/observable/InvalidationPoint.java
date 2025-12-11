package ui11.observable;

import java.util.function.Supplier;

public class InvalidationPoint extends ObservableBase implements Observer {

    public void subscribe() {
        onRead();
    }

    @Override
    public void invalidate() {
        onWrite();
    }

    void invalidate(Supplier<String> debugMessageSupplier) {
        onWrite(debugMessageSupplier);
    }
}
