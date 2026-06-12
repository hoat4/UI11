package ui11;

import org.jspecify.annotations.NonNull;
import ui11.reflectutil.ReflectionUtil;

import java.util.Objects;

final class KeyWrapper extends Widget /*ProxyWidget*/ {

    final @NonNull Slot slot;
    final @NonNull Widget content;

    KeyWrapper(@NonNull Slot slot, @NonNull Widget content) {
        this.slot = Objects.requireNonNull(slot);
        this.content = Objects.requireNonNull(content);
    }

    @Override
    public String toString() {
        // TODO Slot.toString nem okozhat rekurziót?
        return "KeyWrapper[slot="+slot+", content=" + content + "]";
    }

    @Override
    protected Widget build() {
        throw new RuntimeException("should not reach here (KW b)");
    }
}