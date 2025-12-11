package ui11.provide;

import ui11.Widget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

// TODO név
// TODO dokumentáljuk, hogy next egy új Elementbe kerül, ezért slotba wrappelni kell
public final class UpValueWrapper extends Widget /*ProxyWidget*/ {

    @Nonnull private final UpValue value;
    @Nullable private final Widget next;

    public UpValueWrapper(@Nonnull UpValue value) {
        this(value, null);
    }

    public UpValueWrapper(@Nonnull UpValue value, @Nullable Widget next) {
        Objects.requireNonNull(value);
        this.value = value;
        this.next = next;
    }

    @Nonnull
    public UpValue value() {
        return value;
    }

    @Nullable
    public Widget next() {
        return next;
    }

    @Override
    protected Widget build() {
        return next; // lásd ChainSegmentBuilder.build-ben komment
    }

    @Override
    public String toString() {
        return "UpValueWrapper{" +
                "value=" + value +
                ", next=" + next +
                '}';
    }
}
