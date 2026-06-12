package ui11.platform.awt.j2d;

import org.jspecify.annotations.NonNull;
import ui11.Widget;
import ui11.graphics.Empty;
import ui11.graphics.effect.Clip;
import ui11.graphics.effect.Opacity;
import ui11.graphics.shaper.PathShaped;
import ui11.graphics.effect.Overlay;
import ui11.graphics.shaper.Stroke;
import ui11.graphics.effect.Transform;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.fill.LinearGradient;
import ui11.input.gesture.EnterContentListener;
import ui11.input.pointer.PointerRegion;
import ui11.media.SVGImageView;
import ui11.platform.awt.AWTEnterContentListenerPeer;
import ui11.platform.awt.j2d.peer.*;
import ui11.resolution.PeerCreationRequest;
import ui11.resolution.WidgetResolver;
import ui11.text.Text;

import org.jspecify.annotations.Nullable;

public class J2DWidgetResolver implements WidgetResolver {

    public static final WidgetResolver INSTANCE = new J2DWidgetResolver();

    private J2DWidgetResolver() {
    }

    @Override
    public @Nullable Widget resolveOrNull(@NonNull Widget widget, @NonNull PeerCreationRequest<?> peerCreationRequest) {
        return switch (widget) {
            case ColorFill c -> new J2DColorPeer(c);
            case Overlay overlay -> new J2DGroupPeer(overlay);
            case PathShaped clip -> new J2DPathShapedPeer(clip);
            case Clip clip -> new J2DClipPeer(clip);
            case Transform transform -> new J2DTransformPeer(transform);
            case Text text -> new J2DTextPeer(text);
            case PointerRegion pointerRegion -> new J2DPointerRegionPeer(pointerRegion);
            case Stroke stroke -> new J2DStrokePeer(stroke);
            case Empty empty -> new ColorFill(Color.TRANSPARENT); // TODO egér viselkedés így más lesz
            case LinearGradient linearGradient -> new J2DLinearGradientPeer(linearGradient);
            case SVGImageView svg -> new J2DSVGImageViewPeer(svg);
            case Opacity opacity -> new J2DOpacityPeer(opacity);

            // ennek majd kéne csinálni külön VP-t, mert nem J2D
            case EnterContentListener l -> new AWTEnterContentListenerPeer(l);

            case null, default -> null;
        };
    }
}
