package ui11.platform.awt.j2d.inputtree;

import ui11.geom.Vec2;
import ui11.observable.MutableObservable;
import ui11.platform.awt.j2d.RenderingContext;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

public class TextInputNode extends InputNode {

    public final MutableObservable<String> text = MutableObservable.ofNullable();
    public final MutableObservable<Font> font = MutableObservable.ofNullable();
    public final MutableObservable<InputNode> child = MutableObservable.ofNullable();

    private String boundsForText;
    private Font boundsForFont;
    private int width, ascent, maxAscent, maxDescent;
    private TextLayout textLayout;

    @Override
    public boolean pick(PickContext pickContext, Vec2 p) {
        String text = this.text.get();
        Font font = this.font.get();
        InputNode child = this.child.get();

        boolean fontChanged = !font.equals(boundsForFont);

        if (fontChanged || !text.equals(boundsForText)) {
            // TODO ez más FontMetrics (WFontMetrics), mint amit TextRenderNode használ (FontDesignMetrics),
            //      de nem értem hogy miért
            FontMetrics fontMetrics = C.getFontMetrics(font);
            if (fontChanged) {
                maxAscent = fontMetrics.getMaxAscent();
                maxDescent = fontMetrics.getMaxDescent();
                ascent = fontMetrics.getAscent();
                boundsForFont = font;
            }
            width = fontMetrics.stringWidth(text);
            boundsForText = text;
            textLayout = null;
        }

        if (p.x() < 0 || p.x() > width)
            return false;

        if (p.y() < ascent - maxAscent || p.y() > maxAscent + maxDescent)
            return false;

        if (textLayout == null)
            textLayout = new TextLayout(text, font, RenderingContext.FONT_RENDER_CONTEXT);

        if (!textLayout.getOutline(new AffineTransform()).contains(p.x(), p.y()))
            return false;

        return child.pick(pickContext, p);
    }

    private static final Canvas C = new Canvas();
}
