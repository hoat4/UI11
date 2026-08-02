package ui11.platform.opengl;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.PeerRequestor;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.shaper.RectangleShaped;
import ui11.platform.opengl.peer.GLColorFillPeer;
import ui11.platform.opengl.peer.GLOverlayPeer;
import ui11.platform.opengl.peer.GLRectShapedPeer;
import ui11.platform.opengl.peer.GLTransformPeer;
import ui11.WidgetResolver;

public class GLViewProvider extends WidgetResolver {

    public static final GLViewProvider INSTANCE = new GLViewProvider();

    private GLViewProvider() {
    }

    @Override
    protected @Nullable Widget tryResolveGeneric(@NonNull SubstitutedWidget widget) {
        return null;
    }

    @Override
    protected @Nullable Widget tryResolveRequestSpecific(@NonNull SubstitutedWidget widget, PeerRequestor.@NonNull Request<?> request) {
        if (!(request instanceof GLNodeHolder.GLNodeRequest req))
            return null;

        return switch (widget) {
            case ColorFill colorFill -> new GLColorFillPeer(colorFill);
            case Overlay overlay -> new GLOverlayPeer(overlay);
            case Transform transform -> new GLTransformPeer(transform);
            case RectangleShaped clip -> new GLRectShapedPeer(clip);
            default -> null;
        };
    }
}
