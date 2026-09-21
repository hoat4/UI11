package ui11.platform.opengl;


import ui11.color.Color;
import ui11.geom.Vec2;

class LinearGradientTriangleSplitter implements GLLayerUpdater.Triangle2DConsumer {

    private final Vec2 perpendicularDirection;
    private final Vec2[] lines;
    private final Color[] colors;
    private final BufferPool.GrowableVertexBuffer out;
    private int currentColor;

    public LinearGradientTriangleSplitter(Vec2 perpendicularDirection, Vec2[] lines, Color[] colors,
                            BufferPool.GrowableVertexBuffer out) {
        this.perpendicularDirection = perpendicularDirection;
        this.lines = lines;
        this.colors = colors;
        this.out = out;
    }

    @Override
    public void accept(Vec2 a, Vec2 b, Vec2 c) {
        Vec2 d = null; // 4. pontja a trapezoidnak a,b,c után

        int i = 0;
        for (; i < lines.length; i++) {
            currentColor = colors[i].toSRGB().toRGBA(out.order());

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
        out.put(a);
        out.put(currentColor);
        out.put(b);
        out.put(currentColor);
        out.put(c);
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