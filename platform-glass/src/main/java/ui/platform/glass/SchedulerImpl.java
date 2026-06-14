package ui.platform.glass;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.animation.Scheduler;
import ui11.observable.InvalidationPoint;
import ui11.observable.Scope;
import ui11.platform.opengl.renderer.displaylist.DisplayList;

import java.time.Duration;
import java.util.concurrent.*;

// TODO animációhoz kéne valami időt (becsült present time) előállítani az aktuálisan generált frameről.
//      System.nanoTime vagy egyéb CPU idő nem jó, mert az ingadozik, nem igazodik a display frameratehez.
public class SchedulerImpl implements Scheduler {

    static final boolean TRACE_ANIMATION = true;
    private static final Logger logger = LoggerFactory.getLogger(WindowImpl.class);

    private static final ScheduledExecutorService eventQueue = Executors.newSingleThreadScheduledExecutor(
            task -> new Thread(task, "UI App Thread")); // TODO daemon?

    private final InvalidationPoint animationFrameIP = new InvalidationPoint();

    private final Object frameSubmitLock = new Object();
    private DisplayList submittedFrame;

    /**
     * csak UI szálból szabad meghívni
     */
    public void submitFrame(DisplayList displayList) {
        synchronized (frameSubmitLock) {
            if (submittedFrame != null) {
                displayList.renderDoneCallbacks.addAll(0, submittedFrame.renderDoneCallbacks);
                submittedFrame.renderDoneCallbacks.clear();
            }
            submittedFrame = displayList;
            frameSubmitLock.notifyAll();
        }
    }

    /**
     * paint szálból van meghívva
     */
    public DisplayList takeNextSubmittedFrame() throws InterruptedException {
        DisplayList frame;
        synchronized (frameSubmitLock) {
            while (submittedFrame == null)
                frameSubmitLock.wait();
            frame = submittedFrame;
            submittedFrame = null;
        }

        return frame;
    }

    public void runAndWait(Runnable task) throws ExecutionException, InterruptedException {
        eventQueue.submit(task).get();
    }

    public void scheduleAnimationFrame() {
        if (TRACE_ANIMATION)
            System.out.println((System.nanoTime() / 1000000) + " Schedule animation frame");
        eventQueue.submit(() -> {
            doAnimationFrame();
        });
    }

    public void doAnimationFrame() {
        if (TRACE_ANIMATION)
            System.out.println((System.nanoTime() / 1000000) + " Invalidate animationFrameIP");
        animationFrameIP.invalidate();
    }

    @Override
    public void runLater(Runnable task) {
        eventQueue.submit(wrapWithExceptionHandler(task));
    }

    @Override
    public void requestAnimationFrame() {
        // TODO ellenőrizzük, hogy UI szálból van-e meghívva

        animationFrameIP.subscribe();
    }

    @Override
    public void scheduleOneTime(Duration delay, Runnable task, Scope scope) {
        // TODO túl nagy delay kezelése. meg lent is.
        ScheduledFuture<?> f = eventQueue.schedule(wrapWithExceptionHandler(task), delay.toNanos(), TimeUnit.NANOSECONDS);
        scope.onClose(() -> {
            f.cancel(false);
        });
    }

    @Override
    public void scheduleAtFixedRate(Duration delayBetweenExecutions, Runnable task, Scope scope) {
        ScheduledFuture<?> f = eventQueue.scheduleAtFixedRate(wrapWithExceptionHandler(task),
                delayBetweenExecutions.toNanos(), delayBetweenExecutions.toNanos(), TimeUnit.NANOSECONDS);
        scope.onClose(() -> {
            f.cancel(false);
        });
    }

    private static Runnable wrapWithExceptionHandler(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable e) {
                logger.error("Uncaught exception in application thread", e);
            }
        };
    }
}
