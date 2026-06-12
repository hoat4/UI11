package ui11.control;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.layout.multichild.LinearLayout;
import ui11.control.ComboBox.ComboBoxModel;

import org.jspecify.annotations.NonNull;

public final class ButtonBar<T> extends SubstitutedWidget {

    private final ComboBoxModel<T> model;

    public ButtonBar(ComboBoxModel<T> model) {
        this.model = model;
    }

    public ComboBoxModel<T> value() {
        return model;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return model.possibleValues.stream().
                map(t -> new Button(String.valueOf(t), () -> model.selectedValue.set(t))).
                collect(LinearLayout.toRow());
    }
}
