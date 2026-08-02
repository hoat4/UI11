package ui11.platform.awt.j2d;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.PeerRequestor;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.WidgetResolver;
import ui11.color.Color;
import ui11.graphics.Empty;
import ui11.graphics.effect.Clip;
import ui11.graphics.effect.Opacity;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.fill.LinearGradient;
import ui11.graphics.shaper.PathShaped;
import ui11.graphics.shaper.Stroke;
import ui11.input.gesture.EnterContentListener;
import ui11.input.pointer.PointerRegion;
import ui11.media.SVGImageView;
import ui11.platform.awt.AWTEnterContentListenerPeer;
import ui11.platform.awt.j2d.peer.*;
import ui11.text.Text;

import java.util.Set;

public class J2DWidgetResolver extends WidgetResolver {

    public static final J2DWidgetResolver INSTANCE = new J2DWidgetResolver();

    private J2DWidgetResolver() {
    }

    @Override
    protected @Nullable Widget tryResolveGeneric(@NonNull SubstitutedWidget widget) {
        return switch (widget) {
            case PointerRegion pointerRegion -> pointerRegion.content();
            case Empty empty -> new ColorFill(Color.TRANSPARENT); // TODO egér viselkedés így más lesz

            // ennek majd kéne csinálni külön VP-t, mert nem J2D
            case EnterContentListener l -> new AWTEnterContentListenerPeer(l);

            default -> null;
        };
    }

    @Override
    protected @Nullable Widget tryResolveRequestSpecific(@NonNull SubstitutedWidget widget,
                                                         PeerRequestor.@NonNull Request<?> request) {
        if (!(request instanceof J2DSurface surface))
            return null;

        return switch (widget) {
            case ColorFill c -> new J2DColorPeer(c, surface);
            case Overlay overlay -> new J2DGroupPeer(overlay, surface);
            case PathShaped clip -> new J2DPathShapedPeer(clip, surface);
            case Clip clip -> new J2DClipPeer(clip, surface);
            case Transform transform -> new J2DTransformPeer(transform, surface);
            case Text text -> new J2DTextPeer(text, surface);
            case PointerRegion pointerRegion -> new J2DPointerRegionPeer(pointerRegion, surface);
            case Stroke stroke -> new J2DStrokePeer(stroke, surface);
            case LinearGradient linearGradient -> new J2DLinearGradientPeer(linearGradient, surface);
            case SVGImageView svg -> new J2DSVGImageViewPeer(svg, surface);
            case Opacity opacity -> new J2DOpacityPeer(opacity, surface);

            default -> null;
        };
    }
}
