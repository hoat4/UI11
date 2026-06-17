package ui11.layout.impl;

import ui11.Widget;
import ui11.geom.Size;
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
            return new BoxLayoutResult.OfNoConstraints(content);

        Size size = sizeRequest.constraints().min();
        return new BoxLayoutResult.OfChosenSize(size, content);
    }
}
