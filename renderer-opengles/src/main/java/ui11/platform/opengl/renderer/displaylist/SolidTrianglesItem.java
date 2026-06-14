package ui11.platform.opengl.renderer.displaylist;

import ui11.geom.Mat4;
import ui11.platform.opengl.BufferPool;
import ui11.platform.opengl.renderer.GLUtil;
import ui11.platform.opengl.renderer.RenderingContext;
import ui11.platform.opengl.renderer.Shaders;

import static org.lwjgl.opengles.GLES20.*;

public class SolidTrianglesItem extends DisplayListItem {

    private final Mat4 transform;
    private final BufferPool.ReleaseableBuffer buffer;

    public SolidTrianglesItem(Mat4 transform, BufferPool.ReleaseableBuffer buffer) {
        this.transform = transform;
        this.buffer = buffer;
    }

    @Override
    public void execute(RenderingContext context) {
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

        glUniformMatrix4fv(shader.u_transform, false, transform.toColumnMajorFloatArray());
        GLUtil.checkError();

        glDrawArrays(GL_TRIANGLES, 0, buffer.buffer().limit() / Shaders.SolidPolygonShader.BYTES_PER_VERTEX);
        GLUtil.checkError();

        shader.unassignVertexAttributePointers();
    }
}
