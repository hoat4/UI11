package ui11.platform.opengl.renderer;

import org.lwjgl.opengles.GLES20;

import java.util.function.Function;

public class GLUtil {

    public static void checkError() {
        int e = GLES20.glGetError();
        if (e != 0)
            throw new RuntimeException("GL error: " + e);
    }

    public static void checkError(Function<String, RuntimeException> exceptionSupplier) {
        int e = GLES20.glGetError();
        if (e != 0)
            throw exceptionSupplier.apply("GL error: " + e);
    }
}
