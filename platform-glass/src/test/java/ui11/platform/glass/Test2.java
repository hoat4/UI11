package ui11.platform.glass;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.layout.singlechild.Align;
import ui11.window.Window;

import static ui11.geom.Length.px;
import static ui11.graphics.effect.Overlay.overlay;
import static ui11.layout.multichild.LinearLayout.row;
import static ui11.layout.singlechild.FixedSize.withSize;

public class Test2 {
    public static void main(String[] args) {
        //Desktop.getDesktop().openWindow(new ColorFill(Color.LIGHTCORAL));
        /*
        Desktop.getDesktop().openWindow(new Group(List.of(
                new ColorFill(Color.LIGHTCORAL),
                new Transform(new ColorFill(Color.LIGHTBLUE), AffineTransformation.ofTranslation(new Vec2(50, 50)))
        )));
         */

        Window.open(
                overlay(
                        row(
                                new ColorFill(Color.LIGHTCORAL),
                                new ColorFill(Color.LIGHTBLUE)
                        ),
                        Align.center(
                                withSize(
                                        px(100), px(100),
                                        new ColorFill(Color.YELLOW)
                                )
                        )
                )
        );

        /*Desktop.getDesktop().openWindow(
                overlay(
                        new ColorFill(Color.LIGHTCORAL),
                        Padding.allSides(px(30), new ColorFill(Color.LIGHTGREEN))
                )
        );*/
    }

}
