package ui11.renderer.peer;

import ui11.Expose;
import ui11.Widget;
import ui11.geom.Size;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.renderer.TextRenderer;
import ui11.renderer.layer.Item;
import ui11.renderer.layer.TextItem;
import ui11.text.Text;
import ui11.text.TextStyle;

public class TextPeer extends Widget {

    private final Text text;

    @Inject(required = false) private Item.ItemRequest parentRequest;
    @Inject private TextStyle textStyle;
    @Inject private BoxLayoutResult.SizeRequest[] sizeRequests;
    @Inject private TextRenderer textRenderer;

    @Remember private TextRenderer.TextLayoutCalculator textLayoutCalculator;

    public TextPeer(Text text) {
        this.text = text;
    }

    @Override
    protected Widget build() {
        String text = this.text.text();

        textLayoutCalculator = textRenderer.createTextLayoutCalculator(textLayoutCalculator);

        // TODO wrapIfNeeded?
        TextRenderer.TextLayout textLayout = textLayoutCalculator.computeTextLayout(text, textStyle);

        Widget result;
        if (parentRequest == null)
            result = null;
        else
            result = new Expose<>(parentRequest, new TextItem(textLayout, parentRequest.surface.coordinateSpace()));

        for (BoxLayoutResult.SizeRequest sizeRequest : sizeRequests) {
            Size size = sizeRequest.constraints().clamp(textLayout.size());
            if (result == null)
                result = new Expose<>(sizeRequest, new BoxLayoutResult.OfChosenSize(size));
            else
                result = new Expose<>(sizeRequest, new BoxLayoutResult.OfChosenSize(size), result);
        }

        return result;
    }
}
