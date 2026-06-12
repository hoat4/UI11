package ui11.control;

import ui11.observable.MutableObservable;
import ui11.observable.Scope;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class EditablePlainText implements MutableObservable<String> {

    private final MutableObservable<String> delegate;
    @Nullable public final Integer maxLength;

    public EditablePlainText() {
        delegate = MutableObservable.withInitial("");
        this.maxLength = null;
    }

    public EditablePlainText(String s) {
        this.delegate = MutableObservable.withInitial(s);
        this.maxLength = null;
    }

    public EditablePlainText(MutableObservable<String> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        this.maxLength = null;
    }

    /**
     * UTF-16 egységekben mérve
     */
    public EditablePlainText(int maxLength) {
        delegate = MutableObservable.withInitial("", value -> {
            if (value.length() > maxLength)
                throw new IllegalArgumentException("too length: " + value.length() + " chars instead " + maxLength);
        });
        this.maxLength = maxLength;
    }

    // TODO nem kéne 3-mat delegálni

    @Override
    public String get() {
        return delegate.get();
    }

    @Override
    public void set(String s) {
        delegate.set(s);
    }

    @Override
    public void bindTo(Supplier<String> valueSupplier, Scope scope) {
        delegate.bindTo(valueSupplier, scope);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        EditablePlainText that = (EditablePlainText) o;
        return delegate.equals(that.delegate) && Objects.equals(maxLength, that.maxLength);
    }

    @Override
    public int hashCode() {
        int result = delegate.hashCode();
        result = 31 * result + Objects.hashCode(maxLength);
        return result;
    }
}
