package ui.platform.glass.windows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.platform.glass.SchedulerImpl;
import ui11.observable.Scope;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.MemorySegment.NULL;

@SuppressWarnings("preview")
public class CompositorTimingThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(CompositorTimingThread.class);

    private final SchedulerImpl scheduler;
    private final AtomicInteger swapCounter = new AtomicInteger();
    private final AtomicInteger frameCounter = new AtomicInteger();

    public CompositorTimingThread(SchedulerImpl scheduler) {
        super("DirectComposition wait for clock thread");
        this.scheduler = scheduler;
    }

    @SuppressWarnings("preview")
    @Override
    public void run() {
        // TODO ha itt ez nem sikerül, akkor szólni kéne WindowImpl-nek. vagy csak folytatni Thread.sleepekkel a
        //      DCompositionWaitForCompositorClock helyett.

        try {
            while (true) {
                int result = DirectCompositionAPI.waitForCompositorClock(0, NULL, -1);
                if (result != 0 /* STATUS_SUCCESS */) {
                    // TODO ha túl sok van ebből, akkor lehet hogy kéne csinálni valamit.
                    //      de lehet hogy csak suspendelt a monitor.
                    logger.error("DCompositionWaitForCompositorClock failed: " + result);
                    Thread.sleep(10);
                }

                frameCounter.incrementAndGet();

                scheduler.scheduleAnimationFrame();
            }
        } catch (Throwable e) {
            logger.warn("DirectComposition clock synchronization failed, continuing with random phase 60Hz timer", e);

            try {
                scheduler.scheduleAtFixedRate(Duration.ofMillis(16), scheduler::doAnimationFrame, Scope.global());
            } catch (Throwable e2) {
                e2.addSuppressed(e);
                logger.error("Animation timer start failed", e2);
                // TODO ilyenkor szólni kéne az ablaknak hogy álljon le, mert használhatatlan
            }
        }
    }


}
