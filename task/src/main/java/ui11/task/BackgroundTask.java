package ui11.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Component;
import ui11.observable.InvalidationPoint;
import ui11.observable.MutableObservable;

import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.concurrent.*;

public class BackgroundTask<T> extends Component<TaskStatus<T>> {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundTask.class);
    private static final ExecutorService executor = Executors.newCachedThreadPool(
            Thread.ofPlatform().daemon().name("BackgroundTask thread pool - ", 1).factory());

    private final Callable<T> callable;

    // TODO ennek invalidálása nem thread-safe
    @Remember private MutableObservable<TaskStatus<T>> status;

    public BackgroundTask(@NonNull Callable<T> callable) {
        this.callable = Objects.requireNonNull(callable);
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
    protected TaskStatus<T> update() {
        return status.get();
    }
}
