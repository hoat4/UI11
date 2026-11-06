package ui11.media;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;

import java.util.Objects;

public final class Audio extends SubstitutedWidget {

    private final MediaSource source;

    // loop is not a property, because if it were a property, it can't be composable with e.g. AudioPlayer and audio effects API
    // TODO in Video should we also eliminate the loop flag?
    //      but a Loop audio effect won't loop the video, because it's an audio effect, not a audio+graphics effect

    // TODO possible feature: extract a sound track from a video and play it in a different audio widget

    public Audio(@NonNull MediaSource source) {
        this.source = Objects.requireNonNull(source);
    }

    public @NonNull MediaSource source() {
        return source;
    }
}
