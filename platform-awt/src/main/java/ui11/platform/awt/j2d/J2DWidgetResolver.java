package ui11.platform.awt.j2d;

import ui11.*;
import ui11.ResolverRegistry.Priority;
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

public class J2DWidgetResolver implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        r.add(Priority.NATIVE, Empty.class, empty -> {
            // TODO egér viselkedés így más lesz
            return new ColorFill(Color.TRANSPARENT);
        }).offers(J2DNodeHolder.class); // vagy Priority.EMULATED?
        r.add(Priority.NATIVE, EnterContentListener.class,
                AWTEnterContentListenerPeer::new); // TODO requires? ez nem J2D-specifikus

        // TODO r.addPeerIndependentWithFilter(J2DSurface.class, PointerRegion.class, PointerRegion::content);

        /* TODO 
        r.addPeerDependent(BoxLayoutResult.SizeRequest.class, Text.class,
                // TODO így feleslegesen duplikálunk J2DTextPeereket
                (text, sizeRequest) -> new J2DTextPeer(text, null));
         */

        r.add(Priority.NATIVE, ColorFill.class, J2DColorPeer::new).offers(J2DNodeHolder.class);
        r.add(Priority.NATIVE, Overlay.class, J2DGroupPeer::new).offers(J2DNodeHolder.class);
        r.add(Priority.NATIVE, PathShaped.class, J2DPathShapedPeer::new).offers(J2DNodeHolder.class);
        r.add(Priority.NATIVE, Clip.class, J2DClipPeer::new).offers(J2DNodeHolder.class);
        r.add(Priority.NATIVE, Transform.class, J2DTransformPeer::new).offers(J2DNodeHolder.class);
        r.add(Priority.NATIVE, Text.class, J2DTextPeer::new).offers(J2DNodeHolder.class);
        r.add(Priority.NATIVE, PointerRegion.class, J2DPointerRegionPeer::new).offers(J2DNodeHolder.class);
        r.add(Priority.NATIVE, Stroke.class, J2DStrokePeer::new).offers(J2DNodeHolder.class);
        r.add(Priority.NATIVE, LinearGradient.class, J2DLinearGradientPeer::new).offers(J2DNodeHolder.class);
        r.add(Priority.EMULATED_BY_NATIVE, SVGImageView.class, J2DSVGImageViewPeer::new).offers(J2DNodeHolder.class);
        r.add(Priority.NATIVE, Opacity.class, J2DOpacityPeer::new).offers(J2DNodeHolder.class);
    }
}
