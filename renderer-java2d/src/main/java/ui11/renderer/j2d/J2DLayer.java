package ui11.renderer.j2d;

import ui11.color.Color;
import ui11.geom.Location;
import ui11.geom.Mat4;
import ui11.geom.Shape;
import ui11.geom.Vec2;
import ui11.observable.ObservableList;
import ui11.renderer.TextRenderer;
import ui11.renderer.layer.Layer;
import ui11.renderer.subsurface.StrokedShape;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class J2DLayer extends Layer {

    private final Location.CoordinateSpace coordinateSpace;
    private final ObservableList<Task> tasks = new ObservableList<>();

    public J2DLayer(Location.CoordinateSpace coordinateSpace) {
        this.coordinateSpace = coordinateSpace;
    }

    @Override
    public LayerUpdater createLayerUpdater() {
        return new J2DLayerUpdater();
    }

    public Location.CoordinateSpace coordinateSpace() {
        return coordinateSpace;
    }

    public void execute(RenderingContext ctx) {
        for (Task task : tasks)
            task.execute(ctx.g);
    }

    class J2DLayerUpdater implements LayerUpdater {

        private final List<Task> tasks = new ArrayList<>();
        private final Location.CoordinateSpace coordinateSpace;

        public J2DLayerUpdater() {
            this.coordinateSpace = J2DLayer.this.coordinateSpace();
        }

        @Override
        public void fill(Shape shape, Color color) {
            java.awt.Color awtColor = J2DUtil.color(color);
            if (shape instanceof StrokedShape strokedShape) {
                java.awt.Shape awtShape = J2DUtil.pathToJ2D(strokedShape.path.transform(
                        strokedShape.coordinateSpace.transformationTo(coordinateSpace)));
                addTask(g -> {
                    g.setColor(awtColor);
                    g.setStroke(new BasicStroke((float) strokedShape.thickness));
                    g.draw(awtShape);
                });
            } else {
                java.awt.Shape awtShape = J2DUtil.shapeToJ2D(shape, coordinateSpace);
                addTask(g -> {
                    g.setColor(awtColor);
                    g.fill(awtShape);
                });
            }
        }

        @Override
        public void fillWithLinearGradient(Shape shape, Vec2 direction, Vec2[] lineStarts, Color[] colors) {
            throw new RuntimeException("TODO");
        }

        @Override
        public void fill(Shape shape, Layer sublayer) {
            throw new RuntimeException("TODO");
        }

        @Override
        public void blend(double opacity, Layer sublayer) {
            throw new RuntimeException("TODO");
        }

        @Override
        public void text(TextRenderer.TextLayout text, Location.CoordinateSpace cs) {
            J2DTextLayoutCalculator.J2DTextLayout l = (J2DTextLayoutCalculator.J2DTextLayout) text;
            addTask(g -> {
                g.setColor(l.color);
                g.setFont(l.font);

                AffineTransform t = transformation(cs);
                double x = 0, y = l.maxAscent;
                if ((t.getType() & ~AffineTransform.TYPE_TRANSLATION) == 0) {
                    x += t.getTranslateX();
                    y += t.getTranslateY();
                    g.drawString(l.text, (float) x, (float) y);
                } else {
                    AffineTransform prevTransform = g.getTransform();
                    g.transform(t);
                    g.drawString(l.text, (float) x, (float) y);
                    g.setTransform(prevTransform);
                }
            });
        }

        void addTask(Task task) {
            tasks.add(task);
        }

        AffineTransform transformation(Location.CoordinateSpace cs) {
            // TODO 3D esetén kéne fallback (pl. SVG-hez), nem eldobni a maradék mátrixelemeket.
            //      pl. szoftveresen elvégezni a perspektív osztást
            Mat4 t = cs.transformationTo(coordinateSpace);
            AffineTransform tx = new AffineTransform();
            tx.setTransform(
                    t.m00(), t.m01(),
                    t.m10(), t.m11(),
                    t.m30(), t.m31()
            );
            return tx;
        }

        @Override
        public void finish() {
            J2DLayer.this.tasks.setAll(this.tasks);
        }
    }

    interface Task {

        void execute(Graphics2D g);
    }
}
