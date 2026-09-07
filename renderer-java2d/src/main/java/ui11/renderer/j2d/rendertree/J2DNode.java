package ui11.renderer.j2d.rendertree;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.geom.Vec4;
import ui11.renderer.Node;
import ui11.renderer.input.InputNode;
import ui11.renderer.j2d.J2DUtil;
import ui11.renderer.j2d.RenderingContext;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.StringJoiner;

import static java.awt.geom.PathIterator.*;
import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_CUBICTO;

public abstract class J2DNode extends Node {

    public abstract void render(RenderingContext ctx);

    public static class J2DRenderTreePrinter extends RenderTreePrinter {
        @Override
        protected @NonNull String valueToString(@Nullable Object value) {
            if (value instanceof Path2D.Double path) {
                PathIterator pathIterator = path.getPathIterator(null);
                StringJoiner sj = new StringJoiner("; ");
                while (!pathIterator.isDone()) {
                    double[] coords = new double[6];
                    int type = pathIterator.currentSegment(coords);
                    switch (type) {
                        case SEG_MOVETO -> sj.add("MOVETO (" + coords[0] + "," + coords[1] + ")");
                        case SEG_LINETO -> sj.add("LINETO (" + coords[0] + "," + coords[1] + ")");
                        case SEG_QUADTO -> sj.add("QUADTO (" + coords[0] + "," + coords[1] + "),(" +
                                coords[2] + "," + coords[3] + ")");
                        case SEG_CUBICTO -> sj.add("CUBICTO (" + coords[0] + "," + coords[1] + "),(" +
                                coords[2] + "," + coords[3] + "),(" + coords[4] + "," + coords[5] + ")");
                        case SEG_CLOSE -> sj.add("CLOSE");
                    }
                    pathIterator.next();
                }
                return "Path2D.Double: " + sj;
            }

            return String.valueOf(value);
        }
    }
}
