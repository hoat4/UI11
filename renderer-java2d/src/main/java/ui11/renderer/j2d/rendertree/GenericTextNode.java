// TODO

/*
package ui11.renderer.j2d.rendertree;

import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.renderer.input.InputNode;
import ui11.renderer.j2d.RenderingContext;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

public class GenericTextNode {

    public final MutableObservable<String> text = MutableObservable.ofNullable();
    public final MutableObservable<Font> font = MutableObservable.ofNullable();
    public final MutableObservable<J2DNode> child = MutableObservable.ofNullable();

    private String boundsForText;
    private Font boundsForFont;
    private int width, ascent, maxAscent, maxDescent;
    private TextLayout textLayout;

    @Override
    public boolean pick(InputNode.PickContext pickContext, Vec4 p) {
        String text = this.text.get();
        Font font = this.font.get();
        InputNode child = this.child.get();

        boolean fontChanged = !font.equals(boundsForFont);

        if (fontChanged || !text.equals(boundsForText)) {
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
 */