package ui11.renderer.j2d;

import ui11.*;
import ui11.graphics.shaper.Stroke;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.media.SVGImageView;
import ui11.renderer.layer.Item;
import ui11.renderer.peer.TextPeer;
import ui11.renderer.peer.StrokePeer;
import ui11.text.Text;

import java.util.Set;

public class J2DWidgetResolver implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        // TODO itt valahogy meg kéne tudni adni, hogy csak J2D renderer esetén használható
        r.registerForContextType(Item.ItemRequest.class, SVGImageView.class, J2DSVGImageViewPeer::new);
    }
}
