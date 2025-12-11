package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.Button.ButtonState;
import ui11.decoration.Box;
import ui11.graphics.fill.Color;
import ui11.layout.singlechild.Padding;

import static ui11.geom.Length.em;

public final class DefaultButtonLook extends Widget {

    private final ButtonState buttonState;

    public DefaultButtonLook(ButtonState buttonState) {
        this.buttonState = buttonState;
    }

    @Override
    protected Widget build() {
        Color color = Color.GREEN;
        Color color1 = buttonState.pressed()
                ? new Color(color.red() * 0.7, color.green() * 0.7, color.blue() * 0.7)
                : color;
        if (!buttonState.button().enabled())
            color1 = color1.lighter();
        return new Box(Padding.allSides(em(1), buttonState.button().content())).withBackground(color1);
    }

    @Override
    public String toString() {
        return "DefaultButtonLook[" +
                "buttonState=" + buttonState + ']';
    }
}
