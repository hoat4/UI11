package ui11.layout.impl;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Size;
import ui11.graphics.effect.Overlay;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;

public final class DefaultOverlayLayoutImpl extends Widget {

    private final Overlay overlay;
    private final BoxLayoutResult.SizeRequest sizeRequest;

    public DefaultOverlayLayoutImpl(Overlay overlay, BoxLayoutResult.SizeRequest sizeRequest) {
        this.overlay = overlay;
        this.sizeRequest = sizeRequest;
    }

    @Override
    protected Widget build() {
        BoxConstraints constraints = sizeRequest.constraints();
        // TODO ha csak Gone van az Overlayben, akkor mi a teendő?
        BoxLayoutResult.SizeRequest req = new BoxLayoutResult.SizeRequest(constraints);
        // TODO reuse?
        return PeerRequest.requestOnMultipleWidgets(overlay.items(), req, results -> {
            Size s = results.stream().
                    filter(result -> switch (result) {
                        case BoxLayoutResult.OfChosenSize _ -> true;
                        case BoxLayoutResult.OfGone _ -> false;
                    }).
                    map(r -> ((BoxLayoutResult.OfChosenSize) r).size()).
                    reduce(Size::max).
                    orElse(constraints.min());

            if (!constraints.isSatisfiedBy(s))
                throw new RuntimeException(constraints + " is not satisfied by " + s + " (returned by " + this + ")");

            return sizeRequest.createResponse(new BoxLayoutResult.OfChosenSize(s));
        });
    }
}
