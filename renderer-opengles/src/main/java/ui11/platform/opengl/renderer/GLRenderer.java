package ui11.platform.opengl.renderer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.egl.*;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import ui11.geom.Mat4;
import ui11.graphics.VisualContentRequest;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.context.EGLContext;
import ui11.platform.opengl.context.OpenGLContext;
import ui11.platform.opengl.renderer.displaylist.DisplayList;
import ui11.platform.opengl.renderer.displaylist.DisplayListItem;

import static java.util.Arrays.stream;
import static org.lwjgl.egl.KHRDebug.*;
import static org.lwjgl.egl.KHRNoConfigContext.EGL_NO_CONFIG_KHR;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.opengles.GLES;
import ui11.platform.opengl.rendertree.GLNode;
import ui11.renderer.NativeWindowSurface;
import ui11.renderer.Node;
import ui11.renderer.RenderableSurface;
import ui11.renderer.Renderer;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.egl.EGL12.*;
import static org.lwjgl.egl.EGL13.EGL_CONTEXT_CLIENT_VERSION;
import static org.lwjgl.egl.EGL13.EGL_OPENGL_ES2_BIT;
import static org.lwjgl.opengles.GLES20.*;

public class GLRenderer implements Renderer<GLNode, DisplayList> {

    private final NativeWindowSurface surface;
    private final OpenGLContext context;

    private boolean initialized;
    private Shaders shaders;

    public GLRenderer(NativeWindowSurface surface) {
        this.surface = surface;
        this.context = new EGLContext(surface.nativeWindowHandle().address());
    }

    @Override
    public VisualContentRequest<GLNode> createRootContentRequest() {
        return new GLVisualContentRequest(surface);
    }

    @Override
    public DisplayList prepare(GLNode root) {
        DisplayList displayList = new DisplayList(surface.width(), surface.height());
        Mat4 initialTransform = new Mat4(
                2.0 / displayList.viewportWidth, 0, 0, -1,
                0, -2.0 / displayList.viewportHeight, 0, 1,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
/*
                System.out.println("New render tree. Viewport size: "+innerSize.get());
                System.out.println(new J2DRenderTreePrinter().toString(rootRenderNode));
                System.out.println();
                 */

        System.out.println(new Node.RenderTreePrinter().toString(root));
        root.addToDisplayList(initialTransform, displayList);
        return displayList;
    }

    @Override
    public void initializeRenderThreadLocals() {
        context.makeCurrent();
    }

    @Override
    public void render(DisplayList displayList) {
        int w = displayList.viewportWidth, h = displayList.viewportHeight;

        eglWaitClient();

        if (!initialized) {
            GLES.createCapabilities();
            shaders = new Shaders();
            initialized = true;
        }

        glViewport(0, 0, w, h);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glClearColor(0f, .8f, .1f, 1);
        glClear(GL_COLOR_BUFFER_BIT);

        RenderingContext context = new RenderingContext(shaders);

        for (DisplayListItem item : displayList.items) {
            item.execute(context);
        }

        // System.out.println("Uptime: "+ ManagementFactory.getRuntimeMXBean().getUptime());

        GLUtil.checkError();

        // WGL-nél a platform szálban kellett SwapBufferst hívni resizekor, hogy ne legyenek artifaktok.
        // itt úgy tűnik, mintha elég lenne a PaintThreadben hívni.

        this.context.swapBuffers();
    }
}
