package ui11.graphics;

import ui11.PeerRequest;
import ui11.geom.Location;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Rect;
import ui11.geom.Size;

public abstract class VisualContentRequest<P> extends PeerRequest<P> {

    public final Surface surface;

    protected VisualContentRequest(Class<P> peerType, Surface surface) {
        super(peerType);
        this.surface = surface;
    }
}
