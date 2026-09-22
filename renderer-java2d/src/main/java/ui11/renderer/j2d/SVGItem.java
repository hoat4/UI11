package ui11.renderer.j2d;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.renderer.PlatformSupport;
import com.github.weisj.jsvg.view.ViewBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.geom.Location;
import ui11.geom.Size;
import ui11.observable.InvalidationPoint;
import ui11.renderer.layer.Item;
import ui11.renderer.layer.Layer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.util.Collection;
import java.util.List;

public record SVGItem(SVGDocument document, Font font, Size size,
                      Location.CoordinateSpace coordinateSpace) implements Item {
    @Override
    public Collection<? extends Layer> referredSublayers() {
        return List.of();
    }

    @Override
    public void visit(Layer.LayerUpdater layerUpdater) {
        J2DLayer.J2DLayerUpdater u = (J2DLayer.J2DLayerUpdater) layerUpdater;
        u.addTask(g -> {
            AffineTransform prevTransform = g.getTransform();
            g.transform(u.transformation(coordinateSpace));
            document.renderWithPlatform(new PlatformSupportImpl(font), g,
                    new ViewBox((float) size.width(), (float) size.height()));
            g.setTransform(prevTransform);
        });
    }

    private static class PlatformSupportImpl implements PlatformSupport {

        private final InvalidationPoint animationIP;
        private final Font font;

        public PlatformSupportImpl(Font font) {
            this.font = font;
            this.animationIP = new InvalidationPoint();
            animationIP.subscribe();
        }

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
            return font.getSize2D();
        }

        @Override
        public @NonNull String fontFamily() {
            return font.getFamily();
        }

        @Override
        public boolean isLongLived() {
            return true;
        }
    }
}
