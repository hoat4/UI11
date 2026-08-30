package ui11.platform.opengl;

import ui11.ResolverProvider;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.shaper.RectangleShaped;
import ui11.platform.opengl.peer.GLColorFillPeer;
import ui11.platform.opengl.peer.GLOverlayPeer;
import ui11.platform.opengl.peer.GLRectShapedPeer;
import ui11.platform.opengl.peer.GLTransformPeer;

import java.util.List;

public class GLResolverProvider implements ResolverProvider {

    @Override
    public List<ResolutionRule<?>> rules() {
        return List.of(
                new ResolutionRule<>(ColorFill.class, GLColorFillPeer::new).requires(GLSurface.class),
                new ResolutionRule<>(Overlay.class, GLOverlayPeer::new).requires(GLSurface.class),
                new ResolutionRule<>(Transform.class, GLTransformPeer::new).requires(GLSurface.class),
                new ResolutionRule<>(RectangleShaped.class, GLRectShapedPeer::new).requires(GLSurface.class)
        );
    }
}
