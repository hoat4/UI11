package ui11.platform.opengl;

import org.jspecify.annotations.Nullable;
import org.lwjgl.opengles.GLES;
import ui11.geom.Location;
import ui11.geom.Mat4;
import ui11.platform.opengl.context.EGLContext;
import ui11.platform.opengl.context.OpenGLContext;
import ui11.renderer.NativeWindowSurface;
import ui11.renderer.Renderer;
import ui11.renderer.layer.Layer;

import static org.lwjgl.egl.EGL12.eglWaitClient;
import static org.lwjgl.opengles.GLES20.*;

public class GLRenderer implements Renderer<GLLayer> {

    private final NativeWindowSurface surface;
    private final OpenGLContext context;

    private boolean initialized;
    private Shaders shaders;

    public GLRenderer(NativeWindowSurface surface) {
        this.surface = surface;
        this.context = new EGLContext(surface.nativeWindowHandle().address());
    }

    @Override
    public GLLayer createLayer(@Nullable Layer previous) {
        if (previous instanceof GLLayer glLayer)
            return glLayer;
        else
            return new GLLayer(new BufferPool(), surface.coordinateSpace());
    }

    @Override
    public void initializeRenderThreadLocals() {
        context.makeCurrent();
    }

    @Override
    public void render(GLLayer rootLayer, int viewportWidth, int viewportHeight) {
/*
                System.out.println("New render tree. Viewport size: "+innerSize.get());
        System.out.println(new Node.RenderTreePrinter().toString(root));
                System.out.println();
                 */

        eglWaitClient();

        if (!initialized) {
            GLES.createCapabilities();
            shaders = new Shaders();
            initialized = true;
        }

        glViewport(0, 0, viewportWidth, viewportHeight);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glClearColor(0f, .8f, .1f, 1);
        glClear(GL_COLOR_BUFFER_BIT);

        Mat4 initialTransform = new Mat4(
                2.0 / viewportWidth, 0, 0, -1,
                0, -2.0 / viewportHeight, 0, 1,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
        // TODO duplán van invertálva ez a mátrix
        Location.CoordinateSpace ndc = surface.coordinateSpace().withTransformation(initialTransform);
        rootLayer.render(new RenderingContext(shaders, ndc));

        // System.out.println("Uptime: "+ ManagementFactory.getRuntimeMXBean().getUptime());

        GLUtil.checkError();

        // WGL-nél a platform szálban kellett SwapBufferst hívni resizekor, hogy ne legyenek artifaktok.
        // itt úgy tűnik, mintha elég lenne a PaintThreadben hívni.

        this.context.swapBuffers();
    }
}
