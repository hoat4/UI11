package ui11.graphics;

import ui11.ExposeRequest;

public abstract class VisualContentRequest<P> extends ExposeRequest<P> {

    public final Surface surface;

    protected VisualContentRequest(Class<P> peerType, Surface surface) {
        super(peerType);
        this.surface = surface;
    }
}
