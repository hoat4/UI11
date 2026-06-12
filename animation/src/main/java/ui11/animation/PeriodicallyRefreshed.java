package ui11.animation;

import ui11.*;
import ui11.observable.InvalidationPoint;

import java.time.Duration;
import java.util.function.Supplier;

public class PeriodicallyRefreshed extends Widget {

    private final Duration interval;
    private final Supplier<Widget> contentSupplier;

    @Inject private Scheduler scheduler;

    @Remember private InvalidationPoint ip;

    public PeriodicallyRefreshed(Duration interval, Supplier<Widget> contentSupplier) {
        this.interval = interval;
        this.contentSupplier = contentSupplier;
    }

    @Override
    protected void initState() {
        ip = new InvalidationPoint();
    }

    @Override
    protected void onResume() {
        requestLaterRefresh();
    }

    private void requestLaterRefresh() {
        // TODO interval megváltozásakor cancelelni kéne ezt?
        scheduler.scheduleOneTime(interval, () -> {
            ip.invalidate();
            requestLaterRefresh();
        }, untilPause());
    }

    @Override
    protected Widget build() {
        ip.subscribe();
        return contentSupplier.get();
    }
}

