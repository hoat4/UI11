package ui11.layout.impl;

import ui11.Widget;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.resolution.PeerCreationRequest;

public class PreferredSizeIsMinimal extends Widget {

    private final Widget content;

    @Inject private PeerCreationRequest<?> peerCreationRequest;

    public PreferredSizeIsMinimal(Widget content) {
        this.content = content;
    }

    @Override
    protected Widget build() {
        BoxConstraints constraints =
                peerCreationRequest instanceof BoxLayoutResult.BoxConstraintsPeerCreationRequest req ?
                        req.constraints() : null;
        if (constraints == null)
            return peerCreationRequest instanceof BoxLayoutResult.BoxConstraintsPeerCreationRequest ?
                    new BoxLayoutResult.OfNoConstraints() :
                    content;
        else
            return new BoxLayoutResult.OfChosenSize(constraints.min());
    }
}
