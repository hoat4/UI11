package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.ComboBox;
import ui11.text.Text;

public final class DefaultComboBoxLook<T> extends Widget {

    private final ComboBox<T> comboBox;

    public DefaultComboBoxLook(ComboBox<T> comboBox) {
        this.comboBox = comboBox;
    }

    @Override
    protected Widget build() {
        return new Text(String.valueOf(comboBox.displayNames().apply(comboBox.model().selectedValue.get())));
    }
}
