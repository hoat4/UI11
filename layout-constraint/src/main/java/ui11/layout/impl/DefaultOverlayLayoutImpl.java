package ui11.layout.impl;

import ui11.MultiSlot;
import ui11.Slot;
import ui11.Widget;
import ui11.geom.Size;
import ui11.graphics.effect.Overlay;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.resolution.PeerCreationRequest;

public final class DefaultOverlayLayoutImpl extends Widget {

    private final Overlay overlay;
    private final Widget peer;

    @Inject private PeerCreationRequest<?> peerCreationRequest;
    @Inject private Slot peerSlot;
    @Inject private MultiSlot<Integer> childrenSlots;

    public DefaultOverlayLayoutImpl(Overlay overlay, Widget peer) {
        this.overlay = overlay;
        this.peer = peer;
    }

    @Override
    protected Widget build() {
        BoxConstraints constraints =
                peerCreationRequest instanceof BoxLayoutResult.BoxConstraintsPeerCreationRequest req ?
                        req.constraints() : null;
        if (constraints == null)
            return peerCreationRequest instanceof BoxLayoutResult.BoxConstraintsPeerCreationRequest ?
                    new BoxLayoutResult.OfNoConstraints() :
                    peer.withSlot(peerSlot);

        // constraintset nem kell megadni Providerben, mert már amúgyis inherited value
        Size s = useWidgets(childrenSlots, overlay.items(),
                new BoxLayoutResult.BoxConstraintsPeerCreationRequest(constraints)).
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

        return new BoxLayoutResult.OfChosenSize(s);
    }
}
