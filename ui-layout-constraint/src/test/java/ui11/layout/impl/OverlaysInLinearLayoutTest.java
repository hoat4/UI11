package ui11.layout.impl;

import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;

import static ui11.graphics.effect.Overlay.overlay;
import static ui11.layout.multichild.LinearLayout.row;

/**
 * Overlay-re resolveAdditional által rakott wrappert teszteli
 */
public class OverlaysInLinearLayoutTest {

    public void main() {
        ui11.window.Desktop.getDesktop().openWindow(row(
                overlay(
                        new ColorFill(Color.RED),
                        new ColorFill(Color.GREEN)
                ),
                new ColorFill(Color.BLUE)
        ));
    }
}
