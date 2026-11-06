package ui11.audio.effect;

import ui11.SubstitutedWidget;

import java.time.Duration;

public final class Oscillator extends SubstitutedWidget {

    public double frequency;
    public Duration length;

    public Oscillator(double frequency, Duration length) {
        this.frequency = frequency;
        this.length = length;
    }
}
