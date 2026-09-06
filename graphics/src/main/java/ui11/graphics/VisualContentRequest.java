package ui11.graphics;

import ui11.PeerRequest;
import ui11.geom.Location;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Rect;
import ui11.geom.Size;

// TODO ez az interface nem jó, mert nem lehet rá értelmes equals/hashCodeot definiálni

public abstract class VisualContentRequest<P> extends PeerRequest<P> {

    protected VisualContentRequest(Class<P> peerType) {
        super(peerType);
    }

    /**
     * The returned value is in the widget's {@linkplain #coordinateSpace() own coordinate space}, so
     * the size before the ascendant's transformations.
     */
    public abstract Size size();

    public abstract CoordinateSpace coordinateSpace();

    // TODO instead this, a shape getter would be better, but it's not clear what its type should be
    // also, different shapes can exist for a widget:
    // - hitbox
    // - visual bounds
    // - layout bounds
    public boolean hitTest(Location point) {
        // TODO ez csak téglalap alakú dolgoknál ad vissza helyes értéket
        return Rect.of(size()).contains(point.in(coordinateSpace()));
    }
}
