package ui11.audio.effect;

import ui11.SubstitutedWidget;
import ui11.Widget;

import java.time.Duration;

public final class Delay extends SubstitutedWidget {

    public Widget source;
    public Duration delay;

    public static Widget repeat(Widget source, Duration delayBetweenRepeats) {
        Delay d = new Delay();
        Merge combined = new Merge(source, d);
        d.source = combined;
        d.delay = delayBetweenRepeats;
        return combined;
    }
}
