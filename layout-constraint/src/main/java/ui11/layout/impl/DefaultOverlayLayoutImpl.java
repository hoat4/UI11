package ui11.layout.impl;

import ui11.Slot;
import ui11.Widget;
import ui11.geom.Size;
import ui11.graphics.effect.Overlay;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;

public final class DefaultOverlayLayoutImpl extends Widget {

    private final Overlay overlay;
    private final Widget peer;

    @Inject(required = false) private BoxLayoutResult.SizeRequest sizeRequest;
    @Inject private Slot peerSlot;

    public DefaultOverlayLayoutImpl(Overlay overlay, Widget peer) {
        this.overlay = overlay;
        this.peer = peer;
    }

    @Override
    protected Widget build() {
        Widget peerWithSlot = peer.withSlot(peerSlot);
        if (sizeRequest == null)
            return peerWithSlot;

        BoxConstraints constraints = sizeRequest.constraints();
        if (constraints == null)
            // TODO ha csak Gone van az Overlayben, akkor mi a teendő?
            return new BoxLayoutResult.OfNoConstraints(peerWithSlot);
        BoxLayoutResult.SizeRequest req = new BoxLayoutResult.SizeRequest(constraints);
        return req.executedOn(overlay.items(), peers -> {
            Size s = peers.stream().
                    filter(boxLayoutResult -> switch (boxLayoutResult) {
                        case BoxLayoutResult.OfChosenSize _ -> true;
                        case BoxLayoutResult.OfGone _ -> false;
                        case BoxLayoutResult.OfNoConstraints _ -> {
                            throw new RuntimeException("unexpected " +
                                    BoxLayoutResult.class.getSimpleName() + ": " + boxLayoutResult);
                        }
                    }).
                    map(r -> ((BoxLayoutResult.OfChosenSize) r).size()).
                    reduce(Size::max).
                    orElse(constraints.min());

            if (!constraints.isSatisfiedBy(s))
                throw new RuntimeException(constraints + " is not satisfied by " + s + " (returned by " + this + ")");

            return new BoxLayoutResult.OfChosenSize(s, peerWithSlot);
        });
    }
}
