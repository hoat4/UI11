package ui11.output.html;

import ui11.output.html.util.UTF8Writer;
import ui11.color.Color;
import ui11.color.RGBColor;
import ui11.layout.Insets;
import ui11.output.html.HTMLElementRequirements.ClassList;
import ui11.output.html.HTMLElementRequirements.StyleProp;
import ui11.geom.Length;

import java.nio.charset.StandardCharsets;

public class HTMLOutputUtil {

    private static final byte[] CLASS_ATTR_BEGIN = bytes(" class=\"");
    static final byte[] STYLE_ATTR_BEGIN = bytes(" style=\"");
    private static final byte[] CALC_BEGIN = bytes("calc(");
    private static final byte[] EM_PLUS = bytes("em + ");
    private static final byte[] SPACE_PLUS_SPACE = bytes(" + ");
    private static final byte[] HEX_DIGITS = bytes("0123456789ABCDEF");
    private static final byte[] WRAPPER_ELEMENT_BEGIN_NO_CLASSES = bytes(" class=we>");
    private static final byte[] WRAPPER_ELEMENT_BEGIN = bytes(" class=\"we ");// TODO ez td-re is működjön
    static final byte[] PADDING = bytes("padding:");
    private static final byte[] CLOSE_DIV_AND_TD = bytes("</div></td\n>");

    static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    static void writeClassNames(HTMLElementRequirements req, UTF8Writer out) {
        for (ClassList c = req.cssClasses; c != null; c = c.next()) {
            out.writeByte(' ');
            attrValue(out, c.className());
        }
    }

    // UI4 is használja, azért publikus
    public static void attrValue(UTF8Writer out, String s) {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            switch (ch) {
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&#39;");
                    break;
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '\n':
                    out.append("&#10;");
                    break;
                case '\r':
                    if (i < s.length() - 1 && s.charAt(i + 1) == '\n')
                        i++;
                    out.append("&#10;");
                    break;
                default:
                    out.append(ch);
                    break;
            }
        }
    }

    // ez is kell UI4-nek
    public static void writeEscapedText(UTF8Writer out, String text, boolean multiline) {
        // HtmlUtility.replaceHtmlSignificantCharactersAndLineBreaks lassú lenne, mert berakja az eredményt egy Stringbe

        int size = text.length();
        int i = 0;

        while (i < size) {
            char ch = text.charAt(i++);

            switch (ch) {
                case '"':
                    out.append("&quot;");
                    break;
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '\n':
                    if (multiline)
                        out.append("<br>\r\n");
                    else
                        out.append(' ');
                    break;
                case '\r':
                    if (multiline)
                        out.append(' ');
                    else {
                        if (i < size && text.charAt(i) == '\n')
                            i++;
                        out.append("<br>\r\n");
                    }
                    break;
                case ' ':
                    // workaround arra hogy ": " végződésű label végén ne hagyja ki a szóközt
                    // majd nyilván meg kéne csinálni normálisan, hogy ne csak az utolsó karakterre működjön
                    if (i == size)
                        out.append("&nbsp;");
                    else
                        out.append(ch);
                    break;
                default:
                    out.append(ch);
                    break;
            }
        }
    }

    static void writeClassAttr(HTMLElementRequirements req, UTF8Writer out) {
        if (req.cssClasses != null) {
            out.writeBytes(CLASS_ATTR_BEGIN);
            writeClassNames(req, out);
            out.writeByte('"');
        }
    }

    public static void writeLength(Length len, UTF8Writer out) {
        // TODO double-knek nem kéne olyan sok számjegy

        if (len.em() == 0 && len.px() == 0 && len.rel() == 0) {
            out.writeByte('0');
            return;
        }
        if (len.em() == 0 && len.px() == 0) {
            out.write(Double.toString(len.rel() * 100));
            out.writeByte('%');
            return;
        }
        if (len.em() == 0 && len.rel() == 0) {
            out.write(Double.toString(len.px()));
            out.writeBytes('p', 'x');
            return;
        }
        if (len.px() == 0 && len.rel() == 0) {
            out.write(Double.toString(len.em()));
            out.writeBytes('e', 'm');
            return;
        }

        out.writeBytes(CALC_BEGIN);
        if (len.em() != 0) {
            out.write(Double.toString(len.em()));
            out.writeBytes(EM_PLUS); // "em + "
        }
        if (len.px() != 0) {
            out.write(Double.toString(len.px()));
            out.writeBytes('p', 'x');
        }
        if (len.rel() != 0) {
            if (len.px() != 0)
                out.writeBytes(SPACE_PLUS_SPACE);
            out.write(Double.toString(len.rel() * 100));
            out.writeByte('%');
        }
        out.writeByte(')');
    }

    public static void writeColor(Color color, UTF8Writer out) {
        RGBColor srgb = color.toSRGB(); // TODO
        out.writeByte('#');
        writeHexInt8(srgb.redAsInt8(), out);
        writeHexInt8(srgb.greenAsInt8(), out);
        writeHexInt8(srgb.blueAsInt8(), out);

        int alpha = srgb.alphaAsInt8();
        if (alpha != 255)
            writeHexInt8(alpha, out);
    }

    private static void writeHexInt8(int v, UTF8Writer out) {
        out.writeByte(HEX_DIGITS[v >>> 4]);
        out.writeByte(HEX_DIGITS[v & 7]);
    }

    public static HTMLElementType beginElementOrWrap(HTMLElementType elementType,
                                                     UTF8Writer out,
                                                     HTMLElementRequirements req) {
        if (req.type != null) {
            req.type.writeBeginElement(out);
            if (req.cssClasses == null && req.styleProps == null)
                out.writeBytes(WRAPPER_ELEMENT_BEGIN_NO_CLASSES);
            else {
                out.writeBytes(WRAPPER_ELEMENT_BEGIN);
                writeClassNames(req, out);
                out.writeByte('"');
                writeStyleAttr(req, out);
                out.writeByte('>');
                req.cssClasses = null;
                req.styleProps = null;
            }
        }

        elementType.writeBeginElement(out);
        return req.type;
    }

    public static void beginLayoutDisplayModeElement(UTF8Writer out, HTMLElementRequirements req) {
        if (req.type == HTMLElementType.TD) {
            req.type.writeBeginElement(out);
            if (req.cssClasses == null && req.styleProps == null)
                out.writeBytes(WRAPPER_ELEMENT_BEGIN_NO_CLASSES);
            else {
                out.writeBytes(WRAPPER_ELEMENT_BEGIN);
                writeClassNames(req, out);
                out.writeByte('"');
                writeStyleAttr(req, out);
                out.writeByte('>');
                req.cssClasses = null;
                req.styleProps = null;
            }
            HTMLElementType.DIV.writeBeginElement(out);
        }else
            req.typeOrDiv().writeBeginElement(out);
    }

    public static void endLayoutDisplayModeElement(UTF8Writer out, HTMLElementRequirements req) {
        if (req.type == HTMLElementType.TD)
            out.writeBytes(CLOSE_DIV_AND_TD);
        else
            req.typeOrDiv().writeEndElement(out);
    }

    public static void writeDouble(double d, UTF8Writer out) {
        out.write(Double.toString(d)); // TODO
    }

    public static void writeInt(int n, UTF8Writer out) {
        out.write(Integer.toString(n)); // TODO
    }

    public static void writePadding(Insets padding, UTF8Writer out) {
        if (!padding.isZero()) {
            out.writeBytes(PADDING);
            HTMLOutputUtil.writeLength(padding.top(), out);
            out.writeByte(' ');
            HTMLOutputUtil.writeLength(padding.right(), out);
            out.writeByte(' ');
            HTMLOutputUtil.writeLength(padding.bottom(), out);
            out.writeByte(' ');
            HTMLOutputUtil.writeLength(padding.left(), out);
            out.writeByte(';');
        }
    }

    public static void writePassedStyleProps(HTMLElementRequirements req, UTF8Writer out) {
        for (StyleProp prop = req.styleProps; prop != null; prop = prop.next) {
            prop.write(out);
            out.writeByte(';');
        }
    }

    public static void writeStyleAttr(HTMLElementRequirements req, UTF8Writer out) {
        if (req.styleProps != null) {
            out.writeBytes(STYLE_ATTR_BEGIN);
            writePassedStyleProps(req, out);
            out.writeByte('"');
        }
    }
}
