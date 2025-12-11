package ui11.observable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObservableList<E> extends ObservableBase implements List<E> {

    private final List<E> delegate;

    public ObservableList() {
        this(new ArrayList<>());
    }

    private ObservableList(List<E> delegate) {
        this.delegate = delegate;
    }

    public static <E> ObservableList<E> wrap(List<E> elements) {
        return new ObservableList<>(elements);
    }

    public static <E> ObservableList<E> withInitial(Collection<E> elements) {
        return wrap(new ArrayList<>(elements));
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
    public boolean addAll(int index, Collection<? extends E> c) {
        if (delegate.addAll(index, c)) {
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
    public void replaceAll(UnaryOperator<E> operator) {
        delegate.replaceAll(operator);
        onWrite();
    }

    @Override
    public void sort(Comparator<? super E> c) {
        delegate.sort(c);
        onWrite();
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
    public E get(int index) {
        onRead();
        return delegate.get(index);
    }

    @Override
    public E set(int index, E element) {
        final E prev = delegate.set(index, element);
        if (!Objects.equals(prev, element))
            onWrite();
        return prev;
    }

    @Override
    public void add(int index, E element) {
        onWrite();
        delegate.add(index, element);
    }

    @Override
    public E remove(int index) {
        onWrite();
        return delegate.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        onRead();
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        onRead();
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return new SublistImpl(fromIndex, toIndex);
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

    public boolean setAll(List<? extends E> l) {
        if (this.equals(l))
            return false;
        delegate.clear();
        delegate.addAll(l);
        onWrite();
        return true;
    }

    @SuppressWarnings("unchecked")
    public List<E> snoop() {
        return (List<E>) Collections.unmodifiableList(Arrays.asList(delegate.toArray()));
    }

    private class SublistImpl extends AbstractList<E> {

        private final int from, to;

        public SublistImpl(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            onWrite();
            delegate.subList(fromIndex, toIndex).clear();
        }

        @Override
        public E get(int index) {
            if (index >=size()||index<0)
                throw new IndexOutOfBoundsException();
            return ObservableList.this.get(from+index);
        }

        @Override
        public int size() {
            return to-from;
        }

        @Override
        public E set(int index, E element) {
            throw new RuntimeException("TODO");
        }

        @Override
        public void add(int i, E e) {
            throw new RuntimeException("TODO");
        }
    }
}
