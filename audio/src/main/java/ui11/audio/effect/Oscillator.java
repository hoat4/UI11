package ui11.audio.effect;

import ui11.audio.AudioNode;

import java.time.Duration;

public final class Oscillator implements AudioNode {

    public double frequency;
    public Duration length;

    public Oscillator(double frequency, Duration length) {
        this.frequency = frequency;
        this.length = length;
    }
}
