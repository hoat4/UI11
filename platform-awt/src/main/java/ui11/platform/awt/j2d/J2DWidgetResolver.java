package ui11.platform.awt.j2d;

import ui11.*;
import ui11.color.Color;
import ui11.graphics.Empty;
import ui11.graphics.Surface;
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
import ui11.provide.Provider;
import ui11.text.Text;

import java.util.List;

public class J2DWidgetResolver implements ResolverProvider {

    @Override
    public List<ResolutionRule<?>> rules() {
        return List.of(
                new ResolutionRule<>(PointerRegion.class, PointerRegion::content).
                        requires(J2DSurface.class),
                new ResolutionRule<>(Empty.class,
                        empty -> new ColorFill(Color.TRANSPARENT) /* TODO egér viselkedés így más lesz */).
                        requires(J2DSurface.class),
                new ResolutionRule<>(EnterContentListener.class, AWTEnterContentListenerPeer::new).
                        requires(J2DSurface.class), // TODO ez nem is J2DSurface

                new ResolutionRule<>(ColorFill.class, J2DColorPeer::new).requires(J2DSurface.class),
                new ResolutionRule<>(Overlay.class, J2DGroupPeer::new).requires(J2DSurface.class),
                new ResolutionRule<>(PathShaped.class, J2DPathShapedPeer::new).requires(J2DSurface.class),
                new ResolutionRule<>(Clip.class, J2DClipPeer::new).requires(J2DSurface.class),
                new ResolutionRule<>(Transform.class, J2DTransformPeer::new).requires(J2DSurface.class),
                new ResolutionRule<>(Text.class, J2DTextPeer::new).
                        requiresEither(J2DSurface.class, BoxLayoutResult.SizeRequest.class),
                new ResolutionRule<>(PointerRegion.class, J2DPointerRegionPeer::new).requires(J2DSurface.class),
                new ResolutionRule<>(Stroke.class, J2DStrokePeer::new).requires(J2DSurface.class),
                new ResolutionRule<>(LinearGradient.class, J2DLinearGradientPeer::new).requires(J2DSurface.class),
                new ResolutionRule<>(SVGImageView.class, J2DSVGImageViewPeer::new).requires(J2DSurface.class),
                new ResolutionRule<>(Opacity.class, J2DOpacityPeer::new).requires(J2DSurface.class)
        );
    }
}
