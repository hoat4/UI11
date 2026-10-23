package ui11.output.html.util;

import java.util.ArrayDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObjPool<T> {

    private final ArrayDeque<T> deque;

    private final Supplier<T> supplier;
    private final Consumer<T> reset;

    public ObjPool(int size, Supplier<T> supplier) {
        this(size, supplier, obj -> {
        });
    }

    public ObjPool(int size, Supplier<T> supplier, Consumer<T> reset) {
        deque = new ArrayDeque<>(size);
        this.supplier = supplier;
        this.reset = reset;
    }

    public T acquire() {
        T object;
        synchronized (deque) {
            object = deque.poll();
        }
        if (object == null)
            object = supplier.get();
        return object;
    }

    public void release(T object) {
        reset.accept(object);
        synchronized (deque) {
            deque.offerFirst(object);
        }
    }
}
