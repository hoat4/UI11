package ui11.platform.awt.j2d;

import ui11.geom.Vec2;
import ui11.geom.*;
import ui11.geom.Path.*;
import ui11.graphics.fill.Color;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class J2DUtil {

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

    public static java.awt.Color color(Color r) {
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
                case CubicCurveTo(Vec2 p, Vec2 control1, Vec2 control2) ->
                        j2dPath.curveTo(control1.x(), control1.y(),
                                control2.x(), control2.y(), p.x(), p.y());
                case Close() -> j2dPath.closePath();
            }
        }
        return j2dPath;
    }
}
