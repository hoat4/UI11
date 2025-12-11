package ui11.animation;

// https://api.flutter.dev/flutter/animation/Tween-class.html

// TODO ne csak [0; 1]-ben lehessen
//      ez most JavaFX-nél is felmerült: https://mail.openjdk.org/pipermail/openjfx-dev/2025-June/054683.html

import static java.lang.Math.PI;

// TODO értelmesebb osztálynév. Flutterből szedtem ezt a jelenlegi nevet, de ők is valszeg rosszul használják
/**
 * An interpolation function between a beginning and ending value,
 * which returns the actual value for the current time. The is not measured in
 * regular time units, instead in [0, 1].
 */
@FunctionalInterface
public interface Tween<T> {

    /**
     * @param progress [0, 1]
     */
    T interpolate(T begin, T end, double progress);

    static Tween<Double> ofDouble() {
        return (begin, end, progress) -> end * progress + begin * (1 - progress);
    }

    /**
     * Az idő első felében a lejátssza a megadott mozgást, az idő
     * második felében pedig visszafele játssza le a megadott mozgást.
     */
    static <T> Tween<T> bidirectional(Tween<T> tween) {
        return (begin, end, t) -> t < 0.5 ?
                        tween.interpolate(begin, end, t * 2) :
                        tween.interpolate(begin, end, (1 - t) * 2);
    }

    static <T> Tween<T> ease(Tween<T> tween) {
        return (begin, end, progress) -> tween.interpolate(begin, end,
                (Math.sin(progress * PI - PI / 2) + 1) / 2);
    }

    // static Curve cubicBézier(double x1, double y1, double x2, double y2) {}

    // Curve EASE = cubicBézier(0.25, 0.1, 0.25, 1);
}