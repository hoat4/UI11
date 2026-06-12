package ui11.graphics.fill;

import ui11.resolution.SubstitutedWidget;
import ui11.color.Color;
import ui11.geom.Length;

import org.jspecify.annotations.NonNull;
import java.util.List;
import java.util.Objects;

import static ui11.graphics.fill.LinearGradient.Stop.uniformStops;

public final class LinearGradient extends SubstitutedWidget {

    private final double angleDeg;
    private final @NonNull List<@NonNull Stop> stops;

    public LinearGradient(double angleDeg, @NonNull List<@NonNull Stop> stops) {
        stops = List.copyOf(stops);
        this.angleDeg = angleDeg;
        this.stops = stops;
    }

    public static LinearGradient toTop(Stop... stops) {
        return new LinearGradient(0, List.of(stops));
    }

    public static LinearGradient toTop(Color... stops) {
        return new LinearGradient(0, uniformStops(stops));
    }

    public static LinearGradient toBottom(Stop... stops) {
        return new LinearGradient(180, List.of(stops));
    }

    public static LinearGradient toBottom(Color... colors) {
        return new LinearGradient(180, uniformStops(colors));
    }

    public static LinearGradient toLeft(Stop... stops) {
        return new LinearGradient(270, List.of(stops));
    }

    public static LinearGradient toLeft(Color... colors) {
        return new LinearGradient(270, uniformStops(colors));
    }

    public static LinearGradient toRight(Stop... stops) {
        return new LinearGradient(90, List.of(stops));
    }

    public static LinearGradient toRight(Color... stops) {
        return new LinearGradient(90, uniformStops(stops));
    }

    public double angleDeg() {
        return angleDeg;
    }

    public @NonNull List<@NonNull Stop> stops() {
        return stops;
    }

    public record Stop(@NonNull Color color, @NonNull Length pos) {

        public Stop {
            Objects.requireNonNull(color);
            Objects.requireNonNull(pos);
        }

        public static List<Stop> uniformStops(Color... colors) {
            Stop[] stops = new Stop[colors.length];
            double spaceForOneSegment = 1.0 / (colors.length - 1);
            for (int i = 0; i < colors.length; i++)
                stops[i] = new Stop(colors[i], Length.relative(spaceForOneSegment * i));
            return List.of(stops);
        }
    }
}
