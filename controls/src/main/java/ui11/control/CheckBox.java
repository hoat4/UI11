package ui11.control;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.observable.MutableObservable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.Objects;

public final class CheckBox extends SubstitutedWidget {

    private final @NonNull MutableObservable<Boolean> value;
    private final @Nullable Widget graphic;
    private final boolean disabled;

    public CheckBox(@NonNull MutableObservable<Boolean> value, @Nullable Widget graphic, boolean disabled) {
        this.value = Objects.requireNonNull(value);
        this.graphic = graphic;
        this.disabled = disabled;
    }

    public CheckBox(MutableObservable<Boolean> value) {
        this(value, null);
    }

    public CheckBox(MutableObservable<Boolean> value, @Nullable Widget graphic) {
        this(value, graphic, false);
    }

    public @NonNull MutableObservable<Boolean> value() {
        return value;
    }

    public @Nullable Widget graphic() {
        return graphic;
    }

    public boolean disabled() {
        return disabled;
    }
}
