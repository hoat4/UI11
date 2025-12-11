package ui11.animation;

// ez nincs providolva statikus környezet (pl. HTML) esetén.
// régebben erre volt egy isStatic függvény benne (amikor még UIEnvironment volt az interface neve).

import ui11.observable.Scope;

import java.time.Duration;

public interface Scheduler {

    /**
     * Bejegyzi az adott feladatot futtatásra, amint az UI szálban jelenleg feldolgozott eventek (és egyéb feladatok)
     * futtatása befejeződött.
     */
    void runLater(Runnable task);

    /**
     * Bejegyzi a jelenlegi observert meghívásra a következő animációs frame-ben, tehát egy általában
     * képernyőfrissítésnyi idő múlva lesz meghívva újra.
     */
    void requestAnimationFrame();

    // abstract class Frame { }

    /**
     * @throws IllegalArgumentException ha delay negatív
     */
    void scheduleOneTime(Duration delay, Runnable task, Scope scope);

    /**
     * az elején is delayBetweenExecutions-nyi időt vár
     */
    void scheduleAtFixedRate(Duration delayBetweenExecutions,
                             Runnable task, Scope scope);
}
