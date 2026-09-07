package ui11.renderer.j2d.rendertree;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.renderer.PlatformSupport;
import com.github.weisj.jsvg.view.ViewBox;
import ui11.geom.Size;
import ui11.geom.Vec4;
import ui11.observable.InvalidationPoint;
import ui11.observable.MutableObservable;
import ui11.renderer.input.InputNode;
import ui11.renderer.j2d.RenderingContext;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

public class SVGDocumentNode extends J2DNode {

    public final MutableObservable<SVGDocument> svgDocument = MutableObservable.ofNullable();
    public final MutableObservable<Font> font = MutableObservable.ofNullable();
    public final MutableObservable<Size> size = MutableObservable.ofNullable();

    private final PlatformSupportImpl platformSupportImpl = new PlatformSupportImpl();

    // TODO ha lecserélődik az SVGDocument, akkor ezt le kéne tiltani/cserélin valahogy
    private final InvalidationPoint animationIP = new InvalidationPoint();

    @Override
    public void render(RenderingContext ctx) {
        animationIP.subscribe();
        AffineTransform prevTransform = ctx.g.getTransform();
        ctx.g.transform(ctx.transform);
        svgDocument.get().renderWithPlatform(platformSupportImpl, ctx.g,
                new ViewBox((float) size.get().width(), (float) size.get().height()));
        ctx.g.setTransform(prevTransform);
    }

    @Override
    public boolean pick(InputNode.PickContext pickContext, Vec4 p) {
        throw new RuntimeException("TODO SVG mouse events");
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("svgDocument", svgDocument.get().toString());
    }

    private class PlatformSupportImpl implements PlatformSupport {

        private final ImageObserver imageObserver = new ImageObserver() {
            @Override
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                animationIP.invalidate();
                return (infoflags & (ALLBITS|ABORT)) == 0;
            }
        };

        private final TargetSurface targetSurface = new TargetSurface() {
            @Override
            public void repaint() {
                animationIP.invalidate();
            }
        };

        @Nullable
        @Override
        public ImageObserver imageObserver() {
            return imageObserver;
        }

        @Nullable
        @Override
        public TargetSurface targetSurface() {
            return targetSurface;
        }

        @Override
        public float fontSize() {
            return font.get().getSize2D();
        }

        @Override
        public @NonNull String fontFamily() {
            return font.get().getFamily();
        }

        @Override
        public boolean isLongLived() {
            return true;
        }
    }
}
