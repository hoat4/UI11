package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.CheckBox;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.input.gesture.ClickListener;
import ui11.input.pointer.PointerStateDependent;
import ui11.text.Text;

import static ui11.decoration.Background.withBackground;

public final class DefaultCheckBoxImpl extends Widget {

    private final CheckBox checkBox;

    public DefaultCheckBoxImpl(CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    @Override
    protected Widget build() {
        // TODO graphicot is mutassuk
        return new ClickListener(
                withBackground(
                        new PointerStateDependent(
                                new ColorFill(Color.TRANSPARENT),
                                new ColorFill(Color.BLACK.withAlpha(0.1)),
                                new ColorFill(Color.BLACK.withAlpha(0.2))
                        ),
                        checkBox.value().get() ? new Text("be") : new Text("ki")
                ),
                () -> {
                    checkBox.value().set(!checkBox.value().get());
                });
    }
}