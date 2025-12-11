package ui11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

// TODO dokumentálni kéne valahol publikus API-ban, hogy identity equals
abstract class ListenerProxyBase2<L> {

    private static final Logger logger = LoggerFactory.getLogger(ListenerProxyBase2.class);

    Widget widget;
    L listener;

    public ListenerProxyBase2(Widget widget, L listener) {
        Objects.requireNonNull(widget);
        Objects.requireNonNull(listener);
        this.widget = widget;
        this.listener = listener;
    }

    protected L currentListener() {
        if (widget.stateHolderOrNull() == null)
            throw new RuntimeException("listener proxy not valid: " + this);
        else
            return listener;
    }

    @Override
    public String toString() {
        String s = listenerType().getSimpleName() + " Proxy " + Integer.toHexString(System.identityHashCode(this));
        if (widget.stateHolderOrNull() == null)
            s += " (not valid)";
        else
            s += ": " + listener;
        return s;
    }

    protected abstract Class<L> listenerType();

    void propagateChangeToOldWidget(Widget oldWidget, Widget newWidget, Object newValue) {
        Objects.requireNonNull(oldWidget);
        Objects.requireNonNull(newWidget);
        Objects.requireNonNull(newValue);

        if (this.widget != oldWidget)
            throw new IllegalStateException("different widget for " + this + " (propagateChangeToOld)\n" +
                    "Expected: " + this.widget + "\n" +
                    "Old:      " + oldWidget + "\n" + "New:      " + newWidget + "\n" +
                    "Current listener: " + this.listener + "\nNew listener: " + newValue);

        this.listener = listenerType().cast(newValue);
    }

    void repurposeForNewWidget(Widget oldWidget, Widget newWidget, Object newValue) {
        Objects.requireNonNull(oldWidget);
        Objects.requireNonNull(newWidget);
        Objects.requireNonNull(newValue);

        if (this.widget != oldWidget)
            throw new IllegalStateException("different widget for " + this + " (repurposeForNew)\n" +
                    "Expected: " + this.widget + "\n" +
                    "Old:      " + oldWidget + "\n" + "New:      " + newWidget + "\n" +
                    "Current listener: " + this.listener + "\nNew listener: " + newValue);

        this.widget = newWidget;
        this.listener = listenerType().cast(newValue);
    }
}

final class RunnableProxy extends ListenerProxyBase2<Runnable> implements Runnable {

    public RunnableProxy(Widget widget, Runnable listener) {
        super(widget, listener);
    }

    @Override
    protected Class<Runnable> listenerType() {
        return Runnable.class;
    }

    @Override
    public void run() {
        currentListener().run();
    }
}

@SuppressWarnings("rawtypes")
final class ConsumerProxy<T> extends ListenerProxyBase2<Consumer> implements Consumer<T> {

    // ez a komment már nem érvényes (2025-12-06):
    // nem lehet Consumer<T>, mert nem tudjuk, hogy a régi RSW-ben lévő Consumernek ugyanaz volt-e a typevarja,
    // mint az újban lévőnek

    public ConsumerProxy(Widget widget, Consumer listener) {
        super(widget, listener);
    }

    @Override
    protected Class<Consumer> listenerType() {
        return Consumer.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void accept(T o) {
        // reménykedünk, hogy ugyanaz a típus. ha mégsem, akkor az acceptje CCE-t fog dobni.
        currentListener().accept(o);
    }
}
