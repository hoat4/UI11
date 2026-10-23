package ui11.layout.multichild;

import org.jspecify.annotations.NonNull;
import ui11.Widget;
import ui11.geom.Mat4;
import ui11.geom.Rect;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.graphics.shaper.RectangleShaped;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// ez most átkerült layout-model-be, mert nem lehet platform-dom-ot egyelőre layout-constraint-tel együtt
// használni, de úgy meg playerinfo4 RatingHistory nem fordul le.
// és végülis önmagában is értelmes, mint egy önálló layout widget, kb. mint JavaFX-ben AnchorPane.
// layout-constraint modult meg el lehetne nevezni layout-algo-vá.

// ebből majd lehetne egy publikus osztály.
// csak akkor lent a buildbe new FixedSize(Length.zero(), ...) kéne
// (most azért nem lehet, mert végtelen rekurziót eredményezne, mivel
// FixedSize is Box-szal van implementálva)
public final class Canvas {

    private final List<Widget> widgets = new ArrayList<>();

    // valszeg elavult komment:
    // TODO ezt ki kéne emelni közös osztályba, mert DefaultLinearLayoutImpl-nek kell
    public static @NonNull Transform transformWidgetToBounds(Widget childWithSlot, Rect bounds) {
        return new Transform(
                new RectangleShaped(
                        childWithSlot,
                        bounds.size()
                ),
                Mat4.ofTranslation(bounds.topLeft())
        );
    }

    public void add(Widget w, Rect bounds) {
        Objects.requireNonNull(w);
        Objects.requireNonNull(bounds);
        widgets.add(transformWidgetToBounds(w, bounds));
    }

    public Widget build() {
        return new Overlay(widgets);
    }
}
