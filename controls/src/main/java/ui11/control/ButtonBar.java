package ui11.control;

import ui11.SubstitutedWidget;
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
        // TODO ide lehet hogy kellenének slotok, mert possibleValues sorrendje megváltozhat.
        //      csak akkor meg vavlamit kéne csinálni azzal, ha duplán vannak az elemek benne
        return model.possibleValues.stream().
                map(t -> new Button(String.valueOf(t), () -> model.selectedValue.set(t))).
                collect(LinearLayout.toRow());
    }
}
