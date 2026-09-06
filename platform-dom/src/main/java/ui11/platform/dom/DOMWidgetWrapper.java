package ui11.platform.dom;

import ui11.Widget;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Size;
import ui11.graphics.VisualContentRequest;
import ui11.provide.Provider;
import ui11.text.TextStyle;

public class DOMWidgetWrapper extends Widget {

    private final Widget w;

    @Inject private TextStyle inheritedTextStyle;

    @Remember private ProxySurface proxySurface;

    public DOMWidgetWrapper(Widget w) {
        this.w = w;

        if (w != null && !Widget.class.isInstance(w))
            throw new RuntimeException("not a widget (DWW): " + w);
    }

    @Override
    protected void initState() {
        proxySurface = new ProxySurface();
    }

    @Override
    protected Widget build() {
        InheritedTextStyle inheritedTextStyle = new InheritedTextStyle(this.inheritedTextStyle);
        // InheritedTextStyle-t és CumulatingPropListet majd össze lehetne vonni
        return new Provider<>(CumulatingPropList.class, CumulatingPropList.CLEAR,
                new Provider<>(VisualContentRequest.class, proxySurface,
                        new Provider<>(InheritedTextStyle.class, inheritedTextStyle,
                                w
                        )
                )
        );
    }

    static class ProxySurface extends VisualContentRequest<DOMElementHolder> {

        VisualContentRequest s;

        protected ProxySurface() {
            super(DOMElementHolder.class);
        }

        @Override
        public Size size() {
            if (s == null)
                throw new IllegalStateException();
            return s.size();
        }

        @Override
        public CoordinateSpace coordinateSpace() {
            if (s == null)
                throw new IllegalStateException();
            return s.coordinateSpace();
        }

        // TODO equals/hashCode?
    }

    record InheritedTextStyle(TextStyle ts) {}
}
