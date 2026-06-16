package ui11.layout;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.layout.singlechild.Padding;
import ui11.window.Window;

import static ui11.decoration.Background.withBackground;
import static ui11.geom.Length.px;

public class SimplePaddingTest {
    public void main() {
        Window.open(withBackground(Color.RED,
                Padding.allSides(px(100),
                        new ColorFill(Color.GREEN)
                )
        ));
    }
}
