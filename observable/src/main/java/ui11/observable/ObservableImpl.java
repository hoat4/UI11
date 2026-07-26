package ui11.observable;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

class ObservableImpl<T> implements MutableObservable<T> {

    private final InvalidationPoint ip = new InvalidationPoint();

    private T value;
    private EventSource<ChangeEvent<T>> changes;
    private Binding binding;

    // szándékosan nincs no-arg konstruktor, mert a null nem valid érték

    // át kéne állni arra hogy a default a nullable
    public ObservableImpl(T value) {
        validate(value);
        this.value = normalize(value);
    }

    @Override
    public T get() {
        ip.subscribe();
        return value;
    }

    // ez visszaadhatna booleant is, hogy változott-e az érték
    @Override
    public void set(T value) {
        value = normalize(value);
        validate(value);
        T prev = this.value;
        if (!Objects.equals(value, prev)) {
            this.value = value;
            T valueFinal = value;
            // value toString hosszú lehet, pl. Widget esetén
            // this-t mondjuk annyiból nincs értelme odaírni, hogy úgyis benne van a new value
            ip.invalidate(() -> "value change of " + this + "\nOld value: " + prev + "\nNew value: " + valueFinal);
            afterChange(prev, value);
            if (changes != null)
                changes.post(new ChangeEvent<>(prev, value));
        }
    }

    /**
     * ugyanaz mint set(), de visszaadja hogy változott-e az érték
     */
    public boolean set2(T value) {
        value = normalize(value);
        validate(value);
        T prev = this.value;
        if (!Objects.equals(value, prev)) {
            this.value = value;
            ip.invalidate();
            afterChange(prev, value);
            if (changes != null)
                changes.post(new ChangeEvent<>(prev, value));
            return true;
        } else
            return false;
    }

    public T getAndSet(T newValue) {
        T value = get();
        set(newValue);
        return value;
    }

    @Override
    public void bindTo(Supplier<T> valueSupplier, Scope scope) {
        if (binding != null && !binding.scope.isClosed()) {
            // TODO kezelni kéne, ha a régi scope closeja meghívja ezt a bindTot
            binding.closeWithoutValueReset();
        }
        binding = new Binding(scope, value);
        Observable.of(valueSupplier).getAndSubscribe(this::set, binding.scope);
    }

    protected void validate(T value) {
        Objects.requireNonNull(value);
    }

    protected T normalize(T value) {
        return value;
    }

    protected void afterChange(T oldValue, T newValue) {
    }

    @Override
    public String toString() {
        return "ObservableImpl{" + value + '}';
    }

    @Override
    public T snoop() {
        return value;
    }

    public void update(UnaryOperator<T> op) {
        set(op.apply(get()));
    }

    @Override
    public EventSource<ChangeEvent<T>> changes() {
        if (changes == null)
            changes = new EventSource<>();
        return changes;
    }

    public static class NullableObservableImpl<T> extends ObservableImpl<T> {
        public NullableObservableImpl(T value) {
            super(value);
        }

        @Override
        protected void validate(T value) {
        }
    }

    private class Binding {

        private final SimpleScope scope;
        private boolean hasPrevValue;

        public Binding(Scope scope, T prevValue) {
            this.scope = new SimpleScope(scope);
            this.hasPrevValue = true;

            // TODO ez a prevValue dolog nem feltétlen jó, mert pl. refreshScopepal használva
            //      a bindTo-t ide-oda állítgatódik az érték

            this.scope.onClose(() -> {
                if (hasPrevValue) {
                    hasPrevValue = false;
                    set(prevValue);
                }
            });
        }

        public void closeWithoutValueReset() {
            hasPrevValue = false;
            scope.close();
        }
    }
}
