package ui11.renderer.j2d.rendertree;

import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.renderer.input.PickContext;
import ui11.renderer.j2d.RenderingContext;

import java.awt.*;

public class PaintedTextNode extends J2DNode {

    public final MutableObservable<String> text = MutableObservable.ofNullable();
    public final MutableObservable<Font> font = MutableObservable.ofNullable();
    public final MutableObservable<Paint> paint = MutableObservable.ofNullable();

    private String boundsForText;
    private Font boundsForFont;
    private int width, ascent, maxAscent, maxDescent, height;

    @Override
    public void render(RenderingContext ctx) {
        String text = this.text.get();
        Font font = this.font.get();
        Paint paint = this.paint.get();

        ensureBoundsInit(font, text);

        if (width == 0) // height gondolom nem lehet 0
            return;

        ctx.drawClipped(g2 -> {
            g2.setPaint(paint);
            g2.setFont(font);
            g2.transform(ctx.transform);
            g2.drawString(text, 0, maxAscent);
        });
    }

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        String text = this.text.get();
        Font font = this.font.get();

        ensureBoundsInit(font, text);

        if (Rect.of(new Size(width, height)).contains(p.to2D()))
            return pickContext.addResult();
        else
            return false;
    }

    private void ensureBoundsInit(Font font, String text) {
        // TODO FontMetricsnek utána kéne nézni, össze van kavarodva.
        //      java.awt.Component.getFontMetrics WFontMetricset ad vissza,
        //      Graphics.getFontMetrics viszont sun.font.FontDesignMetricset,
        //      és utóbbiban maxAscent ugyanannyi, mint ascent.
        boolean fontChanged = !font.equals(boundsForFont);

        if (fontChanged || !text.equals(boundsForText)) {
            FontMetrics fontMetrics = C.getFontMetrics(font);
            if (fontChanged) {
                maxAscent = fontMetrics.getMaxAscent();
                maxDescent = fontMetrics.getMaxDescent();
                ascent = fontMetrics.getAscent();
                boundsForFont = font;
                height = fontMetrics.getHeight();
            }
            width = fontMetrics.stringWidth(text);
            boundsForText = text;
        }
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("paint", paint.get());
        out.prop("text", text.get());
        out.prop("font", font.get());
    }

    private static final Canvas C = new Canvas();
}
