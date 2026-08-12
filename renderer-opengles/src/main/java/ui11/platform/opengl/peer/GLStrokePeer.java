/*
package ui11.platform.opengl.peer;

import ui11.Widget;
import ui11.geom.Path;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.graphics.shaper.Stroke;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.platform.opengl.GLSurface;
import ui11.platform.opengl.GLSurface.GLSurfaceWithOwnShape;
import ui11.platform.opengl.J2DUtil;
import ui11.provide.Provider;
import ui11.text.TextStyle;


import java.awt.geom.Rectangle2D;

public class GLStrokePeer extends Widget {

    private final Stroke stroke;

    @Inject private Observable<Surface> parentSurface;
    @Inject private Observable<TextStyle> textStyle;

    @State private StrokeSurface surface;

    public GLStrokePeer(Stroke stroke) {
        this.stroke = stroke;
    }

    @Override
    protected void initState() {
        surface = new StrokeSurface();
    }

    @Override
    protected Widget build() {
        double thickness = stroke.thickness().px() + stroke.thickness().em() * textStyle.get().size();
        // relative része nincs a thicknessnek, ld. Stroke konstruktora

        surface.parent.set((GLSurface) parentSurface.get());
        surface.updateShape(stroke.path(), new BasicStroke((float) thickness));

        return new Provider<>(Surface.class, surface, stroke.texture());
    }

    private static class StrokeSurface extends GLSurfaceWithOwnShape {

        private Path path;

        final MutableObservable<Shape> awtShape = MutableObservable.ofNullable();
        final MutableObservable<BasicStroke> stroke = MutableObservable.ofNullable();

        private final MutableObservable<Shape> strokedShape = MutableObservable.ofNullable();
        private final MutableObservable<Size> size = MutableObservable.ofNullable();

        public void updateShape(Path path, BasicStroke stroke) {
            Shape awtShape = path == this.path ? this.awtShape.snoop() : J2DUtil.pathToJ2D(path);

            if (!awtShape.equals(this.awtShape.get()) || !stroke.equals(this.stroke.get())) {
                this.path = path;
                this.stroke.set(stroke);
                this.awtShape.set(awtShape);
                this.size.set(null);
                this.strokedShape.set(null);

                // TODO ez a nullozgatás felesleges invalidálásokat okoz shape()-nél és size()-nál.
                //      kéne valami API MutableObservable-be, ami megmondja hogy van-e observere éppen.
            }
        }

        @Override
        public Size size() {
            Size size = this.size.get();
            if (size == null) {
                Rectangle2D bounds = awtShape.get().getBounds2D();
                size = new Size(bounds.getMaxX() + stroke.get().getLineWidth(),
                        bounds.getMaxY() + stroke.get().getLineWidth());
                this.size.set(size);
            }
            return size;
        }

        @Override
        public Shape shape() {
            Shape strokedShape = this.strokedShape.get();
            if (strokedShape == null) {
                strokedShape = stroke.get().createStrokedShape(this.awtShape.get());
                this.strokedShape.set(strokedShape);
            }
            return strokedShape;
        }
    }
}
*/