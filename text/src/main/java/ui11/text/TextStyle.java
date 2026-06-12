package ui11.text;

import ui11.provide.Provider.Mergeable;
import ui11.color.Color;
import ui11.geom.Length;

import org.jspecify.annotations.Nullable;
import java.util.Objects;

// TODO az emSize lehet hogy nem is az em méretét jelöli
// TODO definiálni kéne hogy lineheightnál mit jelent ha százalékban adjuk meg. CSS-ben valszeg a font-size-hoz képesti
//      méret.
// TODO size-nak Double helyett Lengthnek kéne lennie (bár annál az em és a % is elég zavaros,
//      rem-mel már értelmesebb lehet)
// TODO alignmentnek nem itt kéne lennie, hanem csak Flowban.

public record TextStyle(@Nullable Color color,
                        @Nullable Double size,
                        @Nullable String fontFamily,
                        @Nullable TextAlign alignment,
                        @Nullable FontWeight weight,
                        @Nullable Wrapping wrapping,
                        @Nullable Boolean underline,
                        @Nullable Length lineHeight,
                        @Nullable Length letterSpacing,
                        @Nullable FontStyle fontStyle)
        implements Mergeable<TextStyle> {

    public static final TextStyle NULL = new TextStyle(null, null, null, null,
            null, null, null, null, null, null);

    public static TextStyle of(Color color, String fontFamily, double emSize) {
        return NULL.with(color, emSize, fontFamily);
    }

    public TextStyle withColor(Color color) {
        return new TextStyle(color, size, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withSize(double emSize) {
        return new TextStyle(color, emSize, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withFont(String fontFamily, double emSize) {
        return new TextStyle(color, emSize, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withFontFamily(String fontFamily) {
        return new TextStyle(color, size, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle with(Color color, double emSize, String fontFamily) {
        return new TextStyle(color, emSize, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withAlignment(TextAlign alignment) {
        return new TextStyle(color, size, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withWeight(FontWeight weight) {
        return new TextStyle(color, size, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withWrapping(Wrapping wrapping) {
        return new TextStyle(color, size, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withUnderline(boolean underline) {
        return new TextStyle(color, size, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withLineHeight(Length lineHeight) {
        return new TextStyle(color, size, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withLetterSpacing(Length letterSpacing) {
        return new TextStyle(color, size, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withFontStyle(FontStyle fontStyle) {
        return new TextStyle(color, size, fontFamily, alignment, weight, wrapping, underline, lineHeight, letterSpacing, fontStyle);
    }

    public TextStyle withBold() {
        return withWeight(FontWeight.BOLD);
    }

    public TextStyle leftAligned() {
        return withAlignment(TextAlign.LEFT);
    }

    public TextStyle centered() {
        return withAlignment(TextAlign.CENTER);
    }

    public TextStyle rightAligned() {
        return withAlignment(TextAlign.RIGHT);
    }

    @Override
    public TextStyle mergeWith(TextStyle defaults) {
        return new TextStyle(
                this.color == null ? defaults.color : color,
                this.size == null ? defaults.size : size,
                this.fontFamily == null ? defaults.fontFamily : fontFamily,
                this.alignment == null ? defaults.alignment : alignment,
                this.weight == null ? defaults.weight : weight,
                this.wrapping == null ? defaults.wrapping : wrapping,
                this.underline == null ? defaults.underline : underline,
                this.lineHeight == null ? defaults.lineHeight : lineHeight,
                this.letterSpacing == null ? defaults.letterSpacing : letterSpacing,
                this.fontStyle == null ? defaults.fontStyle : fontStyle
        );
    }

    public TextStyle diffTo(TextStyle o) {
        return new TextStyle(
                Objects.equals(color, o.color) ? null : color,
                Objects.equals(size, o.size) ? null : size,
                Objects.equals(fontFamily, o.fontFamily) ? null : fontFamily,
                Objects.equals(alignment, o.alignment) ? null : alignment,
                Objects.equals(weight, o.weight) ? null : weight,
                Objects.equals(wrapping, o.wrapping) ? null : wrapping,
                Objects.equals(underline, o.underline) ? null : underline,
                Objects.equals(lineHeight, o.lineHeight) ? null : lineHeight,
                Objects.equals(letterSpacing, o.letterSpacing) ? null : letterSpacing,
                Objects.equals(fontStyle, o.fontStyle) ? null : fontStyle
        );
    }

    public enum FontWeight {
        /**
         * 400
         */
        NORMAL,

        /**
         * 600
         */
        SEMI_BOLD,

        /**
         * 700
         */
        BOLD,

        /**
         * 900
         */
        HEAVY
    }

    public enum FontStyle {

        NORMAL,
        ITALIC,
        // TODO OBLIQUE
    }
    
    public enum Wrapping {
        NEVER, BETWEEN_WORDS, EVERYWHERE
    }
}
