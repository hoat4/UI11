package ui11.observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public sealed class EventSource<E> permits EventBus, DerivedValue.ChangeEventEventSource {

    private static final Logger logger = LoggerFactory.getLogger(EventSource.class);

    private final List<ListenerRegistration> listeners = new ArrayList<>();

    EventSource() {
    }

    void post(E e) {
        ObserverHolder.withoutObserver(() -> listeners.forEach(l -> {
            try {
                l.consumer.accept(e);
            } catch (RuntimeException | AssertionError ex) {
                logger.error("Listener " + l.consumer + " failed with exception", ex);
            }
        }));
    }

    public void subscribe(Scope scope, Consumer<E> consumer) {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(consumer, "consumer");

        boolean first = listeners.isEmpty();
        ListenerRegistration r = new ListenerRegistration(consumer);
        listeners.add(r);
        scope.onClose(r::cancel);
        if (first)
            onFirstSubscriber();
    }

    void onFirstSubscriber() {
    }

    void onLastSubscriber() {
    }

    private final class ListenerRegistration {

        private final Consumer<E> consumer;

        private ListenerRegistration(Consumer<E> consumer) {
            this.consumer = consumer;
        }

        public void cancel() {
            if (!listeners.remove(this))
                throw new IllegalStateException("already canceled");
            if (listeners.isEmpty())
                onLastSubscriber();
        }
    }
}
