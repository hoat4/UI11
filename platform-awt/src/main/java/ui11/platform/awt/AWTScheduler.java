package ui11.platform.awt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.animation.Scheduler;
import ui11.observable.InvalidationPoint;
import ui11.observable.Scope;
import ui11.observable.Scope.ScopeAlreadyClosedException;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.concurrent.*;

public class AWTScheduler implements Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(AWTScheduler.class);

    public static final ThreadFactory SCHEDULER_THREAD_FACTORY = Thread.ofPlatform().
            name("AWTScheduler-", 1).daemon().factory();

    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor(SCHEDULER_THREAD_FACTORY);

    @Override
    public void runLater(Runnable task) {
        EventQueue.invokeLater(() -> {
            try {
                task.run();
            } catch (Throwable e) {
                // TODO task.toStringet is ki kéne írni, de az is dobhat exceptiont
                logger.error("Scheduled task threw exception", e);
            }
        });
    }

    @Override
    public void requestAnimationFrame() {
        InvalidationPoint ip = new InvalidationPoint();
        ip.subscribe();
        scheduleOneTime(Duration.ofMillis(10) /* TODO */, ip::invalidate, Scope.global());
    }

    @Override
    public void scheduleOneTime(Duration delay, Runnable task, Scope scope) {
        boolean[] cancel = {false};
        Runnable r = () -> {
            try {
                if (cancel[0])
                    return;
                runLater(task);
            } catch (Throwable e) {
                logger.error("Cannot submit scheduled task that reached time to start", e);
            }
        };
        long delayLong = delay.compareTo(Duration.ofHours(1)) > 0 ? delay.toMillis() : delay.toNanos();
        final TimeUnit delayTimeUnit = delay.compareTo(Duration.ofHours(1)) > 0 ? TimeUnit.MILLISECONDS : TimeUnit.NANOSECONDS;
        ScheduledFuture<?> future = scheduledExecutorService.schedule(r, delayLong, delayTimeUnit);
        try {
            scope.onClose(() -> {
                cancel[0] = true;
                future.cancel(false);
            });
        } catch (ScopeAlreadyClosedException scopeAlreadyClosedException) {
            future.cancel(false);
        }
    }

    @Override
    public void scheduleAtFixedRate(Duration delayBetweenExecutions, Runnable task, Scope scope) {
        throw new RuntimeException("TODO");
    }
}
