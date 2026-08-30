package ui11.platform.awt.j2d;

import ui11.*;
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
import ui11.layout.protocol.BoxLayoutResult;
import ui11.media.SVGImageView;
import ui11.platform.awt.AWTEnterContentListenerPeer;
import ui11.platform.awt.j2d.peer.*;
import ui11.text.Text;

import java.util.Set;

public class J2DWidgetResolver implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        r.registerForContextType(J2DSurface.class, PointerRegion.class, PointerRegion::content);
        r.registerForContextType(J2DSurface.class, Empty.class,
                empty -> new ColorFill(Color.TRANSPARENT) /* TODO egér viselkedés így más lesz */);
        r.registerForContextType(J2DSurface.class, EnterContentListener.class, // TODO ez nem is J2DSurface
                AWTEnterContentListenerPeer::new);

        r.registerForContextType(J2DSurface.class, ColorFill.class, J2DColorPeer::new);
        r.registerForContextType(J2DSurface.class, Overlay.class, J2DGroupPeer::new);
        r.registerForContextType(J2DSurface.class, PathShaped.class, J2DPathShapedPeer::new);
        r.registerForContextType(J2DSurface.class, Clip.class, J2DClipPeer::new);
        r.registerForContextType(J2DSurface.class, Transform.class, J2DTransformPeer::new);
        r.registerForContextTypes(Set.of(J2DSurface.class, BoxLayoutResult.SizeRequest.class),
                Text.class, J2DTextPeer::new);
        r.registerForContextType(J2DSurface.class, PointerRegion.class, J2DPointerRegionPeer::new);
        r.registerForContextType(J2DSurface.class, Stroke.class, J2DStrokePeer::new);
        r.registerForContextType(J2DSurface.class, LinearGradient.class, J2DLinearGradientPeer::new);
        r.registerForContextType(J2DSurface.class, SVGImageView.class, J2DSVGImageViewPeer::new);
        r.registerForContextType(J2DSurface.class, Opacity.class, J2DOpacityPeer::new);
    }
}
