package ui11.platform.awt.j2d;

import ui11.*;
import ui11.observable.Observable;
import ui11.geom.Vec2;
import ui11.input.pointer.Pointer.StandardMouseButton;
import ui11.input.pointer.PointerRegion;
import ui11.input.pointer.PointerRegion.PointerListener;
import ui11.platform.awt.AWTMouse;
import ui11.provide.Provider;
import ui11.provide.UpValueWrapper;

import java.awt.*;

public class J2DPointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;

    @Inject(required = false) private Observable<J2DPointerRegionPeerImpl> upper;
    @Inject private Slot contentSlot;

    @State private J2DPointerRegionPeerImpl state;

    public J2DPointerRegionPeer(PointerRegion pointerRegion) {
        this.pointerRegion = pointerRegion;
    }

    @Override
    protected void initState() {
        state = new J2DPointerRegionPeerImpl();
    }

    @Override
    protected Widget build() {
        Widget wrappedContent = new Provider<>(J2DPointerRegionPeerImpl.class, state,
                pointerRegion.content());
        WidgetInstantiation h = contentSlot.instantiate(wrappedContent);
        state.content = h.lookup(J2DPrimitive.class);
        state.upper = upper.get();
        state.handler = pointerRegion;
        return new UpValueWrapper(state, contentSlot.use(wrappedContent));
    }

    public static class J2DPointerRegionPeerImpl implements J2DPrimitive {

        private J2DPrimitive content;
        private J2DPointerRegionPeerImpl upper;
        private PointerRegion handler;

        @Override
        public void draw(Graphics2D g, Rectangle bounds) {
            content.draw(g, bounds);
        }

        @Override
        public PickResult findInputRegion(Vec2 p) {
            PickResult r = content.findInputRegion(p);
            if (r != null)
                return r;

            return new PickResult(this, p);
        }

        public PointerListener handleMousePress(Vec2 point) {
            for (J2DPointerRegionPeerImpl p = this; p != null; p = p.upper) {
                PointerListener result = p.handler.onPointerDown(AWTMouse.INSTANCE,
                        StandardMouseButton.PRIMARY /* TODO */);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
    }
}
