package ui11.media;

import ui11.resolution.SubstitutedWidget;
import ui11.animation.PlaybackController;
import ui11.media.ImageSource.InlineStringSource;
import ui11.media.ImageSource.TextualImageSource;
import ui11.media.ImageSource.URIImageSource;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

public class LottieView extends SubstitutedWidget {

    private final @NonNull TextualImageSource source;
    private final @Nullable PlaybackController playbackController;

    private LottieView(@NonNull TextualImageSource source, @Nullable PlaybackController playbackController) {
        Objects.requireNonNull(source);
        this.source = source;
        this.playbackController = playbackController;
    }

    public static LottieView from(TextualImageSource source) {
        return new LottieView(source, null);
    }

    public static LottieView fromURI(URI uri) {
        return from(new URIImageSource(uri));
    }

    public static LottieView fromURL(URL uri) {
        return from(new URIImageSource(uri));
    }

    public static LottieView fromURI(String uri) {
        return from(new URIImageSource(uri));
    }

    public static LottieView fromString(String svgSource) {
        return from(new InlineStringSource(svgSource, "video/lottie+json"));
    }

    public @NonNull TextualImageSource source() {
        return source;
    }

    public @Nullable PlaybackController playbackController() {
        return playbackController;
    }

    public LottieView withPlaybackController(PlaybackController playbackController) {
        return new LottieView(source, playbackController);
    }
}
