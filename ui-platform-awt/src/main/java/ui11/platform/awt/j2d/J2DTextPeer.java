package ui11.platform.awt.j2d;

import ui11.Widget;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutProtocol;
import ui11.observable.Observable;
import ui11.provide.UpValueWrapper;
import ui11.text.Text;
import ui11.text.TextStyle;

import javax.annotation.Nonnull;
import java.awt.*;

public class J2DTextPeer extends Widget {

    private static final Canvas C = new Canvas(); // font metricshez

    private final Text text;

    @Inject private Observable<Surface> surface;
    @Inject private Observable<TextStyle> textStyle;

    @State private J2DTextPeerImpl state;

    public J2DTextPeer(Text text) {
        this.text = text;
    }

    @Override
    protected void initState() {
        state = new J2DTextPeerImpl();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected Widget build() {
        TextStyle textStyle = this.textStyle.get();

        // TODO wrapIfNeeded
        if (!text.equals(state.prevText) || !textStyle.equals(state.prevTextStyle)) {
            state.awtColor = J2DUtil.color(textStyle.color());
            state.awtFont = J2DTextPeerImpl.awtFont(textStyle);
            state.prevText = text;
            state.prevTextStyle = textStyle;
            ((J2DSurface) surface.get()).requestRepaint();
        }
        return new UpValueWrapper(state);
    }

    private static class J2DTextPeerImpl implements J2DPrimitive, BoxLayoutProtocol {

        private Text prevText;
        private TextStyle prevTextStyle;
        private Font awtFont;
        private Color awtColor;

        @Override
        public void draw(Graphics2D g, Rectangle bounds) {
        /*
        g.setColor(Color.BLUE);
        g.fill(bounds);
         */

            g.setFont(awtFont);
            g.setColor(awtColor);
            g.drawString(prevText.text(), bounds.x, bounds.y + g.getFontMetrics().getAscent());
        }

        @Nonnull
        private static Font awtFont(TextStyle ts) {
            Font font = new Font("Segoe UI", /*bold ? Font.BOLD : */Font.PLAIN, (int) (double) ts.size());
            if (ts.size() != (int) (double) ts.size())
                font = font.deriveFont((float) (double) ts.size());
            return font;
        }

        @Override
        public Size preferredSize(BoxConstraints constraints) {
            // WidgetInstantiation.lookup csinál ensureFresht, úgyhogy ez talán már nem kell: ensureFresh();
            FontMetrics fm = C.getFontMetrics(awtFont);
            double w = fm.stringWidth(prevText.text());
            double h = fm.getHeight();
            return constraints.clamp(new Size(w, h));
        }

        @Override
        public PickResult findInputRegion(Vec2 p) {
            return null;
        }
    }
}
