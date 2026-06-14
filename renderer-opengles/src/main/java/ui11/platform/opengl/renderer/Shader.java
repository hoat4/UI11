package ui11.platform.opengl.renderer;

import org.lwjgl.opengles.GLES20;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengles.GLES20.*;

public abstract class Shader {

    public final int sortID;
    private final String name;
    private final VertexAttribute[] attributes;
    public final int vertexSize;

    public final int program;

    public Shader(int sortID, String name, VertexAttribute... attributes) {
        GLUtil.checkError(IllegalStateException::new);

        this.sortID = sortID;
        this.name = name;
        this.attributes = attributes;

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, readShaderSource(name + ".vert"));
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) != GL_TRUE)
            throw new RuntimeException("failed to compile vertex shader for '" + name + "': " +
                    glGetShaderInfoLog(vertexShader));

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, readShaderSource(name + ".frag"));
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) != GL_TRUE)
            throw new RuntimeException("failed to compile fragment shader for '" + name + "': " +
                    glGetShaderInfoLog(fragmentShader));

        GLUtil.checkError();

        program = glCreateProgram();
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        GLUtil.checkError();

        int vertexSize = 0;
        for (int i = 0; i < attributes.length; i++) {
            VertexAttribute attr = attributes[i];
            glBindAttribLocation(program, i, attr.name);

            vertexSize += attr.size;
        }
        this.vertexSize = vertexSize;

        glLinkProgram(program);

        GLUtil.checkError(err -> new RuntimeException("failed to link shader '" + name + "': " + err));

        if (glGetProgrami(program, GL_LINK_STATUS) != GL_TRUE)
            throw new RuntimeException("failed to link shader '" + name + "': " + GLES20.glGetProgramInfoLog(program));
    }

    public void bindProgram() {
        glUseProgram(program);
        GLUtil.checkError(err -> new RuntimeException("glUseProgram for '" + name + "' failed: " + err));
    }

    public void assignVertexAttributePointers() {
        GLUtil.checkError(IllegalStateException::new);

        int offset = 0;
        for (int i = 0; i < attributes.length; i++) {
            VertexAttribute attr = attributes[i];
            glVertexAttribPointer(i, attr.components, attr.type, true, vertexSize, offset);
            glEnableVertexAttribArray(i);
            offset += attr.size;

            GLUtil.checkError(err -> new RuntimeException(
                    "can't initialize vertex attribute '" + attr.name + "': " + err));
        }
    }

    public void unassignVertexAttributePointers() {
        GLUtil.checkError(IllegalStateException::new);

        for (int i = 0; i < attributes.length; i++)
            glDisableVertexAttribArray(i);

        GLUtil.checkError(err -> new RuntimeException(
                "can't unassign vertex attributes of '" + this + "': " + err));
    }

    private String readShaderSource(String filename) {
        try (InputStream in = getClass().getResourceAsStream(filename)) {
            if (in == null)
                throw new RuntimeException("shader not found: " + filename);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Can't load " + filename, e);
        }
    }

    public static class VertexAttribute {

        private final String name;
        private final int type;
        private final int components;
        private final int size;

        public VertexAttribute(String name, int components, int type) {
            this.name = name;
            this.type = type;
            this.components = components;
            this.size = components * switch (type) {
                case GL_FLOAT -> 4;
                case GL_UNSIGNED_BYTE -> 1;
                default -> throw new RuntimeException("unknown attribute type: " +
                        name + ", " + components + ", " + type);
            };
        }
    }
}
