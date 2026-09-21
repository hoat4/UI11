package ui11.renderer.peer;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.shaper.RectangleShaped;
import ui11.renderer.layer.Item;

public class ItemPeerProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.registerForContextType(Item.ItemRequest.class, ColorFill.class, ColorFillPeer::new);
        r.registerForContextType(Item.ItemRequest.class, Overlay.class, OverlayPeer::new);
        r.registerForContextType(Item.ItemRequest.class, RectangleShaped.class, RectShapedPeer::new);
        r.registerForContextType(Item.ItemRequest.class, Transform.class, TransformPeer::new);
    }
}
