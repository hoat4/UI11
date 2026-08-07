package ui11.platform.opengl.peer;

import ui11.Widget;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.fill.LinearGradient;
import ui11.graphics.fill.LinearGradient.Stop;
import ui11.observable.Observable;
import ui11.platform.opengl.BufferPool;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.GLSurface;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.inputtree.OpaqueInputNode;
import ui11.platform.opengl.inputtree.TransparentInputNode;
import ui11.platform.opengl.renderer.Shaders;
import ui11.platform.opengl.rendertree.EmptyRenderNode;
import ui11.platform.opengl.rendertree.FillTrianglesWithColorRenderNode;
import ui11.text.TextStyle;

public class GLLinearGradientPeer extends Widget {

    private final LinearGradient gradient;
    private final GLSurface surface;

    @Inject private TextStyle textStyle;
    @Inject private BufferPool bufferPool;

    @Remember private FillTrianglesWithColorRenderNode node;
    @Remember private OpaqueInputNode inputNode;

    public GLLinearGradientPeer(LinearGradient gradient, GLSurface surface) {
        this.gradient = gradient;
        this.surface = surface;
    }

    @Override
    protected void initState() {
        node = new FillTrianglesWithColorRenderNode();
        inputNode = new OpaqueInputNode();
    }

    @Override
    protected Widget build() {
        Shape2D shape = surface.shape();
        if (shape == Shape2D.InfinitePlane.INFINITE_PLANE)
            return surface.createResponse(new GLNodeHolder(EmptyRenderNode.INSTANCE, TransparentInputNode.INSTANCE));

        double emSize = textStyle.size();
        double deg = gradient.angleDeg();
        double w = surface.size().width(), h = surface.size().height();

        deg -= 90;
        if (deg < 0)
            deg = 360 + deg % 360;
        else
            deg %= 360;
        if (deg >= 180)
            deg = 360 - deg;
        if (deg >= 90)
            deg = 180 - deg;

        double angleRad = Math.toRadians(deg);
        double gradientLengthPX = Math.sin(angleRad) * h + Math.cos(angleRad) * w;

        int stopCount = gradient.stops().size();
        Vec2[] lineStarts = new Vec2[stopCount];
        int[] colors = new int[stopCount];
        int estimatedTriangleCount = shape.estimateTriangleCount() * 3 / 2 + stopCount;
        BufferPool.GrowableVertexBuffer buf = bufferPool.allocate(
                Shaders.SolidPolygonShader.BYTES_PER_VERTEX * 3 * estimatedTriangleCount);
        Vec2 direction = Vec2.ofPolarRad(-Math.toRadians(gradient.angleDeg() - 90), 1);
        for (int i = 0; i < stopCount; i++) {
            Stop stop = gradient.stops().get(i);
            double posInPX = stop.pos().em() * emSize + stop.pos().px() + stop.pos().rel() * gradientLengthPX;
            lineStarts[i] = direction.mul(posInPX);
            colors[i] = stop.color().toSRGB().toRGBA(buf.order());
        }

        TriangleSplitter triangleSplitter = new TriangleSplitter(
                direction.rotate90CounterClockwise(), lineStarts, colors, buf, surface.renderNodeTranslation());
        shape.toTriangles(triangleSplitter);
        node.vertices.set(buf.finish());

        // EmptyRenderNode?

        inputNode.shape.set(shape);

        return surface.createResponse(new GLNodeHolder(node, inputNode));
    }

    private static class TriangleSplitter implements Shape2D.Triangle2DConsumer {

        private final Vec2 perpendicularDirection;
        private final Vec2[] lines;
        private final int[] colors;
        private final BufferPool.GrowableVertexBuffer out;
        private final Vec2 renderNodeTranslation;
        private int currentColor;

        public TriangleSplitter(Vec2 perpendicularDirection, Vec2[] lines, int[] colors,
                                BufferPool.GrowableVertexBuffer out, Vec2 renderNodeTranslation) {
            this.perpendicularDirection = perpendicularDirection;
            this.lines = lines;
            this.colors = colors;
            this.out = out;
            this.renderNodeTranslation = renderNodeTranslation;
        }

        @Override
        public void accept(Vec2 a, Vec2 b, Vec2 c) {
            Vec2 d = null; // 4. pontja a trapezoidnak a,b,c után

            int i = 0;
            for (; i < lines.length; i++) {
                currentColor = colors[i];

                Vec2 lineStart = lines[i];

                if (d == null) { // háromszög
                    double intersectionAB = intersect(a, b, lineStart);
                    boolean intersectsAB = intersectionAB >= 0 && intersectionAB <= 1;
                    double intersectionBC = intersect(b, c, lineStart);
                    boolean intersectsBC = intersectionBC >= 0 && intersectionBC <= 1;
                    double intersectionCA = intersect(c, a, lineStart);
                    boolean intersectsCA = intersectionCA >= 0 && intersectionCA <= 1;

                    if (intersectsAB) {
                        Vec2 intersectionPointOnAB = Vec2.lerp(a, b, intersectionAB);
                        if (intersectsBC) {
                            Vec2 intersectionPointOnBC = Vec2.lerp(b, c, intersectionBC);
                            d = a;
                            emitTriangle(b, b = intersectionPointOnBC, a = intersectionPointOnAB);
                        } else if (intersectsCA) {
                            Vec2 intersectionPointOnCA = Vec2.lerp(c, a, intersectionCA);
                            c = b;
                            d = c;
                            emitTriangle(a, b = intersectionPointOnAB, a = intersectionPointOnCA);
                        } else {
                            throw new RuntimeException("only one intersection");
                        }
                    } else if (intersectsBC) {
                        Vec2 intersectionPointOnBC = Vec2.lerp(b, c, intersectionBC);
                        if (intersectsCA) {
                            Vec2 intersectionPointOnCA = Vec2.lerp(c, a, intersectionCA);
                            c = a;
                            d = b;
                            emitTriangle(c, b = intersectionPointOnCA, a = intersectionPointOnBC);
                        } else {
                            throw new RuntimeException("only one intersection");
                        }
                    } else if (intersectsCA) {
                        throw new RuntimeException("only one intersection");
                    } else {
                        // nincs metszés
                    }
                } else { // trapezoid

                    // AB párhuzamos a lines[i]-ból induló egyenessel, ezért nem kell nézni metszést
                    double intersectionBC = intersect(b, c, lineStart);
                    boolean intersectsBC = intersectionBC >= 0 && intersectionBC <= 1;
                    double intersectionCD = intersect(c, d, lineStart);
                    boolean intersectsCD = intersectionCD >= 0 && intersectionCD <= 1;
                    double intersectionDA = intersect(d, a, lineStart);
                    boolean intersectsDA = intersectionDA >= 0 && intersectionDA <= 1;

                    if (intersectsBC) {
                        Vec2 intersectionPointOnBC = Vec2.lerp(b, c, intersectionBC);
                        if (intersectsCD) {
                            Vec2 intersectionPointOnCD = Vec2.lerp(c, d, intersectionCD);
                            emitTriangle(a, b, intersectionPointOnBC);
                            emitTriangle(a, intersectionPointOnBC, intersectionPointOnCD);
                            emitTriangle(a, intersectionPointOnCD, d);
                            a = intersectionPointOnCD;
                            b = intersectionPointOnBC;
                            // c marad
                            d = null;
                        } else if (intersectsDA) {
                            Vec2 intersectionPointOnDA = Vec2.lerp(d, a, intersectionDA);
                            emitTriangle(a, b, intersectionPointOnBC);
                            emitTriangle(a, intersectionPointOnBC, intersectionPointOnDA);
                            a = intersectionPointOnDA;
                            b = intersectionPointOnBC;
                            // c, d marad
                        } else {
                            throw new RuntimeException("only one intersection");
                        }
                    } else if (intersectsCD) {
                        Vec2 intersectionPointOnCD = Vec2.lerp(c, d, intersectionCD);
                        if (intersectsDA) {
                            Vec2 intersectionPointOnDA = Vec2.lerp(d, a, intersectionDA);
                            emitTriangle(a, b, c);
                            emitTriangle(a, c, intersectionPointOnCD);
                            emitTriangle(a, intersectionPointOnCD, intersectionPointOnDA);
                            a = intersectionPointOnDA;
                            b = intersectionPointOnCD;
                            c = d;
                            d = null;
                        } else {
                            throw new RuntimeException("only one intersection");
                        }
                    } else {
                        // nincs metszés
                    }
                }
            }

            if (d == null)
                emitTriangle(a, b, c);
            else {
                // a végén trapezoid maradt
                emitTriangle(a, c, d);
                emitTriangle(a, b, d);
            }
        }

        private void emitTriangle(Vec2 a, Vec2 b, Vec2 c) {
            out.ensureRemaining(Shaders.SolidPolygonShader.BYTES_PER_VERTEX * 3);
            out.put(a.plus(renderNodeTranslation));
            out.put(currentColor);
            out.put(b.plus(renderNodeTranslation));
            out.put(currentColor);
            out.put(c.plus(renderNodeTranslation));
            out.put(currentColor);
        }

        /**
         * @return [0; 1]-beli ha metszi a-b szakaszt
         */
        private double intersect(Vec2 edgeA, Vec2 edgeB, Vec2 perpendicularLineStart) {
            return ((edgeA.x() - perpendicularLineStart.x()) * perpendicularDirection.y() -
                    (edgeA.y() - perpendicularLineStart.y()) * perpendicularDirection.x()) /
                    ((edgeB.y() - edgeA.y()) * perpendicularDirection.x() -
                            (edgeB.x() - edgeA.x()) * perpendicularDirection.y());
        }
    }
}
