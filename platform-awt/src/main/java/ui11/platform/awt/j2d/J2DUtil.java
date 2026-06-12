package ui11.platform.awt.j2d;

import ui11.color.RGBColor;
import ui11.geom.Vec2;
import ui11.geom.*;
import ui11.geom.Path.*;
import ui11.color.Color;

import java.awt.*;
import java.awt.geom.*;
import java.util.StringJoiner;

import static java.awt.geom.PathIterator.*;

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

    public static String debugToString(Object value) {
        if (value instanceof Path2D.Double path) {
            PathIterator pathIterator = path.getPathIterator(null);
            StringJoiner sj = new StringJoiner("; ");
            while (!pathIterator.isDone()) {
                double[] coords = new double[6];
                int type = pathIterator.currentSegment(coords);
                switch (type) {
                    case SEG_MOVETO->sj.add("MOVETO ("+coords[0]+","+coords[1]+")");
                    case SEG_LINETO -> sj.add("LINETO ("+coords[0]+","+coords[1]+")");
                    case SEG_QUADTO -> sj.add("QUADTO ("+coords[0]+","+coords[1]+"),("+
                            coords[2]+","+coords[3]+")");
                    case SEG_CUBICTO -> sj.add("CUBICTO ("+coords[0]+","+coords[1]+"),("+
                            coords[2]+","+coords[3]+"),("+coords[4]+","+coords[5]+")");
                    case SEG_CLOSE -> sj.add("CLOSE");
                }
                pathIterator.next();
            }
            return "Path2D.Double: " + sj;
        }

        return String.valueOf(value);
    }

    /**
     * @param a probably outer
     * @param b probably inner
     */
    public static Shape intersection(Shape a, Shape b) {
        if (a.contains(b.getBounds2D()))
            return b;
        else {
            Area area = new Area(a);
            area.intersect(new Area(b));
            return area;
        }
    }
}
