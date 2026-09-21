package ui11.platform.opengl;

import ui11.color.Color;
import ui11.geom.Location;
import ui11.geom.Rect;
import ui11.geom.Shape;
import ui11.geom.Vec2;
import ui11.renderer.layer.Layer;

class GLLayerUpdater implements Layer.LayerUpdater {

    private final GLLayer layer;
    private final Location.CoordinateSpace coordinateSpace;
    private final BufferPool.GrowableVertexBuffer vertexBuffer;

    public GLLayerUpdater(GLLayer layer, BufferPool.GrowableVertexBuffer growableVertexBuffer) {
        this.layer = layer;
        this.coordinateSpace = layer.coordinateSpace();
        this.vertexBuffer = growableVertexBuffer;
    }

    @Override
    public void fill(Shape shape, Color color) {
        int colorInt = color.toSRGB().toRGBA(vertexBuffer.order());
        triangulate(shape, (a, b, c) -> {
            vertexBuffer.ensureRemaining(Shaders.SolidPolygonShader.BYTES_PER_VERTEX * 3);
            vertexBuffer.put(a);
            vertexBuffer.put(colorInt);
            vertexBuffer.put(b);
            vertexBuffer.put(colorInt);
            vertexBuffer.put(c);
            vertexBuffer.put(colorInt);
        });
    }

    @Override
    public void fillWithLinearGradient(Shape shape, Vec2 direction, Vec2[] lineStarts, Color[] colors) {
        LinearGradientTriangleSplitter triangleSplitter = new LinearGradientTriangleSplitter(
                direction.rotate90CounterClockwise(), lineStarts, colors, vertexBuffer);
        triangulate(shape, triangleSplitter);
    }

    @Override
    public void fill(Shape shape, Layer sublayer) {
        throw new RuntimeException("TODO");
    }

    @Override
    public void finish() {
        layer.setContent(vertexBuffer.finish());
    }

    private void triangulate(Shape shape, Triangle2DConsumer consumer) {
        Rect rect = shape.asRect(coordinateSpace);
        if (rect == null)
            throw new RuntimeException("TODO");

        consumer.accept(rect.topLeft(), rect.bottomRight(), rect.topRight());
        consumer.accept(rect.topLeft(), rect.bottomLeft(), rect.bottomRight());
    }

    @FunctionalInterface
    interface Triangle2DConsumer {

        void accept(Vec2 a, Vec2 b, Vec2 c);
    }
}
