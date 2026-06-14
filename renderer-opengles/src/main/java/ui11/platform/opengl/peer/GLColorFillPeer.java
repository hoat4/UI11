package ui11.platform.opengl.peer;

import ui11.Widget;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.fill.ColorFill;
import ui11.platform.opengl.BufferPool;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.GLSurface;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.inputtree.OpaqueInputNode;
import ui11.platform.opengl.inputtree.TransparentInputNode;
import ui11.platform.opengl.renderer.Shaders;
import ui11.platform.opengl.rendertree.EmptyRenderNode;
import ui11.platform.opengl.rendertree.FillTrianglesWithColorRenderNode;

public class GLColorFillPeer extends Widget {

    private final ColorFill colorFill;

    @Inject private Surface surface;
    @Inject private BufferPool bufferPool;

    @Remember private FillTrianglesWithColorRenderNode node;
    @Remember private OpaqueInputNode inputNode;

    public GLColorFillPeer(ColorFill colorFill) {
        this.colorFill = colorFill;
    }

    @Override
    protected void initState() {
        node = new FillTrianglesWithColorRenderNode();
        inputNode = new OpaqueInputNode();
    }

    @Override
    protected Widget build() {
        GLSurface surface = (GLSurface) this.surface;
        Shape2D shape = surface.shape();
        Vec2 renderNodeTranslation = surface.renderNodeTranslation();

        if (shape == Shape2D.InfinitePlane.INFINITE_PLANE)
            return new GLNodeHolder(EmptyRenderNode.INSTANCE, TransparentInputNode.INSTANCE);

        inputNode.shape.set(shape);

        if (colorFill.color().equals(ui11.color.Color.TRANSPARENT))
            return new GLNodeHolder(EmptyRenderNode.INSTANCE, inputNode);

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
        return new GLNodeHolder(node, inputNode);
    }
}
