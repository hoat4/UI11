package ui11.control;

import ui11.SubstitutedWidget;
import ui11.observable.MutableObservable;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class Slider extends SubstitutedWidget {

    @Nonnull private final MutableObservable<Double> value;

    public Slider(@Nonnull MutableObservable<Double> value) {
        this.value = Objects.requireNonNull(value);
    }

    @Nonnull
    public MutableObservable<Double> value() {
        return value;
    }
}
