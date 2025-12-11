package ui11.control;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.observable.MutableObservable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class CheckBox extends SubstitutedWidget {

    @Nonnull private final MutableObservable<Boolean> value;
    @Nullable private final Widget graphic;
    private final boolean disabled;

    public CheckBox(@Nonnull MutableObservable<Boolean> value, @Nullable Widget graphic, boolean disabled) {
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

    @Nonnull
    public MutableObservable<Boolean> value() {
        return value;
    }

    @Nullable
    public Widget graphic() {
        return graphic;
    }

    public boolean disabled() {
        return disabled;
    }
}
