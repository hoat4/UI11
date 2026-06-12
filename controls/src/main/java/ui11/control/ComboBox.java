package ui11.control;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.observable.MutableObservable;
import ui11.text.Text;

import org.jspecify.annotations.NonNull;
import java.util.*;
import java.util.function.Function;

public final class ComboBox<T> extends SubstitutedWidget {

    private final ComboBoxModel<T> model;
    private final Function<T, String> displayNames;

    public ComboBox(ComboBoxModel<T> model,
                    Function<T, String> displayNames) {
        this.model = model;
        this.displayNames = displayNames;
    }

    public ComboBoxModel<T> model() {
        return model;
    }

    public Function<T, String> displayNames() {
        return displayNames;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new Text(String.valueOf(model.selectedValue.get()));
    }

    public static class ComboBoxModel<T> {

        public final Collection<T> possibleValues;
        public final MutableObservable<T> selectedValue;

        public ComboBoxModel(T initialValue, boolean nullable, Collection<T> possibleValues) {
            if (nullable) {
                List<T> l = new ArrayList<>();
                l.add(null);
                l.addAll(possibleValues);
                this.possibleValues = Collections.unmodifiableList(l);
            } else
                this.possibleValues = List.copyOf(possibleValues);
            if (!this.possibleValues.contains(initialValue))
                throw new IllegalArgumentException("invalid initial value: " + initialValue);

            selectedValue = MutableObservable.withInitial(initialValue, value -> {
                if (!possibleValues.contains(value))
                    throw new RuntimeException("invalid value: " + value);
            });
        }

        // azért nem varargs, mert úgy elfelejthetjük hogy meg kell adni. esetleg lehetne olyan is, hogy egy fix arg és többi varargs.
        public ComboBoxModel(T value, boolean nullable, T[] possibleValues) {
            this(value, nullable, List.of(possibleValues));
        }
    }
}
