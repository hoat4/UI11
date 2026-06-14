package ui11.platform.opengl.renderer;

import ui11.geom.Vec2;
import ui11.color.Color;

import java.nio.ByteBuffer;

import static org.lwjgl.opengles.GLES20.*;

public class Shaders {

    public final SolidPolygonShader solidPolygonShader = new SolidPolygonShader();

    public static class SolidPolygonShader extends Shader {

        public static final int BYTES_PER_VERTEX = 2 * 4 + 4;

        public final int u_transform;

        public SolidPolygonShader() {
            super(0, "solid_polygon",
                    new VertexAttribute("pos", 2, GL_FLOAT),
                    new VertexAttribute("color", 4, GL_UNSIGNED_BYTE)
            );

            u_transform = glGetUniformLocation(program, "u_transform");
        }

        public static String debugPrint(ByteBuffer b) {
            StringBuilder sb = new StringBuilder();
            while (b.hasRemaining()) {
                sb.append("\n- ");
                sb.append(new Vec2(b.getFloat(), b.getFloat())).append(" ").
                        append(Color.sRGBBytes(b.get(), b.get(), b.get(), b.get())).append("; ");
                sb.append(new Vec2(b.getFloat(), b.getFloat())).append(" ").
                        append(Color.sRGBBytes(b.get(), b.get(), b.get(), b.get())).append("; ");
                sb.append(new Vec2(b.getFloat(), b.getFloat())).append(" ").
                        append(Color.sRGBBytes(b.get(), b.get(), b.get(), b.get()));
            }
            return sb.toString();
        }
    }
}
