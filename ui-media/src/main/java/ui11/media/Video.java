package ui11.media;

import ui11.SubstitutedWidget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Objects;

public final class Video extends SubstitutedWidget {

    @Nonnull private final URI source;
    private final boolean loop;
    @Nullable private final MediaResolution resolution;

    public Video(@Nonnull URI source, boolean loop) {
        this(source, loop, null);
    }

    public Video(@Nonnull URI source, boolean loop, @Nullable MediaResolution resolution) {
        this.source = Objects.requireNonNull(source);
        this.loop = loop;
        this.resolution = resolution;
    }

    @Nonnull
    public URI source() {
        return source;
    }

    public boolean loop() {
        return loop;
    }

    @Nullable
    public MediaResolution resolution() {
        return resolution;
    }

    public record MediaResolution(int width, int height) {}
}
