package ui11.platform.awt.j2d;

import ui11.PeerCreationRequest;

public final class J2DPeerCreationRequest extends PeerCreationRequest<J2DNodeHolder> {

    public J2DPeerCreationRequest() {
        super(J2DNodeHolder.class);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof J2DPeerCreationRequest;
    }

    @Override
    public int hashCode() {
        return 403000985;
    }
}
