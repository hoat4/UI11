package ui11.control;

import ui11.SubstitutedWidget;
import ui11.observable.MutableObservable;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class Slider extends SubstitutedWidget {

    private final @NonNull MutableObservable<Double> value;

    public Slider(@NonNull MutableObservable<Double> value) {
        this.value = Objects.requireNonNull(value);
    }

    public @NonNull MutableObservable<Double> value() {
        return value;
    }
}
