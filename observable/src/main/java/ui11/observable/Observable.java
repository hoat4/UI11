package ui11.observable;

import org.jspecify.annotations.NonNull;
import ui11.observable.MutableObservable.ChangeEvent;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Observable<T> {

    // TODO definiálni kéne hogy ha ezt meghívjuk, akkor csak akkor invalidálódhatnak
    //      az observerek ha megváltozott a prop értéke, vagy spontán is
    //      (DerivedValue-féleségeknél lehet hogy csak a forrás értéke változott meg)
    T get();

    // TODO dokumentálni kéne, hogy kerülhetünk állapotba kerülünk ahol nem elérhető az érték
    //      (pl. Element::inherited csak akkor, ha az Element fában van),
    //      ilyenkor get milyen exceptiont dobjon get, és hogyan viselkedjen a changes()

    default EventSource<ChangeEvent<T>> changes() {
        return of(this::get).changes();
    }

    /**
     * ennek a getje mindig meghívja f-et
     */
    // TODO meg kéne csinálni úgy, hogy csak akkor hívja meg f-et, ha változik az értéke
    //      (és akkor a hívó hagyatkozhat pl. f által visszadott objektum identity-jére)
    default <R> Observable<R> map(Function<T, R> f) {
        return of(() -> f.apply(get()));
    }

    default void getAndSubscribe(Consumer<T> consumer, Scope scope) {
        consumer.accept(get());
        changes().subscribe(scope, evt -> consumer.accept(evt.newValue()));
    }

    default T snoop() {
        return ObserverHolder.withoutObserver(this::get);
    }

    static <T> Observable<T> constant(@NonNull T value) {
        Objects.requireNonNull(value);
        return new Observable<>() {
            @Override
            public T get() {
                return value;
            }

            private final EventSource<ChangeEvent<T>> eventSource = new EventSource<>();
            // jobb lenne ehelyett valami nullEventSource vagy ilyesmi

            @Override
            public EventSource<ChangeEvent<T>> changes() {
                return eventSource;
            }
        };
    }

    static <T> Observable<T> of(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return new DerivedValue(supplier);
    }

    /*
    static <T, R> ReadableObservable<R> map(Observable<T> src, Function<T, R> f) {
        return () -> f.apply(src.get());
    }

    static <T> ReadableObservable<T> lazy(Supplier<T> t) {
        return new ReadableObservable<T>() {

            private T val;

            @Override
            public T get() {
                if (val == null && (val = t.get()) == null)
                    throw new RuntimeException("supplier returned null: " + t);
                return val;
            }
        };
    }
     */
}
