package ui11.graphics.fill;

import ui11.SubstitutedWidget;
import ui11.geom.Length;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static ui11.graphics.fill.LinearGradient.Stop.uniformStops;
import static java.util.Arrays.stream;

public final class LinearGradient extends SubstitutedWidget {

    private final double angleDeg;
    @Nonnull private final List<Stop> stops;

    public LinearGradient(double angleDeg, List<Stop> stops) {
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

    @Nonnull
    public List<Stop> stops() {
        return stops;
    }

    public record Stop(@Nonnull Color color, @Nonnull Length pos) {

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
