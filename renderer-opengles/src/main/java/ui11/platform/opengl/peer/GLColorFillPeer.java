package ui11.platform.opengl.peer;

import ui11.Expose;
import ui11.Widget;
import ui11.geom.Shape;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.fill.ColorFill;
import ui11.platform.opengl.BufferPool;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.renderer.Shaders;
import ui11.platform.opengl.rendertree.EmptyNode;
import ui11.platform.opengl.rendertree.FillTrianglesWithColorNode;

public class GLColorFillPeer extends Widget {

    private final ColorFill colorFill;

    @Inject private Surface surface;
    @Inject private GLVisualContentRequest request;
    @Inject private BufferPool bufferPool;

    @Remember private FillTrianglesWithColorNode node;

    public GLColorFillPeer(ColorFill colorFill) {
        this.colorFill = colorFill;
    }

    @Override
    protected void initState() {
        node = new FillTrianglesWithColorNode();
    }

    @Override
    protected Widget build() {
        Shape shape2 = surface.layoutShape();
        if (Shape.degenerateShape().equals(shape2))
            return new Expose<>(request, EmptyNode.INSTANCE);
        Shape2D shape = Shape2D.of(shape2, surface.coordinateSpace());

        node.shape.set(shape);

        if (colorFill.color().equals(ui11.color.Color.TRANSPARENT)) {
            node.vertices.set(null);
        } else {
            // vertex buffert csak akkor kéne újra előállítani, ha megváltozott a shape

            int estimatedVertexCount = shape.estimateTriangleCount() * 3;
            BufferPool.GrowableVertexBuffer buf = bufferPool.allocate(
                    estimatedVertexCount * Shaders.SolidPolygonShader.BYTES_PER_VERTEX);
            int colorInt = colorFill.color().toSRGB().toRGBA(buf.order());
            shape.toTriangles((a, b, c) -> {
                buf.ensureRemaining(Shaders.SolidPolygonShader.BYTES_PER_VERTEX * 3);
                buf.put(a);
                buf.put(colorInt);
                buf.put(b);
                buf.put(colorInt);
                buf.put(c);
                buf.put(colorInt);
            });
            node.vertices.set(buf.finish());
        }
        return new Expose<>(request, node);
    }
}
