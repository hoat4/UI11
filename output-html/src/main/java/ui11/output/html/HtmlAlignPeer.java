package ui11.output.html;

import ui11.Widget;
import ui11.output.html.util.UTF8Writer;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Alignment;

import static ui11.output.html.HTMLOutputUtil.bytes;

public class HtmlAlignPeer extends HtmlPeerBase {

    private static final byte[][] CLASS_ATTRS;
    private static final byte[] CLASS_FILL = bytes(" class=\"Fh FG");

    static {
        CLASS_ATTRS = new byte[Alignment.values().length][];
        CLASS_ATTRS[Alignment.TOP.ordinal()] = bytes(" class=\"Fh Fs FG");
        CLASS_ATTRS[Alignment.RIGHT.ordinal()] = bytes(" class=\"Fh fe");
        CLASS_ATTRS[Alignment.BOTTOM.ordinal()] = bytes(" class=\"Fh Fe FG");
        CLASS_ATTRS[Alignment.LEFT.ordinal()] = bytes(" class=\"Fh fs");
        CLASS_ATTRS[Alignment.HCENTER.ordinal()] = bytes(" class=\"Fh fc");
        CLASS_ATTRS[Alignment.VCENTER.ordinal()] = bytes(" class=\"Fh Fc FG");
        CLASS_ATTRS[Alignment.LEFT_TOP.ordinal()] = bytes(" class=\"Fh fs Fs");
        CLASS_ATTRS[Alignment.CENTER_TOP.ordinal()] = bytes(" class=\"Fh fc Fs");
        CLASS_ATTRS[Alignment.RIGHT_TOP.ordinal()] = bytes(" class=\"Fh fe Fs");
        CLASS_ATTRS[Alignment.LEFT_CENTER.ordinal()] = bytes(" class=\"Fh fs Fc");
        CLASS_ATTRS[Alignment.CENTER.ordinal()] = bytes(" class=\"Fh fc Fc");
        CLASS_ATTRS[Alignment.RIGHT_CENTER.ordinal()] = bytes(" class=\"Fh fe Fc");
        CLASS_ATTRS[Alignment.LEFT_BOTTOM.ordinal()] = bytes(" class=\"Fh fs Fe");
        CLASS_ATTRS[Alignment.CENTER_BOTTOM.ordinal()] = bytes(" class=\"Fh fc Fe");
        CLASS_ATTRS[Alignment.RIGHT_BOTTOM.ordinal()] = bytes(" class=\"Fh fe Fe");
    }

    private final Align align;

    public HtmlAlignPeer(Align align) {
        this.align = align;
    }

    @Override
    protected Widget build() {
        return makePeer(align.content(), child -> {
            return htmlElement((out, req) -> writeImpl(out, req, child));
        });
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req, WriteableHtmlElement child) {
        HTMLOutputUtil.beginLayoutDisplayModeElement(out, req);

        HTMLOutputUtil.writeStyleAttr(req, out);

        out.writeBytes(align.alignment() == null ? CLASS_FILL :
            CLASS_ATTRS[align.alignment().ordinal()]);
        HTMLOutputUtil.writeClassNames(req, out);
        out.writeBytes('"', '>');

        child.write(out, new HTMLElementRequirements());

        HTMLOutputUtil.endLayoutDisplayModeElement(out, req);
    }
}
