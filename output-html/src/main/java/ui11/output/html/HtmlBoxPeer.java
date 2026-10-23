package ui11.output.html;

import ui11.output.html.util.UTF8Writer;
import ui11.Widget;
import ui11.decoration.Box;
import ui11.decoration.Box.BorderSpec;
import ui11.decoration.Box.BoxShadow;
import ui11.geom.Length;
import ui11.layout.LayoutSize;

import java.util.HashMap;
import java.util.Map;

import static ui11.output.html.HTMLOutputUtil.bytes;

public class HtmlBoxPeer extends HtmlPeerBase {

    private static final byte[] CLASS_WRAPPERELEMENT_AND_POSITION_REL = bytes(" class=\"we p");
    private static final byte[] CLASS_WRAPPERELEMENT = bytes(" class=\"we");
    private static final byte[] BORDER_SOLID_SPACE = bytes("border:solid ");
    private static final byte[] SEMICOLON_BORDER_WIDTH = bytes(";border-width:");
    private static final byte[] BACKGROUND = bytes("background:");
    private static final byte[] BORDER_RADIUS = bytes("border-radius:");
    private static final byte[] BOX_SHADOW = bytes("box-shadow:");
    private static final byte[] MIN_WIDTH = bytes("min-width:");
    private static final byte[] SEMICOLON_MIN_HEIGHT = bytes(";min-height:");
    private static final byte[] MAX_WIDTH = bytes("max-width:");
    private static final byte[] SEMICOLON_MAX_HEIGHT = bytes(";max-height:");

    private final Box box;

    public HtmlBoxPeer(Box box) {
        this.box = box;
    }

    @Override
    protected Widget build() {
        enum ChildType {
            CONTENT, BACKGROUND, BORDER_FILL
        }

        Map<ChildType, Widget> widgets = new HashMap<>();
        widgets.put(ChildType.CONTENT, box.content());
        if (box.background() != null)
            widgets.put(ChildType.BACKGROUND, box.background());
        if (box.border() != null && !box.border().thickness().isZero())
            widgets.put(ChildType.BORDER_FILL, box.border().fill());

        return makePeers(widgets, peers -> {
            WriteableHtmlElement content = peers.get(ChildType.CONTENT);
            WriteableHtmlElement background = peers.get(ChildType.BACKGROUND);
            WriteableHtmlElement borderFill  = peers.get(ChildType.BORDER_FILL);
            return htmlElement((out, req) ->
                writeImpl(out, req, content, background, borderFill));
        });
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req,
                           WriteableHtmlElement contentPeer, WriteableHtmlElement bgPeer,
                           WriteableHtmlElement borderFillPeer) {
        Box e = box;

        boolean needsPositionRelative = false;
        Widget bg = e.background();
        if (bg != null) {
            if (bgPeer.asColor() == null)
                needsPositionRelative = true;
        }

        HTMLElementType elementType = req.typeOrDiv();
        elementType.writeBeginElement(out);
        out.writeBytes(needsPositionRelative ? CLASS_WRAPPERELEMENT_AND_POSITION_REL : CLASS_WRAPPERELEMENT);
        HTMLOutputUtil.writeClassNames(req, out);
        out.writeByte('"');

        out.writeBytes(HTMLOutputUtil.STYLE_ATTR_BEGIN);
        HTMLOutputUtil.writePassedStyleProps(req, out);

        if (bgPeer != null && bgPeer.asColor() != null) {
            out.writeBytes(BACKGROUND);
            HTMLOutputUtil.writeColor(bgPeer.asColor(), out);
            out.writeByte(';');
        }

        BorderSpec border = e.border();
        if (border != null && !border.thickness().isZero()) {
            if (border.thickness().top().rel() != 0 ||
                    border.thickness().right().rel() != 0 ||
                    border.thickness().bottom().rel() != 0 ||
                    border.thickness().left().rel() != 0)
                throw new RuntimeException("TODO border with relative thickness: " + border);
            if (borderFillPeer.asColor() != null) {
                // TODO ha insets minden oldala egyezik, akkor bele lehetne írni borderba az inseteket
                out.writeBytes(BORDER_SOLID_SPACE);
                HTMLOutputUtil.writeColor(borderFillPeer.asColor(), out);
                out.writeBytes(SEMICOLON_BORDER_WIDTH);
                HTMLOutputUtil.writeLength(border.thickness().top(), out);
                out.writeByte(' ');
                HTMLOutputUtil.writeLength(border.thickness().right(), out);
                out.writeByte(' ');
                HTMLOutputUtil.writeLength(border.thickness().bottom(), out);
                out.writeByte(' ');
                HTMLOutputUtil.writeLength(border.thickness().left(), out);
                out.writeByte(';');
            } else {
                throw new RuntimeException("TODO border with non-color fill: " + border);
            }
        }

        Length cornerRadius = e.cornerRadius();
        if (cornerRadius.rel() != 0)
            throw new RuntimeException("TODO cornerRadius with non-zero relative part: " + cornerRadius);
        if (!cornerRadius.isZero()) {
            out.writeBytes(BORDER_RADIUS);
            HTMLOutputUtil.writeLength(cornerRadius, out); // TODO overflow:hidden
            out.writeByte(';');
        }

        BoxShadow shadow = e.boxShadow();
        if (shadow != null) {
            if (shadow.xOffset().rel() != 0 || shadow.yOffset().rel() != 0 ||
                    shadow.blur().rel() != 0 || shadow.spread().rel() != 0)
                throw new RuntimeException("TODO box shadow with relative size: " + shadow);
            out.writeBytes(BOX_SHADOW);
            HTMLOutputUtil.writeLength(shadow.xOffset(), out);
            out.writeByte(' ');
            HTMLOutputUtil.writeLength(shadow.yOffset(), out);
            out.writeByte(' ');
            HTMLOutputUtil.writeLength(shadow.blur(), out);
            out.writeByte(' ');
            HTMLOutputUtil.writeLength(shadow.spread(), out);
            out.writeByte(' ');
            HTMLOutputUtil.writeColor(shadow.color(), out);
            out.writeByte(';');
        }

        LayoutSize fixedSize = e.fixedSize();
        LayoutSize minSize = fixedSize;
        if (fixedSize != null) {
            // TODO bekavarhat display:block
            if (fixedSize.width() != null) {
                out.writeBytes(MAX_WIDTH);
                HTMLOutputUtil.writeLength(fixedSize.width(), out);
            }
            if (fixedSize.height() != null) {
                out.writeBytes(SEMICOLON_MAX_HEIGHT);
                HTMLOutputUtil.writeLength(fixedSize.height(), out);
            }
            out.writeByte(';');
        } else
            minSize = e.minSize();

        if (minSize != null) {
            if (minSize.width() != null) {
                out.writeBytes(MIN_WIDTH);
                HTMLOutputUtil.writeLength(minSize.width(), out);
            }
            if (minSize.height() != null) {
                out.writeBytes(SEMICOLON_MIN_HEIGHT);
                HTMLOutputUtil.writeLength(minSize.height(), out);
            }
            out.writeByte(';');
        }

        out.writeBytes('"', '>');

        if (needsPositionRelative)
            bgPeer.write(out, new HTMLElementRequirements().addCSSClass("P"));

        contentPeer.write(out, new HTMLElementRequirements());
        elementType.writeEndElement(out);
    }
}
