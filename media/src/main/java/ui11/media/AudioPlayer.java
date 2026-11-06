package ui11.media;

import ui11.Widget;
import ui11.observable.ObservableList;

import java.util.function.Function;

import static ui11.graphics.effect.Overlay.overlay;

/**
 * It overlays the widget content with notification sounds when
 * {@linkplain AudioPlayerDelegate#play(Widget)} is called.
 */
public final class AudioPlayer extends Widget {

    private final Function<AudioPlayerDelegate, Widget> contentFunction;

    @Remember private ObservableList<Widget> playing;
    @Remember private AudioPlayerDelegate delegate;

    public AudioPlayer(Function<AudioPlayerDelegate, Widget> contentFunction) {
        this.contentFunction = contentFunction;
    }

    @Override
    protected void initState() {
        playing = new ObservableList<>();
        delegate = new AudioPlayerDelegate();
    }

    @Override
    protected Widget build() {
        return overlay(o -> {
            o.accept(contentFunction.apply(delegate));
            playing.forEach(o);
        });
    }

    public class AudioPlayerDelegate {

        private AudioPlayerDelegate() {
        }

        public void play(Widget audio) {
            playing.add(audio); // TODO add an identity key
            // TODO remove when no audio present on a widget
        }
    }
}
