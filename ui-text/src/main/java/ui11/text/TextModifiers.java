package ui11.text;

import ui11.Widget;
import ui11.geom.Length;
import ui11.graphics.fill.Color;
import ui11.provide.Provider;
import ui11.text.TextStyle.FontStyle;
import ui11.text.TextStyle.FontWeight;

import java.util.Objects;

// TODO ellipsis
// TODO vízszintes igazítást ki kéne szedni TextStyleból,
//      helyette valami FlowAlign widgetbe osztályba rakni, függőleges igazítással együtt

/**
 * Contains methods which alter the appearance and layout of texts. The text style will be inherited to not
 * only the modified widget, but also to its descendants.
 * <p>
 * If there is already a text style property set in some ancestor widget, the deepest widget's (i.e. the most
 * nested) text style properties will override the text style properties of the ancestors.
 */
public class TextModifiers {

    private TextModifiers() {
        throw new Error();
    }

    /**
     * Wraps the specified widget so it will inherit the text style properties from the specified {@linkplain TextStyle}
     * object. If some text style property in the specified {@linkplain TextStyle} is null, then it will inherit
     * that property from the nearest ancestor that has that text style property non-null.
     */
    public static Widget withTextStyle(TextStyle textStyle, Widget content) {
        Objects.requireNonNull(textStyle);
        Objects.requireNonNull(content);
        return new Provider<>(TextStyle.class, textStyle, content);
    }

    /**
     * Wraps the specified widget so the text in it has {@link FontWeight#BOLD BOLD} font weight.
     */
    public static Widget bold(Widget content) {
        return withTextStyle(TextStyle.NULL.withWeight(FontWeight.BOLD), content);
    }

    /**
     * Wraps the specified widget so the text in it has {@link FontStyle#ITALIC ITALIC} font style.
     */
    public static Widget italic(Widget content) {
        return withTextStyle(TextStyle.NULL.withFontStyle(FontStyle.ITALIC), content);
    }

    /**
     * Wraps the specified widget so the text in it has {@link FontStyle#underline() underline} enabled.
     */
    public static Widget underlined(Widget content) {
        return withTextStyle(TextStyle.NULL.withUnderline(true), content);
    }

    /**
     * Wraps the specified widget so the text in it has the specified color.
     */
    public static Widget withTextColor(Color color, Widget content) {
        return withTextStyle(TextStyle.NULL.withColor(color), content);
    }

    /**
     * Wraps the specified widget so the text in it is laid out as the height of each text line is the specified line
     * height.
     */
    public static Widget withLineHeight(Length lineHeight, Widget content) {
        Objects.requireNonNull(lineHeight);
        return withTextStyle(TextStyle.NULL.withLineHeight(lineHeight), content);
    }

    /**
     * Wraps the specified widget so the text in it has the specified font family and font size.
     */
    public static Widget withFont(String fontName, double size, Widget content) {
        Objects.requireNonNull(fontName);
        return withTextStyle(TextStyle.NULL.withFontFamily(fontName).withSize(size), content);
    }

    /**
     * Wraps the specified widget so the text in it has the specified font size.
     */
    // TODO fontméret mértékegységek?
    public static Widget withFontSize(double fontSize, Widget content) {
        Objects.requireNonNull(content);
        return withTextStyle(TextStyle.NULL.withSize(fontSize), content);
    }

    /**
     * Wraps the specified widget so the text in it has the specified alignment.
     */
    public static Widget withTextAlignment(TextAlign alignment, Widget content) {
        Objects.requireNonNull(alignment);
        return withTextStyle(TextStyle.NULL.withAlignment(alignment), content);
    }

    /**
     * Wraps the specified widget so the text will wrap, if the container widget is too
     * narrow.
     */
    // TODO specifikálni kéne, hogy ez tetszőleges flow-ra vonatkozik, nem csak text-re
    // TODO milyen sortörési módok vannak? CSS white-space meg hasonló property-k specjét el kéne olvasni.
    //      majd dokumentálni hogy milyen karakterek esetén törik a sor.
    public static Widget withLineWrapping(Widget content) {
        return withTextStyle(TextStyle.NULL.withWrapIfNeeded(true), content);
    }

    /**
     * Wraps the specified widget so the text will won't break into more lines (except where the string has newline
     * characters), even if the container's size is too small.
     */
    // TODO dokumentálni, hogy milyen karaktereket tekintünk newlinenak,
    //      és hogy mi történik, ha kilóg a szöveg
    public static Widget withNoLineWrapping(Widget content) {
        return withTextStyle(TextStyle.NULL.withWrapIfNeeded(false), content);
    }
}
