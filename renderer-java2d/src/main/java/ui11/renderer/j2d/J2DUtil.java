package ui11.renderer.j2d;

import ui11.color.RGBColor;
import ui11.geom.Shape;
import ui11.geom.Vec2;
import ui11.geom.*;
import ui11.geom.Path.*;
import ui11.color.Color;
import ui11.graphics.Surface;

import java.awt.geom.*;

public class J2DUtil {

    public static final java.awt.Color TRANSPARENT = new java.awt.Color(0, true);

    static int round(double i) {
        return (int) (i * 1.0 + .5);
    }

    public static Rect rect(Rectangle2D r) {
        return new Rect(
                new Vec2(r.getMinX(), r.getMinY()),
                new Size(r.getWidth(), r.getHeight())
        );
    }

    public static Rectangle2D rect(Rect r) {
        return new Rectangle2D.Double(
                r.origin().x(),
                r.origin().y(),
                r.size().width(),
                r.size().height()
        );
    }

    public static java.awt.Color color(Color c) {
        RGBColor r = c.toSRGB();
        //if (!(color instanceof RGBColor r))
        //    throw new UnsupportedOperationException();
        return new java.awt.Color((float) r.red(), (float) r.green(), (float) r.blue(), (float) r.alpha());
    }

    public static Point2D.Double point(Vec2 p) {
        return new Point2D.Double(p.x(), p.y());
    }

/*
    public static Paint asPaint(Fill fill) {
        Objects.requireNonNull(fill);

        if (fill instanceof Color c)
            return color(c);
        else if (fill instanceof LinearGradient g) {
            float[] fractions = new float[g.stops().size()];
            java.awt.Color[] colors = new java.awt.Color[g.stops().size()];
            for (int i = 0; i < g.stops().size(); i++) {
                LinearGradient.Stop stop = g.stops().get(i);
                fractions[i] = (float) stop.fraction();
                colors[i] = color(stop.color());
            }
            return new LinearGradientPaint(point(g.start()), point(g.end()), fractions, colors);
        } else
            throw new UnsupportedOperationException(fill.toString());
    }
    */

    public static java.awt.Shape pathToJ2D(Path path) {
        Path2D.Double j2dPath = new Path2D.Double(Path2D.WIND_NON_ZERO);
        for (PathElement pathElement : path.items()) {
            switch (pathElement) {
                case MoveTo(Vec2 p) -> j2dPath.moveTo(p.x(), p.y());
                case LineTo(Vec2 p) -> j2dPath.lineTo(p.x(), p.y());
                case QuadCurveTo(Vec2 p, Vec2 control) -> j2dPath.quadTo(control.x(), control.y(), p.x(), p.y());
                case CubicCurveTo(Vec2 p, Vec2 control1, Vec2 control2) -> j2dPath.curveTo(control1.x(), control1.y(),
                        control2.x(), control2.y(), p.x(), p.y());
                case Close() -> j2dPath.closePath();
            }
        }
        return j2dPath;
    }

    /**
     * @param a probably outer
     * @param b probably inner
     */
    public static java.awt.Shape intersection(java.awt.Shape a, java.awt.Shape b) {
        if (a.contains(b.getBounds2D()))
            return b;
        else {
            Area area = new Area(a);
            area.intersect(new Area(b));
            return area;
        }
    }

    // nem J2D-specifikus, de egyelőre ide rakjuk
    public static boolean isNotVisible(Shape shape, Surface surface) {
        return Shape.degenerateShape().equals(shape) ||
                shape.bounds(surface.coordinateSpace()).size().equals(Size.ZERO);
    }

    // TODO fillRect ugyanaz mint fill(Rectangle)?
    public static java.awt.Shape shapeToJ2D(Shape shape, Location.CoordinateSpace coordinateSpace) {
        Rect rect = shape.asRect(coordinateSpace);
        if (rect != null)
            return rect(rect);
        Path path = shape.asPath(coordinateSpace);
        return pathToJ2D(path);
    }

    public static Path pathFromJ2D(java.awt.Shape shape) {
        throw new RuntimeException("TODO");
    }

    public static Shape shapeFromJ2D(java.awt.Shape shape, Location.CoordinateSpace coordinateSpace) {
        throw new RuntimeException("TODO");
    }
}
