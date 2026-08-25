package ui11.platform.opengl;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.ResolverRegistry.Priority;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.shaper.RectangleShaped;
import ui11.platform.opengl.peer.GLColorFillPeer;
import ui11.platform.opengl.peer.GLOverlayPeer;
import ui11.platform.opengl.peer.GLRectShapedPeer;
import ui11.platform.opengl.peer.GLTransformPeer;

public class GLResolverProvider implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        r.add(Priority.NATIVE, ColorFill.class, GLColorFillPeer::new);
        r.add(Priority.NATIVE, Overlay.class, GLOverlayPeer::new);
        r.add(Priority.NATIVE, Transform.class, GLTransformPeer::new);
        r.add(Priority.NATIVE, RectangleShaped.class, GLRectShapedPeer::new);
    }
}
