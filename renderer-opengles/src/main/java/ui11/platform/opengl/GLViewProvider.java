package ui11.platform.opengl;

import org.jspecify.annotations.NonNull;
import ui11.Widget;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.shaper.RectangleShaped;
import ui11.platform.opengl.peer.GLColorFillPeer;
import ui11.platform.opengl.peer.GLOverlayPeer;
import ui11.platform.opengl.peer.GLRectShapedPeer;
import ui11.platform.opengl.peer.GLTransformPeer;
import ui11.PeerCreationRequest;
import ui11.WidgetResolver;

public class GLViewProvider implements WidgetResolver {
    @Override
    public Widget resolveOrNull(Widget widget, @NonNull PeerCreationRequest<?> peerCreationRequest) {
        return switch (widget) {
            case ColorFill colorFill -> new GLColorFillPeer(colorFill);
            case Overlay overlay -> new GLOverlayPeer(overlay);
            case Transform transform -> new GLTransformPeer(transform);
            case RectangleShaped clip -> new GLRectShapedPeer(clip);
            default -> null;
        };
    }
}
