package ui11.platform.awt.j2d.peer;

import org.jspecify.annotations.NonNull;
import ui11.Widget;
import ui11.color.Color;
import ui11.geom.Size;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.J2DUtil;
import ui11.platform.awt.j2d.inputtree.OpaqueInputNode;
import ui11.platform.awt.j2d.rendertree.TextRenderNode;
import ui11.text.Text;
import ui11.text.TextStyle;

import java.awt.*;

public class J2DTextPeer extends Widget {

    private static final Canvas C = new Canvas(); // font metricshez

    private final Text text;
    private final J2DSurface surface;

    @Inject private TextStyle textStyle;
    @Inject private BoxLayoutResult.SizeRequest[] sizeRequests;

    @Remember private TextRenderNode node;
    @Remember private OpaqueInputNode inputNode;

    @Remember private String prevText;
    @Remember private TextStyle prevTextStyle;
    @Remember private Color prevColor;
    @Remember private Font prevFont;

    public J2DTextPeer(Text text, J2DSurface surface) {
        this.text = text;
        this.surface = surface;
    }

    @Override
    protected void initState() {
        node = new TextRenderNode();
        inputNode = new OpaqueInputNode();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected Widget build() {
        String text = this.text.text();

        // TODO wrapIfNeeded
        if (!text.equals(prevText))
            node.text.set(prevText = text);

        if (!textStyle.equals(prevTextStyle)) {
            if (!textStyle.color().equals(prevColor))
                node.paint.set(J2DUtil.color(prevColor = textStyle.color()));

            Font font = awtFont(textStyle);
            if (!font.equals(prevFont))
                node.font.set(prevFont = font);

            prevTextStyle = textStyle;
        }


        FontMetrics fm = C.getFontMetrics(prevFont);
        int w = fm.stringWidth(text);
        int h = fm.getHeight();

        inputNode.shape.set(new Rectangle(w, h));

        Widget result;
        if (surface == null)
            result = null;
        else
            result = surface.createResponse(new J2DNodeHolder(
                    node,
                    inputNode
            ));

        for (BoxLayoutResult.SizeRequest sizeRequest : sizeRequests) {
            Size size = sizeRequest.constraints().clamp(new Size(w, h));
            if (result == null)
                result = sizeRequest.createResponse(new BoxLayoutResult.OfChosenSize(size));
            else
                result = sizeRequest.createResponse(new BoxLayoutResult.OfChosenSize(size), result);
        }

        return result;
    }

    static @NonNull Font awtFont(TextStyle ts) {
        Font font = new Font("Segoe UI", /*bold ? Font.BOLD : */Font.PLAIN, (int) (double) ts.size());
        if (ts.size() != (int) (double) ts.size())
            font = font.deriveFont((float) (double) ts.size());
        return font;
    }
}
