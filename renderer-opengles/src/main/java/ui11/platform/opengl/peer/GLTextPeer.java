/*
package ui11.platform.opengl.peer;

import ui11.Widget;
import ui11.geom.Size;
import ui11.graphics.VisualContentRequest;
import ui11.color.Color;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.observable.Observable;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.J2DUtil;
import ui11.platform.opengl.inputtree.OpaqueInputNode;
import ui11.provide.UpValueWrapper;
import ui11.text.Text;
import ui11.text.TextStyle;

import javax.annotation.Nonnull;


public class GLTextPeer extends Widget {

    private static final Canvas C = new Canvas(); // font metricshez

    private final Text text;

    @Inject private Observable<VisualContentRequest> surface;
    @Inject private Observable<TextStyle> textStyle;
    @Inject(required = false) private Observable<BoxConstraints> constraints;

    @State private TextRenderNode node;
    @State private OpaqueInputNode inputNode;

    @State private String prevText;
    @State private TextStyle prevTextStyle;
    @State private Color prevColor;
    @State private Font prevFont;

    public GLTextPeer(Text text) {
        this.text = text;
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
        TextStyle textStyle = this.textStyle.get();

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

        Size preferredSize = constraints.get() == null ? null : constraints.get().clamp(new Size(w, h));
        return new UpValueWrapper(new BoxLayoutResult(preferredSize), new UpValueWrapper(new GLNodeHolder(
                node,
                inputNode
        )));
    }

    @Nonnull
    private static Font awtFont(TextStyle ts) {
        Font font = new Font("Segoe UI", /*bold ? Font.BOLD : * /Font.PLAIN, (int) (double) ts.size());
        if (ts.size() != (int) (double) ts.size())
            font = font.deriveFont((float) (double) ts.size());
        return font;
    }
}
*/