package ui11.layout.impl;

import ui11.EndingWidget;
import ui11.Widget;
import ui11.geom.Size;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;

public class PreferredSizeIsMinimal extends Widget {

    private final Widget content;

    @Inject(required = false) private BoxLayoutResult.SizeRequest sizeRequest;

    public PreferredSizeIsMinimal(Widget content) {
        this.content = content;
    }

    @Override
    protected Widget build() {
        if (sizeRequest == null)
            return content;

        if (sizeRequest.constraints() == null)
            return EndingWidget.combine(content, new BoxLayoutResult.OfNoConstraints());

        Size size = sizeRequest.constraints().min();
        return EndingWidget.combine(content, new BoxLayoutResult.OfChosenSize(size));
    }
}
