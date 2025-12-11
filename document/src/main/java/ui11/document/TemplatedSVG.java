package ui11.document;

import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Map;

public final class TemplatedSVG extends SubstitutedWidget {

    private final SVGTemplate svg;
    private final Map<String, ? extends Widget> embeddedWidgets;

    public TemplatedSVG(SVGTemplate svg, Map<String, ? extends Widget> embeddedWidgets) {
        this.svg = svg;
        this.embeddedWidgets = embeddedWidgets;
    }

    public SVGTemplate svg() {
        return svg;
    }

    public Map<String, ? extends Widget> embeddedWidgets() {
        return embeddedWidgets;
    }

    /**
     * {@code <foreignObject>}-eket tartalmazó SVG, amibe utólag {@linkplain Widget Widgeteket} helyezünk a {@code
     * <foreignObject>}-ek helyére
     */
    public record SVGTemplate(String source) {}
}
