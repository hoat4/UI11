package ui11.observable;

public final class EventBus<E> extends EventSource<E> {

    @Override
    public void post(E e) {
        super.post(e);
    }
}
