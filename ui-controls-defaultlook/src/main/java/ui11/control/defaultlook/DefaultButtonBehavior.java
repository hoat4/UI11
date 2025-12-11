package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.Button;
import ui11.control.Button.ButtonState;
import ui11.input.gesture.ClickListener;
import ui11.input.pointer.PointerStateDependent;

public final class DefaultButtonBehavior extends Widget {

    private final Button button;

    public DefaultButtonBehavior(Button button) {
        this.button = button;
    }

    @Override
    protected Widget build() {
        return new ClickListener(
                new PointerStateDependent(
                        new ButtonState(button, false),
                        new ButtonState(button, false),
                        new ButtonState(button, true)
                ),
                () -> {
                    if (button.actionHandler() != null)
                        button.actionHandler().run();
                    // TODO kéne jelezni valamit, ha nincs engedélyezve?
                }
        );
    }

    public Button button() {
        return button;
    }
}
