package ui11.output.html;

import ui11.Widget;
import ui11.output.html.util.UTF8Writer;
import ui11.geom.Location;
import ui11.text.Text;
import ui11.text.TextElementPeer;
import ui11.text.TextStyle;

import static ui11.output.html.HTMLOutputUtil.bytes;

class HtmlTextPeer extends HtmlPeerBase implements TextElementPeer {

    private static final byte[] CLASS_NOWRAP = bytes(" class=\"w");

    private final Text text;

    @Inject private TextStyle ts;

    public HtmlTextPeer(Text text) {
        this.text = text;
    }

    @Override
    protected Widget build() {
        return htmlElement(this::writeImpl);
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req) {
        HTMLElementType elementType = req.typeOrDiv();
        elementType.writeBeginElement(out);

        // először arra gondoltam, hogy elég akkor wrapper elemet rakni, ha
        // req.type != null || req.styleProps != null || req.cssClasses != null || hasTS || !e.wrapIfNeeded()
        // de valójában máskor, mert pl. LL esetén szövegenként kell külön elembe rakni a texteket.
        // flow esetén el lehetne hagyni, de akkor meg whitespace kell közéjük.
        // viszont align esetén meg jó lenne elhagyni. bár lehet hogy arra meg érdemes
        // akkor már külön logikát rakni Alignba.

        HTMLOutputUtil.writeStyleAttr(req, out);

        switch (ts.wrapping()) {
            case NEVER -> {
                out.writeBytes(CLASS_NOWRAP);
                HTMLOutputUtil.writeClassNames(req, out);
                out.writeByte('"');
            }
            case EVERYWHERE -> {
                HTMLOutputUtil.writeClassAttr(req, out);
            }
            // TODO Wrapping.BETWEEN_WORDS
            default -> {
                throw new RuntimeException("TODO " + ts.wrapping());
            }
        }

        out.writeByte('>');

        HTMLOutputUtil.writeEscapedText(out, text.text(), false);

        elementType.writeEndElement(out);
    }

    @Override
    public int pick(Location location) {
        throw new UnsupportedOperationException();
    }
}
