package ui11.media;

import ui11.SubstitutedWidget;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.net.URI;
import java.util.Objects;

public final class Video extends SubstitutedWidget {

    private final @NonNull URI source;
    private final boolean loop;
    private final @Nullable MediaResolution resolution;

    public Video(@NonNull URI source, boolean loop) {
        this(source, loop, null);
    }

    public Video(@NonNull URI source, boolean loop, @Nullable MediaResolution resolution) {
        this.source = Objects.requireNonNull(source);
        this.loop = loop;
        this.resolution = resolution;
    }

    public @NonNull URI source() {
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
