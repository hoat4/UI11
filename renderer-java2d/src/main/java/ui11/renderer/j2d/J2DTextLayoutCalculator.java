package ui11.renderer.j2d;

import org.jspecify.annotations.NonNull;
import ui11.geom.Size;
import ui11.renderer.TextRenderer;
import ui11.renderer.TextRenderer.TextLayout;
import ui11.text.TextStyle;

import java.awt.*;

class J2DTextLayoutCalculator implements TextRenderer.TextLayoutCalculator {

    private static final Canvas C = new Canvas(); // only for font metrics, not for drawing

    private String prevText;
    private TextStyle prevTextStyle;
    private Font prevFont;
    private java.awt.Color awtColor;

    private J2DTextLayout cachedResult;

    @Override
    public @NonNull TextLayout computeTextLayout(@NonNull String text, @NonNull TextStyle textStyle) {
        boolean anythingChanged = false;

        if (!text.equals(prevText)) {
            prevText = text;
            anythingChanged = true;
        }

        if (!textStyle.equals(prevTextStyle)) {
            java.awt.Color c = J2DUtil.color(textStyle.color());
            if (!c.equals(awtColor)) {
                awtColor = c;
                anythingChanged = true;
            }

            Font font = awtFont(textStyle);
            if (!font.equals(prevFont)) {
                anythingChanged = true;
                prevFont = font;
                prevTextStyle = textStyle;
            }
        }
        assert prevText != null && prevFont != null;

        if (anythingChanged) {
            FontMetrics fm = C.getFontMetrics(prevFont);
            int w = fm.stringWidth(text);
            int h = fm.getHeight();
            int maxAscent = fm.getMaxAscent();
            cachedResult = new J2DTextLayout(text, prevFont, awtColor, w, h, maxAscent);
        }

        return cachedResult;
    }

    static @NonNull Font awtFont(TextStyle ts) {
        Font font = new Font("Segoe UI", /*bold ? Font.BOLD : */Font.PLAIN, (int) (double) ts.size());
        if (ts.size() != (int) (double) ts.size())
            font = font.deriveFont((float) (double) ts.size());
        return font;
    }

    static class J2DTextLayout implements TextLayout {

        final String text;
        final java.awt.Font font;
        final java.awt.Color color;
        final int width;
        final int height;
        final int maxAscent; // valamiért ezt használjuk ascent helyett, nem tudom hogy miért

        private J2DTextLayout(String text, Font font, Color color, int width, int height, int maxAscent) {
            this.text = text;
            this.font = font;
            this.color = color;
            this.width = width;
            this.height = height;
            this.maxAscent = maxAscent;
        }

        @Override
        public Size size() {
            return new Size(width, height);
        }
    }
}
