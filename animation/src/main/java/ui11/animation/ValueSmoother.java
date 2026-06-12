package ui11.animation;

import ui11.observable.Observable;
import ui11.observable.ObservableList;
import ui11.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Olyan animáció, aminek nem egy előre kitalált kezdete és vége van, hanem egy érték változását próbáljuk elsimítani.
 */
public final class ValueSmoother<T> extends Component<T> {

    private final T targetValue;
    private final Duration duration;
    private final Tween<T> tween;

    @Inject private Scheduler scheduler;

    @Remember private List<Transition<T>> transitions;
    @Remember private T value;
    @Remember private boolean notFirst;

    public ValueSmoother(T targetValue, Duration duration, Tween<T> tween) {
        this.targetValue = targetValue;
        this.duration = duration;
        this.tween = tween;
    }

    @Override
    protected void initState() {
        transitions = new ObservableList<>();
    }

    @Override
    protected T update() {
        // TODO duration és tween menet közbeni valszeg nem supportáljuk

        set(targetValue);
        T t = get();

        if (!transitions.isEmpty()) {
            scheduler.requestAnimationFrame();
        }
        return t;
    }

    private void set(T value) {
        // TODO ez itt értelmetlennek tűnik, hogy minden változáskor hozzáadunk egy újabb transitiont.
        //      elég lenne egyszerre csak 1 transitionnek futnia.
        if (notFirst) {
            if (Objects.equals(value, this.value))
                return;
            transitions.add(new Transition<>(this.value, value, Instant.now()));
        } else
            notFirst = true;
        this.value = value;
    }

    private T get() {
        if (transitions.isEmpty())
            return value;

        Instant now = Instant.now();
        T v = transitions.get(0).begin;
        for (Iterator<Transition<T>> iterator = transitions.iterator(); iterator.hasNext(); ) {
            Transition<T> transition = iterator.next();
            double progress = (double) Duration.between(transition.start, now).toNanos() / duration.toNanos();
            if (progress > 1) {
                v = transition.end;
                iterator.remove();
                // ha nem leszünk meghívva a következő animation frame-ben, akkor
                // memory leak lesz abból, hogy bennmaradnak az előző értékek a transitions listában.
                // Double meg Color meg hasonlók esetén ez nyilván nem probléma,
                // de ha nagyobb objektumok lennének a transitionölt értékek, akkor már probléma.
            } else {
                progress = Math.min(1, progress);
                assert progress >= 0;
                v = tween.interpolate(v, transition.end, progress);
            }
        }
        return v;
    }

    private record Transition<T>(T begin, T end, Instant start) {}
}
