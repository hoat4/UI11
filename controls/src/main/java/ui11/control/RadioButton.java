package ui11.control;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.observable.MutableObservable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.Objects;

public final class RadioButton<T> extends SubstitutedWidget {

    private final @NonNull MutableObservable<T> prop;
    private final T value;
    private final @Nullable Widget graphic;
    private final boolean disabled;

    // TODO disabled lehetne inherited

    public RadioButton(@NonNull MutableObservable<T> prop, T value, @Nullable Widget graphic, boolean disabled) {
        Objects.requireNonNull(prop);
        this.prop = prop;
        this.value = value;
        this.graphic = graphic;
        this.disabled = disabled;
    }

    public RadioButton(MutableObservable<T> prop, T value, @Nullable Widget graphic) {
        this(prop, value, graphic, false);
    }

    public @NonNull MutableObservable<T> prop() {
        return prop;
    }

    public T value() {
        return value;
    }

    @Nullable
    public Widget graphic() {
        return graphic;
    }

    public boolean disabled() {
        return disabled;
    }

    @Override
    public String toString() {
        return "RadioButton[" +
                "prop=" + prop + ", " +
                "value=" + value + ", " +
                "graphic=" + graphic + ", " +
                "disabled=" + disabled + ']';
    }
}
