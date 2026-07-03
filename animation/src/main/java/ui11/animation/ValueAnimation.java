package ui11.animation;

import ui11.Widget;
import ui11.observable.MutableObservable;

import java.time.Duration;
import java.util.function.Function;

/**
 * Egy kiinduló érték/állapot és egy célérték/állapot között mozgat egy értéket. A kiindulástól a célig.
 */
public class ValueAnimation<T> extends Widget {

    private final T begin;
    private final T end;
    private final Duration duration;
    private final Tween<T> tween;
    private final boolean infinite; // megfordul ha vége lett
    private final Runnable onFinished;
    private final Function<T, Widget> contentFunction;

    @Inject private Scheduler scheduler;

    @Remember private boolean dir; // csak végtelenített esetén értelmezett
    @Remember private MutableObservable<Long> beginTime;

    public ValueAnimation(T begin, T end, Duration duration, Tween<T> tween, Function<T, Widget> contentFunction) {
        this(begin, end, duration, tween, false, contentFunction);
    }

    public ValueAnimation(T begin, T end, Duration duration, Tween<T> tween,
                          boolean infinite, Function<T, Widget> contentFunction) {
        this(begin, end, duration, tween, infinite, null, contentFunction);
    }

    public ValueAnimation(T begin, T end, Duration duration, Tween<T> tween,
                          boolean infinite, Runnable onFinished, Function<T, Widget> contentFunction) {
        this.duration = duration;
        this.tween = tween;
        this.begin = begin;
        this.end = end;
        this.infinite = infinite;
        this.onFinished = listenerProxy(onFinished);
        this.contentFunction = contentFunction;
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
    protected Widget build() {
        return contentFunction.apply(value());
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
