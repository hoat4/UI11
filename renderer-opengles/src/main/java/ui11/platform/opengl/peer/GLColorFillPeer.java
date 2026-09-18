package ui11.platform.opengl.peer;

import ui11.Expose;
import ui11.Widget;
import ui11.geom.Vec2;
import ui11.graphics.fill.ColorFill;
import ui11.platform.opengl.BufferPool;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.renderer.Shaders;
import ui11.platform.opengl.rendertree.EmptyNode;
import ui11.platform.opengl.rendertree.FillTrianglesWithColorNode;

public class GLColorFillPeer extends Widget {

    private final ColorFill colorFill;

    @Inject private GLVisualContentRequest surface;
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
        Shape2D shape = surface.shape();
        Vec2 renderNodeTranslation = surface.renderNodeTranslation();

        if (shape == Shape2D.InfinitePlane.INFINITE_PLANE)
            return new Expose<>(surface, EmptyNode.INSTANCE);

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
                buf.put(a.plus(renderNodeTranslation));
                buf.put(colorInt);
                buf.put(b.plus(renderNodeTranslation));
                buf.put(colorInt);
                buf.put(c.plus(renderNodeTranslation));
                buf.put(colorInt);
            });
            node.vertices.set(buf.finish());
        }
        return new Expose<>(surface, node);
    }
}
