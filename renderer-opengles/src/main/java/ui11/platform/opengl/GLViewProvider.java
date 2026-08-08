package ui11.platform.opengl;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.*;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.shaper.RectangleShaped;
import ui11.platform.opengl.peer.GLColorFillPeer;
import ui11.platform.opengl.peer.GLOverlayPeer;
import ui11.platform.opengl.peer.GLRectShapedPeer;
import ui11.platform.opengl.peer.GLTransformPeer;

public class GLViewProvider extends WidgetResolver {

    public static final GLViewProvider INSTANCE = new GLViewProvider();

    private GLViewProvider() {
    }

    @Override
    protected @Nullable Widget tryResolveGeneric(@NonNull SubstitutedWidget widget) {
        return null;
    }

    @Override
    protected @Nullable Widget tryResolveRequestSpecific(@NonNull SubstitutedWidget widget, @NonNull PeerRequest<?> request) {
        if (!(request instanceof GLSurface surface))
            return null;

        return switch (widget) {
            case ColorFill colorFill -> new GLColorFillPeer(colorFill, surface);
            case Overlay overlay -> new GLOverlayPeer(overlay, surface);
            case Transform transform -> new GLTransformPeer(transform, surface);
            case RectangleShaped clip -> new GLRectShapedPeer(clip, surface);
            default -> null;
        };
    }
}
