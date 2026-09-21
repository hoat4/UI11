package ui11.platform.opengl;

import ui11.geom.Location;
import ui11.geom.Mat4;
import ui11.renderer.layer.Layer;

import static org.lwjgl.opengles.GLES20.*;
import static org.lwjgl.opengles.GLES20.GL_ARRAY_BUFFER;
import static org.lwjgl.opengles.GLES20.GL_STATIC_DRAW;
import static org.lwjgl.opengles.GLES20.GL_TRIANGLES;
import static org.lwjgl.opengles.GLES20.glBufferData;
import static org.lwjgl.opengles.GLES20.glDrawArrays;
import static org.lwjgl.opengles.GLES20.glUniformMatrix4fv;

public class GLLayer extends Layer {

    private final BufferPool bufferPool;
    private final Location.CoordinateSpace coordinateSpace;

    // TODO content módosításakor új render
    private BufferPool.ReleaseableBuffer content;

    public GLLayer(BufferPool bufferPool, Location.CoordinateSpace coordinateSpace) {
        this.bufferPool = bufferPool;
        this.coordinateSpace = coordinateSpace;
    }

    @Override
    public LayerUpdater createLayerUpdater() {
        return new GLLayerUpdater(this, bufferPool.allocate(100 /* ? */));
    }

    public Location.CoordinateSpace coordinateSpace() {
        return coordinateSpace;
    }

    public void setContent(BufferPool.ReleaseableBuffer content) {
        this.content = content;
    }

    public void render(RenderingContext context) {
        if (content == null)
            throw new IllegalStateException();

        BufferPool.ReleaseableBuffer buffer = this.content;
        Mat4 transformMat = context.ndcCoordinateSpace.transformationTo(this.coordinateSpace);

        Shaders.SolidPolygonShader shader = context.shaders.solidPolygonShader;

        shader.bindProgram();
        GLUtil.checkError();

        int vbo = glGenBuffers();
        GLUtil.checkError();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        GLUtil.checkError();

        glBufferData(GL_ARRAY_BUFFER, buffer.buffer(), GL_STATIC_DRAW);
        GLUtil.checkError();

        shader.assignVertexAttributePointers();

        GLUtil.checkError();

        glUniformMatrix4fv(shader.u_transform, false, transformMat.toColumnMajorFloatArray());
        GLUtil.checkError();

        glDrawArrays(GL_TRIANGLES, 0, buffer.buffer().limit() / Shaders.SolidPolygonShader.BYTES_PER_VERTEX);
        GLUtil.checkError();

        shader.unassignVertexAttributePointers();
    }
}
