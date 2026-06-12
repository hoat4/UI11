package ui11.control.defaultlook;

import ui11.Widget;
import ui11.color.RGBColor;
import ui11.control.Button.ButtonState;
import ui11.decoration.Box;
import ui11.color.Color;
import ui11.layout.singlechild.Padding;

import static ui11.geom.Length.em;

public final class DefaultButtonLook extends Widget {

    private final ButtonState buttonState;

    public DefaultButtonLook(ButtonState buttonState) {
        this.buttonState = buttonState;
    }

    @Override
    protected Widget build() {
        RGBColor color = Color.GREEN;
        RGBColor color1 = buttonState.pressed()
                ? color.toSRGB().multiply(0.7, 0.7, 0.7, 1)
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
