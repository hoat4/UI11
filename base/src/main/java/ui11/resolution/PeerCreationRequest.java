package ui11.resolution;

import ui11.EndingWidget;

public abstract class PeerCreationRequest<P extends EndingWidget> {

    private final Class<P> peerType;

    protected PeerCreationRequest(Class<P> peerType) {
        this.peerType = peerType;
    }

    public final Class<P> peerType() {
        return peerType;
    }
}
