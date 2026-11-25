package ui11.layout;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.layout.multichild.LinearLayout.*;

public class LLWeightTest {

    void main() {
        ColorFill red = new ColorFill(Color.RED);
        ColorFill green = new ColorFill(Color.GREEN);
        ColorFill blue = new ColorFill(Color.BLUE);
        ColorFill cyan = new ColorFill(Color.CYAN);
        ColorFill magenta = new ColorFill(Color.MAGENTA);
        ColorFill yellow = new ColorFill(Color.YELLOW);

        Window.open(column(
                row(red, green, blue),
                row(magenta, expanded(yellow), cyan),
                row(red, withWeight(1, green), blue),
                row(withWeight(1, magenta), withWeight(1, yellow), withWeight(1, cyan)),
                row(withWeight(1, red), withWeight(1, green), withWeight(2, blue)),

                // teszteljük hogy beljebbi withWeight nem írja-e felül a korábbit
                // (azaz Element.findInUpValueList-ben put helyett putIfAbsent van)
                row(withWeight(3, withWeight(1, magenta)), withWeight(1, yellow), withWeight(2, cyan)),
                row(withWeight(1, withWeight(3, red)), withWeight(1, green), withWeight(2, blue))
        ));
    }
}
