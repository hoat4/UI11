package ui11.platform.glass;

import ui11.Widget;
import ui11.animation.Scheduler;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.layout.singlechild.Align;
import ui11.window.Window;

import static ui11.geom.Length.px;
import static ui11.graphics.effect.Overlay.overlay;
import static ui11.layout.singlechild.FixedSize.withWidth;

public class AnimationTest extends Widget {

    @Inject
    private Scheduler scheduler;

    @Override
    protected Widget build() {
        scheduler.requestAnimationFrame();
        double pos = System.nanoTime() / 10000000.0 % 400;
        //System.out.println(pos);
        return overlay(
                new ColorFill(Color.WHITE),
                Align.left(withWidth(px(pos), new ColorFill(Color.GREEN))),
                Align.right(withWidth(px(50), new ColorFill(Color.BLUE)))
        );
    }

    public static void main(String[] args) {
        Window.open(new AnimationTest());
    }
}
