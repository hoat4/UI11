package ui11.layout.impl;

import ui11.Slot;
import ui11.Widget;
import ui11.geom.Size;
import ui11.graphics.effect.Overlay;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutProtocol;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.provide.UpValueWrapper;

public final class DefaultOverlayLayoutImpl extends Widget implements BoxLayoutProtocol {

    private final Overlay overlay;
    private final Widget peer;

    @Inject(required = false) private Observable<BoxConstraints> constraints;
    @Inject private Slot peerSlot;

    @State private MutableObservable<Size> determinedSize; // TODO ezt nem kéne törölni valamikor?

    public DefaultOverlayLayoutImpl(Overlay overlay, Widget peer) {
        this.overlay = overlay;
        this.peer = peer;
    }

    @Override
    protected void initState() {
        determinedSize = MutableObservable.ofNullable();
    }

    @Override
    protected Widget build() {
        BoxConstraints constraints = this.constraints.get();
        if (constraints == null)
            return peerSlot.use(peer);

        // constraintset nem kell megadni Providerben, mert már amúgyis inherited value
        Size s = overlay.items().stream().
                map(item -> instantiate(item).
                        lookup(BoxLayoutProtocol.class).
                        preferredSize(constraints)).
                reduce(Size::max).
                orElse(constraints.min());

        if (!constraints.isSatisfiedBy(s))
            throw new RuntimeException(constraints + " is not satisfied by " + s + " (returned by " + this + ")");

        determinedSize.set(s);

        return new UpValueWrapper(this, peerSlot.use(peer));
    }

    @Override
    public Size preferredSize(BoxConstraints constraints) {
        Size s = determinedSize.get();
        if (s == null)
            throw new IllegalStateException();
        return s;
    }
}
