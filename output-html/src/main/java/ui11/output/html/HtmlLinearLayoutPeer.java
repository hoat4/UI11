package ui11.output.html;

import ui11.Widget;
import ui11.geom.Axis;
import ui11.geom.Length;
import ui11.layout.multichild.LinearLayout;
import ui11.output.html.HTMLElementRequirements.StyleProp;
import ui11.output.html.util.UTF8Writer;

import java.util.List;
import java.util.Set;

import static ui11.output.html.HTMLOutputUtil.bytes;

public class HtmlLinearLayoutPeer extends HtmlPeerBase {

    private static final byte[] CLASS_FH = bytes(" class=\"Fh");
    private static final byte[] CLASS_FV = bytes(" class=\"Fv");
    private static final byte[] STYLE_BEGIN = bytes("\" style=\"");
    private static final byte[] FLEX_GROW = bytes("flex-grow:");
    private static final byte[] GAP = bytes("gap:");

    private final LinearLayout linearLayout;

    public HtmlLinearLayoutPeer(LinearLayout linearLayout) {
        this.linearLayout = linearLayout;
    }

    @Override
    protected Widget build() {
        return makePeers(linearLayout.items(), Set.of(LinearLayout.WeightMarker.WeightRequest.INSTANCE),
            (children, otherResults) -> {
                List<? extends LinearLayout.WeightMarker> weights = (List<? extends LinearLayout.WeightMarker>)
                    otherResults.get(LinearLayout.WeightMarker.WeightRequest.INSTANCE);
                return htmlElement((out, req) ->
                    writeImpl(out, req, children, weights));
            });
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req,
                           List<? extends WriteableHtmlElement> childrenPeers,
                           List<? extends LinearLayout.WeightMarker> weights) {
        assert childrenPeers.size() == linearLayout.items().size();
        assert childrenPeers.size() == weights.size();

        HTMLOutputUtil.beginLayoutDisplayModeElement(out, req);

        out.writeBytes(linearLayout.mainAxis() == Axis.HORIZONTAL ? CLASS_FH : CLASS_FV);
        // TODO JustifyContent, AlignChildren

        boolean everyWeightIsZero = true;
        for (LinearLayout.WeightMarker weightMarker : weights) {
            if (weightMarker.weight() != 0) {
                everyWeightIsZero = false;
                break;
            }
        }

        if (everyWeightIsZero) {
            out.writeBytes(' ', 'F');
            out.writeByte('a');
        }

        HTMLOutputUtil.writeClassNames(req, out);

        Length gap = linearLayout.gap();
        boolean hasGap = !gap.isZero();
        //boolean hasPadding = !padding.isZero();
        if (req.styleProps != null || hasGap/* || hasPadding*/) {
            out.writeBytes(STYLE_BEGIN);
            if (hasGap) {
                out.writeBytes(GAP);
                HTMLOutputUtil.writeLength(gap, out);
                out.writeByte(';');
            }
            //HTMLOutputUtil.writePadding(padding, out);
            HTMLOutputUtil.writePassedStyleProps(req, out);
        }

        out.writeByte('"');
        out.writeBytes('\n', '>');

        for (int i = 0; i < childrenPeers.size(); i++) {
            HTMLElementRequirements req2 = new HTMLElementRequirements();
            double weight = weights.get(i).weight();
            if (weight != 0) {
                req2.styleProps = new StyleProp(req2.styleProps) {
                    @Override
                    public void write(UTF8Writer out) {
                        out.writeBytes(FLEX_GROW);
                        HTMLOutputUtil.writeDouble(weight, out);
                    }
                };
            }
            childrenPeers.get(i).write(out, req2);
        }

        HTMLOutputUtil.endLayoutDisplayModeElement(out, req);
    }
}
