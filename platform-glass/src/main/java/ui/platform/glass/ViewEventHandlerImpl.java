package ui.platform.glass;

import com.sun.glass.events.ViewEvent;
import com.sun.glass.ui.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.platform.glass.windows.DirectCompositionAPI;
import ui11.platform.opengl.renderer.displaylist.DisplayList;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class ViewEventHandlerImpl extends View.EventHandler {

    private static final boolean TRACE_EVENTS = false;
    private static final Logger logger = LoggerFactory.getLogger(ViewEventHandlerImpl.class);

    private final WindowImpl window;
    private boolean ignoreRepaintAfterResize;

    public ViewEventHandlerImpl(WindowImpl window) {
        this.window = window;
    }

    @Override
    public void handleMouseEvent(View view, long time, int type, int button, int x, int y,
                                 int xAbs, int yAbs, int modifiers, boolean isPopupTrigger,
                                 boolean isSynthesized) {
    }

    @Override
    public void handleViewEvent(View view, long time, int type) {
        super.handleViewEvent(view, time, type);
        if (TRACE_EVENTS)
            System.out.println(ViewEvent.getTypeString(type) + " @ " + time);
        //Thread.dumpStack();
        switch (type) {
            case ViewEvent.REPAINT -> {
                if (ignoreRepaintAfterResize) {
                    ignoreRepaintAfterResize = false;
                    return;
                }
                // ennek nem kéne UI szálon keresztülmennie
                window.submitTask(() -> window.repaint());
            }
            case ViewEvent.RESIZE -> {
                int newWidth = view.getWidth();
                int newHeight = view.getHeight();

                CountDownLatch latch = new CountDownLatch(1);
                CountDownLatch platformThreadDoneLatch = new CountDownLatch(1);
                Object countDownLock = new Object();
                Boolean[] result = {null};
                if (SchedulerImpl.TRACE_ANIMATION)
                    System.out.println("Resized to " + newWidth + ", " + newHeight);
                window.onResize(new WindowImpl.ViewSize(newWidth, newHeight), new DisplayList.RenderDoneCallback() {
                    @Override
                    public void renderFinished() {
                        synchronized (countDownLock) {
                            if (latch.getCount() == 0)
                                return;
                            result[0] = true;
                            latch.countDown();
                        }
                    }

                    @Override
                    public void willNotRender() {
                        synchronized (countDownLock) {
                            result[0] = false;
                            latch.countDown();
                        }
                    }
                });
                ignoreRepaintAfterResize = true;

                try {
                    long waitBegin = System.nanoTime();
                    boolean shouldSwapBuffers;
                    try {
                        // 10ms volt, de animáció közbeni resizehoz az nem volt elég. majd meg kéne nézni, hogy miért.
                        latch.await(1000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    synchronized (countDownLock) {
                        if (latch.getCount() == 1) {
                            //if (SchedulerImpl.TRACE_ANIMATION)
                                System.out.println("Paint after resize timeout");
                            latch.countDown();
                            shouldSwapBuffers = false;
                        } else {
                            shouldSwapBuffers = result[0];
                            long t = System.nanoTime() - waitBegin;
                            if (SchedulerImpl.TRACE_ANIMATION)
                                System.out.println("Paint after resize (" + t / 1000 + " us): " + shouldSwapBuffers);
                            DirectCompositionAPI.flush();
                        }
                    }
                } finally {
                    platformThreadDoneLatch.countDown();
                }
            }
        }
    }
}
