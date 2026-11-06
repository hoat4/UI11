package ui11.media;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.window.Window;

import static ui11.graphics.effect.Overlay.overlay;

public class PlaySingleWavTest {
    static void main() {
        Window.open(overlay(
                new ColorFill(Color.YELLOW),
                new Audio(new MediaSource.URIMediaSource(PlaySingleWavTest.class.getResource("beep.wav")))
        ));
    }
}
