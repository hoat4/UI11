package ui11.media;

import org.jspecify.annotations.NonNull;
import ui11.MultiSlot;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.media.ImageSource.InlineStringSource;
import ui11.media.ImageSource.TextualImageSource;
import ui11.media.ImageSource.URIImageSource;

import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class SVGImageView extends SubstitutedWidget {

    private final TextualImageSource source;
    private final boolean interactive;
    private final Map<String, ? extends Widget> embeddedWidgets;

    @Inject private MultiSlot<String> embeddedWidgetSlots;

    private SVGImageView(@NonNull TextualImageSource source, boolean interactive,
                         @NonNull Map<String, ? extends @NonNull Widget> embeddedWidgets) {
        Objects.requireNonNull(source);
        this.source = source;
        this.interactive = interactive;
        this.embeddedWidgets = Map.copyOf(embeddedWidgets);
    }

    public static SVGImageView from(TextualImageSource svgSource) {
        return new SVGImageView(svgSource, false, Map.of());
    }

    public static SVGImageView fromURI(URI uri) {
        return from(new URIImageSource(uri));
    }

    public static SVGImageView fromURL(URL uri) {
        return from(new URIImageSource(uri));
    }

    public static SVGImageView fromURI(String uri) {
        return from(new URIImageSource(uri));
    }

    public static SVGImageView fromString(String svgSource) {
        return from(new InlineStringSource(svgSource, "image/svg+xml"));
    }

    public TextualImageSource source() {
        return source;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public Map<String, ? extends Widget> embeddedWidgets() {
        if (embeddedWidgetSlots == null)
            return embeddedWidgets;

        @SuppressWarnings("unchecked")
        Map.Entry<String, Widget>[] entries = new Map.Entry[embeddedWidgets.size()];
        embeddedWidgets.forEach(new BiConsumer<String, Widget>() {

            private int i;

            @Override
            public void accept(String name, Widget widget) {
                entries[i++] = Map.entry(name, widget.withSlot(embeddedWidgetSlots.get(name)));
            }
        });
        return Map.ofEntries(entries);
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
