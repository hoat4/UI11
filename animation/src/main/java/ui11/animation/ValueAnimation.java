package ui11.animation;

import ui11.observable.MutableObservable;
import ui11.Component;

import java.time.Duration;

/**
 * Egy kiinduló érték/állapot és egy célérték/állapot között mozgat egy értéket. A kiindulástól a célig.
 */
public class ValueAnimation<T> extends Component<T> {

    private final T begin;
    private final T end;
    private final Duration duration;
    private final Tween<T> tween;
    private final boolean infinite; // megfordul ha vége lett
    private final Runnable onFinished;

    @Inject private Scheduler scheduler;

    @Remember private boolean dir; // csak végtelenített esetén értelmezett
    @Remember private MutableObservable<Long> beginTime;

    public ValueAnimation(T begin, T end, Duration duration, Tween<T> tween) {
        this(begin, end, duration, tween, false);
    }

    public ValueAnimation(T begin, T end, Duration duration, Tween<T> tween,
                          boolean infinite) {
        this(begin, end, duration, tween, infinite, null);
    }

    public ValueAnimation(T begin, T end, Duration duration, Tween<T> tween,
                          boolean infinite, Runnable onFinished) {
        this.duration = duration;
        this.tween = tween;
        this.begin = begin;
        this.end = end;
        this.infinite = infinite;
        this.onFinished = listenerProxy(onFinished);
    }

    @Override
    protected void initState() {
        beginTime = MutableObservable.ofNullable();
    }

    @Override
    protected void onResume() {
        startAnimation();
    }

    @Override
    protected T update() {
        return value();
    }

    /**
     * Elindítja az animációt. Innentől kezdve a {@link #value()} minden hívásra más értéket fog visszaadni. Az animáció
     * vége után meghívható újra, és animáció közben is (mindkét esetben újraindul az animáció).
     */
    private void startAnimation() {
        dir = false;
        beginTime.set(System.nanoTime());

        // lehet inkább a refresht kéne a hívó helyett
        scheduler.requestAnimationFrame(); // TODO
    }

    // TODO azonos időpontot kéne használni minden frame-nél

    /**
     * Visszadja az animáció aktuális állapotához tartozó interpolált értéket. Ha nem fut az animáció, akkor a végső
     * értéket adja vissza.
     */
    private T value() {
        Long beginTime = this.beginTime.get();
        double progress = beginTime == null ?
                1 : (double) (System.nanoTime() - beginTime) / duration.toNanos();
        if (progress < 0)
            progress = 0;
        if (progress >= 1) {
            progress = 1;
            if (onFinished != null)
                onFinished.run();
            if (infinite) {
                startAnimation();
                dir = !dir;
            }
        } else
            scheduler.requestAnimationFrame();
        if (dir)
            progress = 1 - progress;
        return tween.interpolate(begin, end, progress);
    }
}
