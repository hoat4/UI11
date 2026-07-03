package ui11.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Widget;
import ui11.observable.MutableObservable;

import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;

public class BackgroundTask<T> extends Widget {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundTask.class);
    private static final ExecutorService executor = Executors.newCachedThreadPool(
            Thread.ofPlatform().daemon().name("BackgroundTask thread pool - ", 1).factory());

    private final Callable<T> callable;
    private final Function<TaskStatus<T>, Widget> contentFunction;

    // TODO ennek invalidálása nem thread-safe
    @Remember private MutableObservable<TaskStatus<T>> status;

    public BackgroundTask(@NonNull Callable<T> callable, Function<TaskStatus<T>, Widget> contentFunction) {
        this.callable = Objects.requireNonNull(callable);
        this.contentFunction = contentFunction;
    }

    // TODO ez így fura hogy Callable-nek csak az első értékét vesszük figyelembe, de nem tudom hogy mit kéne vele
    //      kezdeni

    @Override
    protected void onResume() {
        status.set(new TaskStatus.InProgress<>());
        Future<?> future = executor.submit(() -> {
            try {
                status.set(new TaskStatus.Success<>(callable.call()));
            } catch (Throwable e) {
                String callableToString;
                try {
                    callableToString = callable.toString();
                } catch (Throwable e2) {
                    e2.addSuppressed(e);
                    logger.error("Background task failed", e2);
                    status.set(new TaskStatus.Failure<>(e2));
                    return;
                }
                logger.error("Background task failed: " + callableToString, e);
                status.set(new TaskStatus.Failure<>(e));
            }
        });
        untilPause().onClose(() -> {
            future.cancel(true);
            status = null;
        });
    }

    @Override
    protected Widget build() {
        return contentFunction.apply(status.get());
    }
}
