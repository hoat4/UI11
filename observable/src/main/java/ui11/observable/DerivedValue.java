package ui11.observable;

import ui11.observable.MutableObservable.ChangeEvent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

// ha ennek az osztálynak a nevét  megváltoztatjuk, akkor változtassuk meg TeaVMWidgetAccessorban
// a propagateType-nak átadott classnevet is!

class DerivedValue<T> implements Observable<T> {

    private final Supplier<T> supplier;

    private boolean watched;
    private T val;

    private final DerivedValueObserver obs = new DerivedValueObserver();

    private final EventSource<ChangeEvent<T>> changes = new ChangeEventEventSource();


    public DerivedValue(Supplier<T> supplier) {
        this.supplier = Objects.requireNonNull(supplier);
    }

    @Override
    public T get() {
        // watched == true esetén elég lenne val-t visszaadni, de
        // úgy nem iratkoznánk fel az observablekre,
        // és bonyolult lenne megcsinálni hogy akkor is működjön
        // ha közben changes összes subscriberét levesszük
        return supplier.get();
    }

    @Override
    public EventSource<ChangeEvent<T>> changes() {
        return changes;
    }

    private void updateValueWithObserver() {
        ObserverHolder h = ObserverHolder.current();
        ObserverCollection prevC = h.obsC;
        int prevI = h.obsI;
        try {
            h.obsC = obs;
            h.obsI = 1;
            val = supplier.get();
        } finally {
            h.obsC = prevC;
            h.obsI = prevI;
        }
    }

    private class DerivedValueObserver implements ObserverCollection {

        private final Set<ObservableBase> observables = new HashSet<>();

        @Override
        public void invalidate(int observerMask, Supplier<String> debugMessageSupplier) {
            removeObservers();
            if (!watched)
                throw new IllegalStateException();
            T prev = val;
            updateValueWithObserver();
            if (!Objects.equals(prev, val))
                changes.post(new ChangeEvent<>(prev, val));
        }

        @Override
        public void subscribedTo(ObservableBase observable) {
            observables.add(observable);
        }

        @Override
        public void checkObserver(int mask) {
        }

        void removeObservers() {
            observables.forEach(o -> o.removeObserver(this, 1));
            observables.clear();
        }
    }

    final class ChangeEventEventSource extends EventSource<ChangeEvent<T>> {

        @Override
        void onFirstSubscriber() {
            watched = true;
            updateValueWithObserver();
        }

        @Override
        void onLastSubscriber() {
            watched = false;
            obs.removeObservers();
        }
    }
}
