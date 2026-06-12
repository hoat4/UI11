package ui11.platform.awt.j2d.rendertree;

import ui11.observable.MutableObservable;
import ui11.platform.awt.j2d.RenderingContext;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class TextRenderNode extends RenderNode {

    public final MutableObservable<String> text = MutableObservable.ofNullable();
    public final MutableObservable<Font> font = MutableObservable.ofNullable();
    public final MutableObservable<Paint> paint = MutableObservable.ofNullable();

    private String boundsForText;
    private Font boundsForFont;
    private int width, ascent, maxAscent, maxDescent;

    @Override
    public void render(RenderingContext ctx) {
        String text = this.text.get();
        Font font = this.font.get();
        Paint paint = this.paint.get();

        // TODO FontMetricsnek utána kéne nézni, össze van kavarodva.
        //      java.awt.Component.getFontMetrics WFontMetricset ad vissza,
        //      Graphics.getFontMetrics viszont sun.font.FontDesignMetricset,
        //      és utóbbiban maxAscent ugyanannyi, mint ascent.
        boolean fontChanged = !font.equals(boundsForFont);

        if (fontChanged || !text.equals(boundsForText)) {
            ctx.g.setFont(font);
            FontMetrics fontMetrics = ctx.g.getFontMetrics();
            if (fontChanged) {
                maxAscent = fontMetrics.getMaxAscent();
                maxDescent = fontMetrics.getMaxDescent();
                ascent = fontMetrics.getAscent();
                boundsForFont = font;
            }
            width = fontMetrics.stringWidth(text);
            boundsForText = text;
        }

        if (width == 0) // height gondolom nem lehet 0
            return;

        ctx.drawClipped(g2 -> {
            g2.setPaint(paint);
            g2.transform(ctx.transform);
            g2.drawString(text, 0, maxAscent);
        });
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("paint", paint.get());
        out.prop("text", text.get());
        out.prop("font", font.get());
    }
}
