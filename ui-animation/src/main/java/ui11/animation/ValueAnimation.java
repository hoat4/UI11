package ui11.animation;

import ui11.observable.EventBus;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.Component;

import java.time.Duration;

/**
 * Egy kiinduló érték/állapot és egy célérték/állapot között mozgat egy értéket. A kiindulástól a célig.
 */
public class ValueAnimation<T> extends Component {

    public final EventBus<Void> onFinished = new EventBus<>();

    private final Duration duration;
    private final Tween<T> tween;
    private final T begin;
    private final T end;
    private final boolean infinite; // megfordul ha vége lett

    @Inject private Observable<Scheduler> scheduler;

    @State private boolean dir; // csak végtelenített esetén értelmezett
    @State private MutableObservable<Long> beginTime;

    public ValueAnimation(T begin, T end, Duration duration, Tween<T> tween) {
        this(begin, end, duration, tween, false);
    }

    public ValueAnimation(T begin, T end, Duration duration, Tween<T> tween,
                          boolean infinite) {
        this.duration = duration;
        this.tween = tween;
        this.begin = begin;
        this.end = end;
        this.infinite = infinite;
    }

    @Override
    protected void initState() {
        beginTime = MutableObservable.ofNullable();
    }

    /**
     * Elindítja az animációt. Innentől kezdve a {@link #value()} minden hívásra más értéket fog visszaadni. Az animáció
     * vége után meghívható újra, és animáció közben is (mindkét esetben újraindul az animáció).
     */
    // TODO ezt most nem lehet meghívni onstart előtt
    public void start() {
        dir = false;
        beginTime.set(System.nanoTime());

        // lehet inkább a refresht kéne a hívó helyett
        scheduler.get().requestAnimationFrame(); // TODO
    }

    // TODO azonos időpontot kéne használni minden frame-nél

    /**
     * Visszadja az animáció aktuális állapotához tartozó interpolált értéket. Ha nem fut az animáció, akkor a végső
     * értéket adja vissza.
     */
    public T value() {
        Long beginTime = this.beginTime.get();
        double progress = beginTime == null ?
                1 : (double) (System.nanoTime() - beginTime) / duration.toNanos();
        if (progress < 0)
            progress = 0;
        if (progress >= 1) {
            progress = 1;
            onFinished.post(null);
            if (infinite) {
                start();
                dir = !dir;
            }
        } else
            scheduler.get().requestAnimationFrame();
        if (dir)
            progress = 1 - progress;
        return tween.interpolate(begin, end, progress);
    }
}
