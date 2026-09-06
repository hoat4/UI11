package ui11.platform.opengl;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
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
        r.registerForContextType(GLVisualContentRequest.class, ColorFill.class, GLColorFillPeer::new);
        r.registerForContextType(GLVisualContentRequest.class, Overlay.class, GLOverlayPeer::new);
        r.registerForContextType(GLVisualContentRequest.class, Transform.class, GLTransformPeer::new);
        r.registerForContextType(GLVisualContentRequest.class, RectangleShaped.class, GLRectShapedPeer::new);
    }
}
