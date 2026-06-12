package ui11;

import ui11.color.Color;
import ui11.geom.Path;
import ui11.geom.Rect;
import ui11.graphics.effect.Clip;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.shaper.PathShaped;
import ui11.window.Window;

import static ui11.graphics.effect.Overlay.overlay;

// sikeres, ha az első sorban a második és a harmadik téglalap tetején a félig átlátszó sor ugyanolyan színű
public class ClipFillInsideShapedTest {
    public void main() {
        Window.open(overlay(
                new PathShaped(
                        overlay(
                                new ColorFill(Color.GREEN),
                                // ez hogy két ColorFill van, csak ezért kell hogy ne olvassza össze
                                // a 3.-nál a Clipet és a ColorFillt
                                new ColorFill(Color.GREEN.withAlpha(1 / 255.0))
                        ),
                        Path.ofRect(Rect.ofTopRightBottomLeft(50, 100, 100, 50))
                ),

                // egyszerű FillPathRenderNode, hogy megnézzük hogy az ablak insetek jól vannak-e beállítva
                // (ha tört számok, akkor elmosódott lesz a téglalap)
                new PathShaped(
                        new ColorFill(Color.GREEN),
                        Path.ofRect(Rect.ofTopRightBottomLeft(150, 100, 200, 50))
                ),

                new PathShaped(
                        overlay(
                                new ColorFill(Color.GREEN),
                                new ColorFill(Color.GREEN.withAlpha(1 / 255.0))
                        ),
                        Path.ofRect(Rect.ofTopRightBottomLeft(50.5, 200, 100, 150))
                ),
                new PathShaped(
                        new Clip(
                                overlay(
                                        new ColorFill(Color.GREEN),
                                        new ColorFill(Color.GREEN.withAlpha(1 / 255.0))
                                )
                        ),
                        Path.ofRect(Rect.ofTopRightBottomLeft(50.5, 300, 100, 250))
                )
        ));
    }
}
