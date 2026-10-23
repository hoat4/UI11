package ui11.platform.dom.peers;

import ui11.Expose;
import ui11.Widget;
import ui11.platform.dom.DOMPeerBase;

import java.net.URI;

public class CSSImageWidget extends Widget {

    private final URI uri;

    @Inject private DOMPeerBase.CSSBackgroundImagePeerCreationRequest[] reqs;

    public CSSImageWidget(URI uri) {
        this.uri = uri;
    }

    @Override
    protected Widget build() {
        Widget w = null;
        for (DOMPeerBase.CSSBackgroundImagePeerCreationRequest req : reqs)
            w = new Expose<>(req, new DOMCoverPeer.CSSBackgroundImage(uri), w);
        return w;
    }
}
