package ui.platform.glass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.renderer.Renderer;
import ui11.renderer.layer.Layer;

public class PaintThread<L extends Layer> extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(PaintThread.class);

    private final SchedulerImpl scheduler;
    private final Renderer<L> renderer;
    private long traceBegin;

    public PaintThread(Renderer<L> renderer, SchedulerImpl scheduler) {
        this.renderer = renderer;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        //System.load("C:\\Program Files\\Microsoft PIX\\2509.25\\WinPixGpuCapturer.dll");
        try {
            renderer.initializeRenderThreadLocals();

            traceBegin = System.nanoTime();

            while (true) {
                Frame<L> frame = scheduler.takeNextSubmittedFrame();

                trace("Run render task: " + frame);
                //addDebugItem(task);
                renderer.render(frame.rootLayer, frame.viewportWidth, frame.viewportHeight);

                // ezt lehet hogy a swapBuffers előtt kéne
                // TODO ha megváltozik közben a view méret, akkor nem is kéne várakozni (illetve a renderer.run-t is
                //      meg kéne szakítani)
                frame.renderDoneCallbacks.forEach(Frame.RenderDoneCallback::renderFinished);

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

    /*
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
         *

        SolidTrianglesItem debugItem = new SolidTrianglesItem(
                Mat4.IDENTITY, b.finish());
        displayList.items.add(debugItem);
    }
     */
}
