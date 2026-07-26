package ui11.layout.impl;

import ui11.Slot;
import ui11.Widget;
import ui11.geom.Size;
import ui11.graphics.effect.Overlay;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.text.Text;

public final class DefaultOverlayLayoutImpl extends Widget {

    private final Overlay overlay;
    private final BoxLayoutResult.SizeRequest sizeRequest;
    private final Widget peer;

    public DefaultOverlayLayoutImpl(Overlay overlay, BoxLayoutResult.SizeRequest sizeRequest, Widget peer) {
        this.overlay = overlay;
        this.sizeRequest = sizeRequest;
        this.peer = peer;
    }

    @Override
    protected Widget build() {
        BoxConstraints constraints = sizeRequest.constraints();
        // TODO ha csak Gone van az Overlayben, akkor mi a teendő?
        BoxLayoutResult.SizeRequest req = new BoxLayoutResult.SizeRequest(constraints);
        // TODO slotok? reuse?
        return req.executedOn(overlay.items(), results -> {
            Size s = results.stream().
                    filter(result -> switch (result.peer()) {
                        case BoxLayoutResult.OfChosenSize _ -> true;
                        case BoxLayoutResult.OfGone _ -> false;
                    }).
                    map(r -> ((BoxLayoutResult.OfChosenSize) r.peer()).size()).
                    reduce(Size::max).
                    orElse(constraints.min());

            if (!constraints.isSatisfiedBy(s))
                throw new RuntimeException(constraints + " is not satisfied by " + s + " (returned by " + this + ")");

            return new BoxLayoutResult.OfChosenSize(s, peer);
        });
    }
}
