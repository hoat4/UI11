package ui11.media;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.media.MediaSource.InlineStringSource;
import ui11.media.MediaSource.TextualMediaSource;
import ui11.media.MediaSource.URIMediaSource;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

public final class SVGImageView extends SubstitutedWidget {

    private final TextualMediaSource source;
    private final boolean interactive;
    private final Map<String, ? extends Widget> embeddedWidgets;

    private SVGImageView(MediaSource.@NonNull TextualMediaSource source, boolean interactive,
                         @NonNull Map<@NonNull String, ? extends @NonNull Widget> embeddedWidgets) {
        Objects.requireNonNull(source);
        this.source = source;
        this.interactive = interactive;
        this.embeddedWidgets = Map.copyOf(embeddedWidgets);
    }

    public static SVGImageView from(TextualMediaSource svgSource) {
        return new SVGImageView(svgSource, false, Map.of());
    }

    public static SVGImageView fromURI(URI uri) {
        return from(new URIMediaSource(uri));
    }

    public static SVGImageView fromURL(URL uri) {
        return from(new URIMediaSource(uri));
    }

    public static SVGImageView fromURI(String uri) {
        return from(new URIMediaSource(uri));
    }

    public static SVGImageView fromString(String svgSource) {
        return from(new InlineStringSource(svgSource, "image/svg+xml"));
    }

    @Override
    protected SVGImageView forSubstitution() {
        return new SVGImageView(source, interactive, withID("embeddedWidgets", embeddedWidgets));
    }

    public TextualMediaSource source() {
        return source;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public @NonNull Map<@NonNull String, ? extends @NonNull Widget> embeddedWidgets() {
        return embeddedWidgets;
    }

    public SVGImageView withInteractivity(boolean interactive) {
        return new SVGImageView(source, interactive, embeddedWidgets);
    }

    /**
     * Az SVG fájlban szereplő {@code <foreignObject>}-ekbe lesznek helyezve a megadott {@linkplain Widget Widgetek}. A
     * map entry-k key-ei a {@code <foreignObject>}-ek ID-jainak felelnek meg.
     */
    public SVGImageView withEmbeddedWidgets(Map<String, ? extends Widget> embeddedWidgets) {
        return new SVGImageView(source, interactive, embeddedWidgets);
    }
}
