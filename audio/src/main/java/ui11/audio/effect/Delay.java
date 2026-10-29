package ui11.audio.effect;

import ui11.audio.AudioNode;

import java.time.Duration;

public final class Delay implements AudioNode {

    public AudioNode source;
    public Duration delay;

    public static AudioNode repeat(AudioNode source, Duration delayBetweenRepeats) {
        Delay d = new Delay();
        Merge combined = new Merge(source, d);
        d.source = combined;
        d.delay = delayBetweenRepeats;
        return combined;
    }
}
