package ui11.control;

import ui11.observable.MutableObservable;
import ui11.observable.Scope;

import javax.annotation.Nullable;
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
        this.delegate = delegate;
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
}
