package ui11.renderer.peer;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.graphics.Empty;
import ui11.graphics.effect.Clip;
import ui11.graphics.effect.Opacity;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.fill.LinearGradient;
import ui11.graphics.shaper.PathShaped;
import ui11.graphics.shaper.RectangleShaped;
import ui11.graphics.shaper.Stroke;
import ui11.input.pointer.PointerRegion;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.layout.protocol.BoxLayoutResult.SizeRequest;
import ui11.renderer.layer.Item;
import ui11.renderer.layer.Item.ItemRequest;
import ui11.renderer.subsurface.StrokedShape;
import ui11.text.Text;

import java.util.Set;

public class ItemPeerProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.registerForContextType(ItemRequest.class, Clip.class, ClipPeer::new);
        r.registerForContextType(ItemRequest.class, ColorFill.class, ColorFillPeer::new);
        r.registerForContextType(ItemRequest.class, Empty.class, __ -> EmptyPeer.INSTANCE);
        r.registerForContextType(ItemRequest.class, LinearGradient.class, LinearGradientPeer::new);
        r.registerForContextType(ItemRequest.class, Opacity.class, OpacityPeer::new);
        r.registerForContextType(ItemRequest.class, Overlay.class, OverlayPeer::new);
        r.registerForContextType(ItemRequest.class, PathShaped.class, PathShapedPeer::new);
        r.registerForContextType(ItemRequest.class, PointerRegion.class, PointerRegionPeer::new);
        r.registerForContextType(ItemRequest.class, RectangleShaped.class, RectShapedPeer::new);
        r.registerForContextType(ItemRequest.class, Stroke.class, StrokePeer::new);
        r.registerForContextTypes(Set.of(ItemRequest.class, SizeRequest.class), Text.class, TextPeer::new);
        r.registerForContextType(ItemRequest.class, Transform.class, TransformPeer::new);
    }
}
