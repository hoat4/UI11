package ui11.observable;

import ui11.observable.ObservableImpl.NullableObservableImpl;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

// nullability-t ki kéne fejezni típusokon keresztül?
// equals/hashCode nem függ az aktuális értéktől

/**
 * Writeable variant of {@link Observable}.
 */
public interface MutableObservable<T> extends Observable<T> {

    @Override
    T get();

    void set(T t);

    // TODO mi legyen, ha több binding van? most egyelőre annyi van,
    //      hogy a később érkező törli a korábbit.
    //      majd talán kéne csinálni valami ellenőrzést, hogy ha
    //      refresh utáni inaktiválgatás után is egyszerre van kettő is,
    //      akkor kiírjuk hibát.
    // TODO ez jelenleg nem működik (vagy legalábbis warningol), ha a scope=untilNextRebuild():
    //      ugyanis a scope bezárultával (ami a következő rebuild elején van) visszaírja az előző értéket,
    //      és erről próbálná értesíteni az olvasót, miközben még ugyanebben a refreshben újra beírná
    //      azt az értéket, ami nagy valószínűséggel ugyanaz, mint a binding megszüntetése előtti.
    //      és emiatt folyton Observed value was invalidated, but node is in REFRESHING_CHILDREN_AFTER_SELF lesz.
    void bindTo(Supplier<T> valueSupplier, Scope scope);

//    static <T> void bind(Supplier<Observable<T>> destination, Supplier<T> source, Scope scope)

    @Override
    default EventSource<ChangeEvent<T>> changes() {
        return Observable.of(this::get).changes();
    }

    // TODO a visszaadott értéken nem fog jól működni equals
    // TODO ha getMapper több értéket ugyanarra mappel, akkor felesleges invalidationök fognak generálódni
    default <T2> MutableObservable<T2> mapBidirectional(Function<T, T2> getMapper, BiFunction<T, T2, T> setMapper) {
        return new MutableObservable<T2>() {
            @Override
            public T2 get() {
                return getMapper.apply(MutableObservable.this.get());
            }

            @Override
            public void set(T2 t2) {
                MutableObservable.this.set(setMapper.apply(MutableObservable.this.get(), t2));
            }

            @Override
            public void bindTo(Supplier<T2> valueSupplier, Scope scope) {
                MutableObservable.this.bindTo(() -> setMapper.apply(MutableObservable.this.get(), valueSupplier.get()), scope);
            }
        };
    }

    // TODO inkonzisztens, hogy melyik nullable és melyik nonnull

    /**
     * nonnull
     */
    static <T> MutableObservable<T> withInitial(T initialValue) {
        return new ObservableImpl<>(initialValue);
    }

    /**
     * nullable
     */
    static <T> MutableObservable<T> withInitial(T initialValue, Consumer<T> validator) {
        return new ObservableImpl<>(initialValue) {
            @Override
            protected void validate(T value) {
                validator.accept(value);
            }
        };
    }

    static <T> MutableObservable<T> ofNullable() {
        return new NullableObservableImpl<>(null);
    }

    static <T> MutableObservable<T> ofNullable(T initialValue) {
        return new NullableObservableImpl<>(initialValue);
    }

    /**
     * oldValue nem lehet egyenlő newValue-ban, Objects.equals szerint
     */
    record ChangeEvent<T>(T oldValue, T newValue) {
        public ChangeEvent {
            if (Objects.equals(oldValue, newValue))
                throw new IllegalArgumentException("old value equals new value: " +
                        oldValue + ", " + newValue);
        }
    }

    default void debug_traceUnsubscribes() {
        throw new UnsupportedOperationException();
    }

    // public Lock lock() { ... }

    /*

    pl. Box.backgroundnál lett volna használva, hogy ne kelljen cserélgetni folyton a ColorFilleket.
    de olyan macerás lenne egy ilyen update függvényt használni, hogy inkább csinálunk Slot-ba SE view cacheelést.
    bár pl. Box.border esetén az már nehezebb.

    static <T, H extends T> void update(Observable<T> prop, Class<H> requiredClass, Supplier<H> supplier,
                                        Consumer<H> c) {
        throw new RuntimeException("TODO");
    }

    static <T, I, H extends I> void update(Observable<T> prop, Function<T, I> nav,
                                           Function<H, T> supplier,
                                           Supplier<H> hsupplier,
                                           Consumer<H> c) {
        throw new RuntimeException("TODO");
    }

     */
}
