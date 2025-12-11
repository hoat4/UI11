package ui11.observable;

import java.util.*;

public class ObservableMap<K, V> extends ObservableBase implements Map<K, V> {

    private final Map<K, V> delegate;

    // onWriteot az írás után hívjuk meg, hogy már az új értéket lássák

    public ObservableMap() {
        this(new HashMap<>());
    }

    private ObservableMap(Map<K, V> delegate) {
        this.delegate = delegate;
    }

    public static <K, V> ObservableMap<K, V> wrap(Map<K, V> backingMap) {
        return new ObservableMap<>(backingMap);
    }

    public static <K, V> ObservableMap<K, V> withInitial(Map<K, V> initialContents) {
        return wrap(new HashMap<>(initialContents));
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
    public boolean containsKey(Object key) {
        onRead();
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        onRead();
        return delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        onRead();
        return delegate.get(key);
    }

    @Override
    public V put(K key, V value) {
        onRead();
        V prevVal = delegate.put(key, value);
        onWrite();
        return prevVal;
    }

    @Override
    public V remove(Object key) {
        onRead();
        V removed = delegate.remove(key);
        onWrite();
        return removed;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        delegate.putAll(m);
        onWrite();
    }

    @Override
    public void clear() {
        delegate.clear();
        onWrite();
    }

    @Override
    public Set<K> keySet() {
        onRead();
        return delegate.keySet(); // TODO remove
    }

    @Override
    public Collection<V> values() {
        onRead();
        return delegate.values(); // TODO remove
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        onRead();
        return delegate.entrySet(); // TODO remove
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public boolean equals(Object o) {
        onRead();
        if (this == o) return true;
        if (o == null || !(o instanceof Map<?, ?> oMap)) return false;
        return entrySet().equals(oMap.entrySet());
    }

    @Override
    public int hashCode() {
        onRead();
        return delegate.hashCode();
    }
}
