import ui11.audio.AudioDestination;
import ui11.audio.AudioNode;
import ui11.audio.effect.Delay;
import ui11.audio.effect.Merge;
import ui11.audio.effect.Oscillator;

import java.time.Duration;

public class BeepLoop {
    static void main() {
        Oscillator o = new Oscillator(500, Duration.ofMillis(500));
        AudioNode repeated = Delay.repeat(o, Duration.ofSeconds(1));
        AudioDestination.getDefaultSpeaker().play(repeated);
    }
}
