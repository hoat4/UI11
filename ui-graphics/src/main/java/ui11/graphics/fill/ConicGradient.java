package ui11.graphics.fill;

import ui11.SubstitutedWidget;

import java.util.List;

public final class ConicGradient extends SubstitutedWidget {

    private final List<Stop> stops;

    public ConicGradient(List<Stop> stops) {
        if (stops.isEmpty())
            throw new IllegalArgumentException();
        stops = List.copyOf(stops);
        this.stops = stops;
    }

    public List<Stop> stops() {
        return stops;
    }

    @Override
    public String toString() {
        return "ConicGradient[" +
                "stops=" + stops + ']';
    }

    public record Stop(Color color, double degrees) {}
}
