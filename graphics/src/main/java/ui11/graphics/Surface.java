package ui11.graphics;

import org.jspecify.annotations.Nullable;
import ui11.geom.Location;
import ui11.geom.Size;
import ui11.geom.Shape;
import ui11.provide.FromPeerRequests;

// hasonló fogalom: https://www.chromium.org/developers/design-documents/chromium-graphics/surfaces/

/**
 * Information about a graphical output, e.g. a whole window, or an arbitrary shaped part of the screen.
 */
public interface Surface {

    Shape layoutShape();

    Shape clipShape();

    Shape inputShape();

    /**
     * The origo of this coordinate space is the top-left point of the {@link #layoutShape() layout shape's}
     * bounding box.
     */
    Location.CoordinateSpace coordinateSpace();

    /**
     * The returned value is in the widget's {@linkplain #coordinateSpace() own coordinate space}, so
     * the size before the ascendant's transformations.
     */
    default Size size() {
        return layoutShape().bounds(coordinateSpace()).size();
    }

    double devicePixelRatio();

    default boolean hasVisiblePart() {
        Shape shape = clipShape();
        return !Shape.degenerateShape().equals(shape) && shape.bounds(coordinateSpace()).size().isLargerThan(Size.ZERO);
    }

    @FromPeerRequests
    private static @Nullable Surface fromPeerRequests(VisualContentRequest<?>[] requests) {
        if (requests.length == 0)
            return null;
        if (requests.length > 1)
            // TODO lehetne valahogy mergeölni?
            throw new RuntimeException("multiple " + VisualContentRequest.class.getSimpleName() + "s");
        return requests[0].surface;
    }
}
