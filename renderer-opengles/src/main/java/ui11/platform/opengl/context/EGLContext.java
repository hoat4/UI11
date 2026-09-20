package ui11.platform.opengl.context;

import org.lwjgl.PointerBuffer;
import org.lwjgl.egl.EGL;
import org.lwjgl.egl.EGL10;
import org.lwjgl.egl.EGL12;
import org.lwjgl.egl.KHRDebug;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static org.lwjgl.egl.EGL10.*;
import static org.lwjgl.egl.EGL10.EGL_ALPHA_SIZE;
import static org.lwjgl.egl.EGL10.EGL_BLUE_SIZE;
import static org.lwjgl.egl.EGL10.EGL_GREEN_SIZE;
import static org.lwjgl.egl.EGL10.EGL_NONE;
import static org.lwjgl.egl.EGL10.EGL_PBUFFER_BIT;
import static org.lwjgl.egl.EGL10.EGL_RED_SIZE;
import static org.lwjgl.egl.EGL10.EGL_SUCCESS;
import static org.lwjgl.egl.EGL10.EGL_SURFACE_TYPE;
import static org.lwjgl.egl.EGL10.EGL_TRUE;
import static org.lwjgl.egl.EGL10.EGL_WINDOW_BIT;
import static org.lwjgl.egl.EGL10.eglGetError;
import static org.lwjgl.egl.EGL10.eglMakeCurrent;
import static org.lwjgl.egl.EGL11.eglSwapInterval;
import static org.lwjgl.egl.EGL12.EGL_RENDERABLE_TYPE;
import static org.lwjgl.egl.EGL12.eglBindAPI;
import static org.lwjgl.egl.EGL13.EGL_CONTEXT_CLIENT_VERSION;
import static org.lwjgl.egl.EGL13.EGL_OPENGL_ES2_BIT;
import static org.lwjgl.egl.KHRDebug.*;
import static org.lwjgl.egl.KHRDebug.EGL_DEBUG_MSG_INFO_KHR;
import static org.lwjgl.egl.KHRNoConfigContext.EGL_NO_CONFIG_KHR;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

@SuppressWarnings("Since15") // IntelliJ bug
public class EGLContext implements OpenGLContext {

    private final long eglDisplay;
    private final long hwnd;
    private final long hdc;
    private final long windowConfig;
    private final long eglContext;
    private long eglSurface;
    private long eglGetSyncValuesCHROMIUM;

    public EGLContext(long hwnd) {
        if (hwnd == 0)
            throw new RuntimeException("null hwnd");

        this.hwnd = hwnd;
        this.hdc = org.lwjgl.system.windows.User32.GetDC(hwnd);

        eglDisplay = EGL10.eglGetDisplay(hdc);

        try (MemoryStack stack = stackPush()) {
            IntBuffer major = stack.mallocInt(1);
            IntBuffer minor = stack.mallocInt(1);

            if (!eglInitialize(eglDisplay, major, minor)) {
                throw new IllegalStateException(String.format("Failed to initialize EGL [0x%X]", eglGetError()));
            }

            EGL.createDisplayCapabilities(eglDisplay, major.get(0), minor.get(0));

            eglGetSyncValuesCHROMIUM = EGL.getFunctionProvider().getFunctionAddress("eglGetSyncValuesCHROMIUM");
        }

        List<String> eglCapabilitiesAsStrings = List.of(eglQueryString(eglDisplay, EGL_EXTENSIONS).split(" "));
        System.out.println("Capabilities all: " +
                eglCapabilitiesAsStrings.stream().map(s -> "\n- " + s).collect(Collectors.joining()));

        int[] configAttributes = new int[]{
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_ALPHA_SIZE, 8,
                EGL_RENDERABLE_TYPE,
                EGL_OPENGL_ES2_BIT,
                EGL_SURFACE_TYPE,
                EGL_PBUFFER_BIT | EGL_WINDOW_BIT,
                EGL_NONE
        };

        try (MemoryStack m = MemoryStack.stackPush()) {
            int debugResult = KHRDebug.eglDebugMessageControlKHR((error, command, messageType, threadLabel, objectLabel, message) -> {
                String commandStr = org.lwjgl.system.MemoryUtil.memUTF8(command);
                String messageStr = org.lwjgl.system.MemoryUtil.memUTF8(message);
                System.err.println("eglDebugMessageControlKHR " + error + ", " + commandStr + ", " + messageType
                        + ", " + threadLabel + ", " + objectLabel + ", " + messageStr);
            }, m.pointers(
                    EGL_DEBUG_MSG_CRITICAL_KHR, EGL_TRUE,
                    EGL_DEBUG_MSG_ERROR_KHR, EGL_TRUE,
                    EGL_DEBUG_MSG_WARN_KHR, EGL_TRUE,
                    EGL_DEBUG_MSG_INFO_KHR, EGL_TRUE,
                    EGL_NONE
            ));
            if (debugResult != EGL_SUCCESS)
                throw new RuntimeException(debugResult + ", " + eglGetError());
        }

        if (!eglBindAPI(EGL12.EGL_OPENGL_ES_API))
            throw new RuntimeException("eglBindAPI failed");
        System.out.println("eglBindAPI done");

        try (MemoryStack mem = stackPush()) {
            PointerBuffer windowConfigBuf = mem.mallocPointer(1);
            windowConfigBuf.put(0, EGL_NO_CONFIG_KHR);
            int[] windowConfigNum = new int[1];
            if (!eglChooseConfig(eglDisplay, configAttributes, windowConfigBuf, windowConfigNum))
                throw new RuntimeException("eglChooseConfig failed");
            if (windowConfigNum[0] <= 0)
                throw new RuntimeException("eglChooseConfig returned " + windowConfigNum[0] + " configs");
            windowConfig = windowConfigBuf.get(0);
            if (windowConfig == EGL_NO_CONFIG_KHR)
                throw new RuntimeException("eglChooseConfig returned EGL_NO_CONFIG_KHR");
            System.out.println("windowConfig: " + windowConfig);
        }


        int[] contextAttributes = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
        eglContext = eglCreateContext(eglDisplay, windowConfig, NULL, contextAttributes);

        MemorySegment waitHandleBuffer = Arena.ofAuto().allocate(ValueLayout.ADDRESS);
        waitHandleBuffer.setAtIndex(ValueLayout.ADDRESS, 0, MemorySegment.NULL);

        int[] surfaceAttributes = {
                0x33A5 /* EGL_DIRECT_COMPOSITION_ANGLE */, EGL_TRUE,
                EGL_NONE
        };

        eglSurface = eglCreateWindowSurface(eglDisplay, windowConfig, hwnd, surfaceAttributes);
        if (eglSurface == NULL)
            throw new RuntimeException("eglCreateWindowSurface failed: " + eglGetError());


        // TODO eglDestroySurface
    }

    @Override
    public void makeCurrent() {
        if (!eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext))
            throw new RuntimeException("eglMakeCurrent failed: " + eglGetError());
    }

    @Override
    public void swapBuffers() {
        if (!eglSwapInterval(eglDisplay, 0))
            throw new RuntimeException("eglSwapInterval failed: " + eglGetError());
        if (!eglSwapBuffers(eglDisplay, eglSurface))
            throw new RuntimeException("eglSwapBuffers failed: " + eglGetError());
    }

    /**
     * -1, ha nem elérhető
     *
     * @return
     */
    public long getVSyncCount() {
        try (MemoryStack m = MemoryStack.stackPush()) {
            LongBuffer ustBuf = m.longs(0), mscBuf = m.longs(0), sbcBuf = m.longs(0);
            if (JNI.invokePPPPPI(eglDisplay, eglSurface,
                    MemoryUtil.memAddress(ustBuf), MemoryUtil.memAddress(mscBuf), MemoryUtil.memAddress(sbcBuf),
                    eglGetSyncValuesCHROMIUM) == 0)
                return -1;

            long ust = ustBuf.get(), msc = mscBuf.get(), sbc = sbcBuf.get();
            System.out.println(ust + "/" + msc + "/" + sbc);
            return sbc;
        }
    }
}
