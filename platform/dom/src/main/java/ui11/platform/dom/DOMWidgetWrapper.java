package ui11.platform.dom;

import ui11.Widget;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.observable.Observable;
import ui11.provide.Provider;
import ui11.text.TextStyle;

public class DOMWidgetWrapper extends Widget {

    private final Widget w;

    @Inject private Observable<TextStyle> inheritedTextStyle;

    @State private ProxySurface proxySurface;

    public DOMWidgetWrapper(Widget w) {
        this.w = w;
    }

    @Override
    protected void initState() {
        proxySurface = new ProxySurface();
    }

    @Override
    protected Widget build() {
        InheritedTextStyle inheritedTextStyle = new InheritedTextStyle(this.inheritedTextStyle.get());
        // InheritedTextStyle-t és CumulatingPropListet majd össze lehetne vonni
        return new Provider<>(CumulatingPropList.class, CumulatingPropList.CLEAR,
                new Provider<>(Surface.class, proxySurface,
                        new Provider<>(InheritedTextStyle.class, inheritedTextStyle,
                                w
                        )
                )
        );
    }

    static class ProxySurface implements Surface {

        Surface s;

        @Override
        public Size size() {
            if (s == null)
                throw new IllegalStateException();
            return s.size();
        }

        @Override
        public double devicePixelRatio() {
            if (s == null)
                throw new IllegalStateException();
            return s.devicePixelRatio();
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
