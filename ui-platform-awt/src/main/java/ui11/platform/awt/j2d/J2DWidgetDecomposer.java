package ui11.platform.awt.j2d;

import ui11.Widget;
import ui11.graphics.*;
import ui11.graphics.effect.ClipPath;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Stroke;
import ui11.graphics.effect.Transform;
import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.fill.LinearGradient;
import ui11.input.gesture.EnterContentListener;
import ui11.platform.awt.AWTEnterContentListenerPeer;
import ui11.provide.UpValueWrapper;
import ui11.resolution.WidgetResolver;
import ui11.input.pointer.PointerRegion;
import ui11.text.Text;

import javax.annotation.Nullable;

public class J2DWidgetDecomposer implements WidgetResolver {

    public static final WidgetResolver INSTANCE = new J2DWidgetDecomposer();

    private J2DWidgetDecomposer() {
    }

    @Nullable
    @Override
    public Widget resolveOrNull(Widget widget, ResolutionContext resolutionContext) {
        return switch (widget) {
            case ColorFill c -> new UpValueWrapper(new J2DColorPrimitive(c.color()));
            case Overlay overlay -> new J2DGroupPeer(overlay);
            case ClipPath clip -> new J2DClipPeer(clip);
            case Transform transform -> new J2DTransformPeer(transform);
            case Text text -> new J2DTextPeer(text);
            case PointerRegion pointerRegion -> new J2DPointerRegionPeer(pointerRegion);
            case Stroke stroke -> new J2DStrokePeer(stroke);
            case Empty empty -> new ColorFill(Color.TRANSPARENT); // TODO egér viselkedés így más lesz
            case LinearGradient linearGradient -> new J2DLinearGradientPeer(linearGradient);

            // ennek majd kéne csinálni külön VP-t, mert nem J2D
            case EnterContentListener l -> new AWTEnterContentListenerPeer(l);

            case null, default -> null;
        };
    }
}
