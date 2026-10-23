package ui11.output.html;

import ui11.output.html.util.UTF8Writer;
import ui11.output.html.HTMLElementRequirements.StyleProp;
import ui11.color.Color;
import ui11.text.TextStyle;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static ui11.output.html.HTMLOutputUtil.bytes;

public class WriteableHtmlElement {

    private static final byte[] FONT_SIZE = bytes("font-size:");
    private static final byte[] FONT_FAMILY = bytes("font-family:");
    private static final byte[] COLOR = bytes("color:");

    private final Set<String> cssClasses;
    private final TextStyle textStyle;
    private final BiConsumer<UTF8Writer, HTMLElementRequirements> writeFunction;
    private final Supplier<Color> asColor;

    public WriteableHtmlElement(Set<String> cssClasses,
                                TextStyle textStyle,
                                BiConsumer<UTF8Writer, HTMLElementRequirements> writeFunction,
                                Supplier<Color> asColor) {
        this.cssClasses = cssClasses;
        this.textStyle = textStyle;
        this.writeFunction = writeFunction;
        this.asColor = asColor;
    }

    public final void write(UTF8Writer out, HTMLElementRequirements req) {
        // TODO wrap kezelése
        cssClasses.forEach(req::addCSSClass);

        emitTextStyle(textStyle, req);

        writeFunction.accept(out, req);
    }

    private void emitTextStyle(TextStyle ts, HTMLElementRequirements req) {
        req.styleProps = new StyleProp(req.styleProps) {
            @Override
            public void write(UTF8Writer out) {
                if (ts.size() != null) {
                    out.writeBytes(FONT_SIZE);
                    HTMLOutputUtil.writeDouble(ts.size(), out);
                    out.writeByte(';');
                }
                if (ts.color() != null) {
                    out.writeBytes(COLOR);
                    HTMLOutputUtil.writeColor(ts.color(), out);
                    out.writeByte(';');
                }
                if (ts.fontFamily() != null) {
                    out.writeBytes(FONT_FAMILY);
                    HTMLOutputUtil.attrValue(out, ts.fontFamily()); // TODO macskakörmök?
                    out.writeByte(';');
                }
                // TODO többi text style prop
            }
        };
    }

    /**
     * ha ez nem null, akkor lehet background-color vagy border-color értékébe berakni ahelyett hogy
     * DOM elemet generálnánk
     */
    public Color asColor() {
        return asColor.get();
    }
}
