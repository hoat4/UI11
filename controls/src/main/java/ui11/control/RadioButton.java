package ui11.control;

import ui11.Slot2;
import ui11.SubstitutedWidget;
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

    @Remember private Slot2 graphicSlot;

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

    @Override
    protected void initState() {
        graphicSlot = new Slot2();
    }

    @Override
    protected RadioButton<T> forSubstitution() {
        return new RadioButton<>(prop, value, graphicSlot.with(graphic), disabled);
    }

    public @NonNull MutableObservable<T> prop() {
        return prop;
    }

    public T value() {
        return value;
    }

    public @Nullable Widget graphic() {
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
