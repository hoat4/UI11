package ui11;

import java.util.Objects;

// TODO duplicate-ek detektálása

/**
 * A collections of {@link Slot Slots} that are cached by a key.
 * <p>
 * This is usually used when a widget needs to show a list of items: each row gets a different slot, so when some row
 * changes, only the changed row refreshed, not all items.
 *
 * @param <K> the type of the cache keys
 */
public final class MultiSlot<K> {

    private final Slot base;

    MultiSlot(Slot base) {
        Objects.requireNonNull(base);
        this.base = base;
    }

    // item nullable legyen?
    public Slot of(K item) {
        Objects.requireNonNull(item);
        return new Slot(base.slotContainerWidget, new MultiKeyItem(base.key, item));
    }

    public KeyWrapper use(K item, Widget widget) {
        // TODO ha widgetben már van KeyWrapper, akkor nem csak térjen vissza vele.
        //      mondjuk ha a KeyWrapper előtt van egy Provider, akkor bonyolultabb a helyzet.
        return of(item).use(widget);
    }

    public WidgetInstantiation instantiate(K item, Widget widget) {
        Objects.requireNonNull(item);
        return of(item).instantiate(widget);
    }

    private record MultiKeyItem(Object baseIdentifier, Object item) {}
}
