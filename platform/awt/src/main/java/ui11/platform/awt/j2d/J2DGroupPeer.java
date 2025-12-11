package ui11.platform.awt.j2d;

import ui11.KeyWrapper;
import ui11.MultiSlot;
import ui11.Widget;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.effect.Overlay;
import ui11.observable.Observable;
import ui11.provide.UpValueWrapper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class J2DGroupPeer extends Widget {

    private final Overlay overlay;

    @Inject private Observable<Surface> surface;

    @State private J2DGroupPeerImpl state;

    public J2DGroupPeer(Overlay overlay) {
        this.overlay = overlay;
    }

    @Override
    protected void initState() {
        state = new J2DGroupPeerImpl();
    }

    @Override
    protected Widget build() {
        /*
        System.out.println();
        System.out.println();
        System.out.println("J2DGroupPeer update: \n"+debug_getRefreshStack());
        System.out.println();
        System.out.println();
         */
        List<J2DPrimitive> prevChildren = state.childrenPeers;
        state.childrenPeers = new ArrayList<>();
        for (KeyWrapper widget : overlay.items())
            state.childrenPeers.add(instantiate(widget).lookup(J2DPrimitive.class));
        if (!state.childrenPeers.equals(prevChildren))
            ((J2DSurface) surface.get()).requestRepaint();
        return new UpValueWrapper(state);
    }

    private static class J2DGroupPeerImpl implements J2DPrimitive {

        private List<J2DPrimitive> childrenPeers;

        @Override
        public void draw(Graphics2D g, Rectangle bounds) {
            for (J2DPrimitive p : childrenPeers)
                p.draw(g, bounds);
        }

        @Override
        public PickResult findInputRegion(Vec2 p) {
            for (int i = childrenPeers.size() - 1; i >= 0; i--) {
                PickResult r = childrenPeers.get(i).findInputRegion(p);
                if (r != null)
                    return r;
            }
            return null;
        }
    }
}
