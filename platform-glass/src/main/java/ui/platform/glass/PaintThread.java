package ui.platform.glass;

import com.sun.glass.ui.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.geom.Mat4;
import ui11.geom.Vec2;
import ui11.color.Color;
import ui11.platform.opengl.BufferPool;
import ui11.platform.opengl.renderer.GLRenderer;
import ui11.platform.opengl.renderer.Shaders;
import ui11.platform.opengl.renderer.displaylist.DisplayList;
import ui11.platform.opengl.renderer.displaylist.SolidTrianglesItem;

public class PaintThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(PaintThread.class);

    private final long hwnd;
    private final View view;
    private final SchedulerImpl scheduler;
    public volatile GLRenderer renderer;
    private long traceBegin;

    public PaintThread(View view, SchedulerImpl scheduler) {
        this.hwnd = view.getNativeView();
        this.view = view;
        this.scheduler = scheduler;
    }


    @Override
    public void run() {
        //System.load("C:\\Program Files\\Microsoft PIX\\2509.25\\WinPixGpuCapturer.dll");
        try {
            renderer = new GLRenderer(hwnd);
            renderer.traceSwaps = SchedulerImpl.TRACE_ANIMATION;

            traceBegin = System.nanoTime();

            while (true) {
                DisplayList task = scheduler.takeNextSubmittedFrame();

                trace("Run render task: " + task);
                //addDebugItem(task);
                renderer.render(task);
                renderer.swapBuffers();

                // ezt lehet hogy a swapBuffers előtt kéne
                // TODO ha megváltozik közben a view méret, akkor nem is kéne várakozni (illetve a renderer.run-t is
                //      meg kéne szakítani)
                task.renderDoneCallbacks.forEach(DisplayList.RenderDoneCallback::renderFinished);

                trace("Swapped");
            }
        } catch (Throwable e) {
            // TODO ablak bezárása
            logger.error("Paint thread failed", e);
        }
    }

    void trace(String msg) {
        System.out.println("[" + getTime(traceBegin) + "] " + msg);
    }

    private static long getTime(long begin) {
        return (System.nanoTime() - begin) / 1000000;
    }

    private void addDebugItem(DisplayList displayList) {
        BufferPool.GrowableVertexBuffer b = new BufferPool().allocate(12 * Shaders.SolidPolygonShader.BYTES_PER_VERTEX);
        b.put(new Vec2(-1, -1));
        b.put(Color.WHITE.toRGBA(b.order()));
        b.put(new Vec2(-1, 1));
        b.put(Color.WHITE.toRGBA(b.order()));
        b.put(new Vec2(1, -1));
        b.put(Color.WHITE.toRGBA(b.order()));
        b.put(new Vec2(1, 1));
        b.put(Color.WHITE.toRGBA(b.order()));
        b.put(new Vec2(-1, 1));
        b.put(Color.WHITE.toRGBA(b.order()));
        b.put(new Vec2(1, -1));
        b.put(Color.WHITE.toRGBA(b.order()));

        /*
        double width = (compositorTimingThread.currentFrame() % 100.0 / 100.0) / displayList.viewportWidth * 800;

        b.put(new Vec2(-1, -1));
        b.put(Color.RED.toRGBA(b.order()));
        b.put(new Vec2(-1, 1));
        b.put(Color.RED.toRGBA(b.order()));
        b.put(new Vec2(width * 2 - 1, -1));
        b.put(Color.RED.toRGBA(b.order()));
        b.put(new Vec2(width * 2 - 1, -1));
        b.put(Color.RED.toRGBA(b.order()));
        b.put(new Vec2(width * 2 - 1, 1));
        b.put(Color.RED.toRGBA(b.order()));
        b.put(new Vec2(-1, 1));
        b.put(Color.RED.toRGBA(b.order()));
         */

        SolidTrianglesItem debugItem = new SolidTrianglesItem(
                Mat4.IDENTITY, b.finish());
        displayList.items.add(debugItem);
    }
}
