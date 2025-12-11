package ui11.control;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.observable.MutableObservable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class RadioButton<T> extends SubstitutedWidget {

    @Nonnull private final MutableObservable<T> prop;
    private final T value;
    @Nullable private final Widget graphic;
    private final boolean disabled;

    // TODO disabled lehetne inherited

    public RadioButton(@Nonnull MutableObservable<T> prop, T value, @Nullable Widget graphic, boolean disabled) {
        Objects.requireNonNull(prop);
        this.prop = prop;
        this.value = value;
        this.graphic = graphic;
        this.disabled = disabled;
    }

    public RadioButton(MutableObservable<T> prop, T value, @Nullable Widget graphic) {
        this(prop, value, graphic, false);
    }

    @Nonnull
    public MutableObservable<T> prop() {
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
