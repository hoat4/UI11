package ui11.observable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

public class ObservableSet<E> extends ObservableBase implements Set<E> {

    private final Set<E> delegate;

    public ObservableSet() {
        this(new HashSet<>());
    }

    private ObservableSet(Set<E> delegate) {
        this.delegate = delegate;
    }

    public static <E> ObservableSet<E> wrap(Set<E> set) {
        return new ObservableSet<>(set);
    }

    public static <E> ObservableSet<E> withInitial(Collection<E> set) {
        return wrap(new HashSet<>(set));
    }

    // TODO ehelyett inkább egy collector kéne
    public static <E> ObservableSet<E> withInitial(Stream<E> elements) {
        return wrap(elements.collect(Collectors.toCollection(HashSet::new)));
    }

    @Override
    public int size() {
        onRead();
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        onRead();
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        onRead();
        return delegate.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        onRead();
        return delegate.iterator(); // TODO Iterator::remove
    }

    @Override
    public Object[] toArray() {
        onRead();
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        onRead();
        return delegate.toArray(a);
    }

    @Override
    public boolean add(E e) {
        if (delegate.add(e)) {
            onWrite();
            return true;
        } else
            return false;
    }

    @Override
    public boolean remove(Object o) {
        if (delegate.remove(o)) {
            onWrite();
            return true;
        } else
            return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        onRead();
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (delegate.addAll(c)) {
            onWrite();
            return true;
        } else
            return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (delegate.removeAll(c)) {
            onWrite();
            return true;
        } else
            return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (delegate.retainAll(c)) {
            onWrite();
            return true;
        } else
            return false;
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            delegate.clear();
            onWrite();
        }
    }

    @Override
    public boolean equals(Object o) {
        onRead();
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        onRead();
        return delegate.hashCode();
    }

    @Override
    public Spliterator<E> spliterator() {
        onRead();
        return delegate.spliterator();
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        onRead();
        return delegate.toArray(generator);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        if (delegate.removeIf(filter)) {
            onWrite();
            return true;
        }
        return false;
    }

    @Override
    public Stream<E> stream() {
        onRead();
        return delegate.stream();
    }

    @Override
    public Stream<E> parallelStream() {
        onRead();
        return delegate.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        onRead();
        delegate.forEach(action);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    // paraméter típus lehet collection is
    public void setAll(Set<? extends E> set) {
        removeIf(not(set::contains));
        addAll(set.stream().filter(not(this::contains)).toList());
    }
}
