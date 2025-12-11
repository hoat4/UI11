package ui11.layout.impl;

import ui11.Widget;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.layout.helper.MultiChildLayout;
import ui11.layout.helper.MultiChildLayout.MultiChildLayoutCallback;
import ui11.layout.helper.MultiChildLayout.MultiChildLayoutCallback.Placeable;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Alignment;

public final class DefaultAlignImpl extends Widget {

    private final Align align;

    public DefaultAlignImpl(Align align) {
        this.align = align;
    }

    @Override
    protected Widget build() {
        return new MultiChildLayout(this::doLayout);
    }

    private Size doLayout(BoxConstraints constraints, MultiChildLayoutCallback callback) {
        BoxConstraints c2 = constraints;
        Alignment alignment = align.alignment();
        if (alignment.horizSum != 0)
            c2 = c2.loosenHorizontally();
        if (alignment.vertSum != 0)
            c2 = c2.loosenVertically();

        Placeable childPlaceable = callback.asPlaceable("content", align.content());
        Size childSize = childPlaceable.measure(c2);
        Size containerSize = Size.max(constraints.min(), childSize);

        assert constraints.isSatisfiedBy(containerSize);
        double l = (containerSize.width() - childSize.width()) * alignment.leftFraction;
        double t = (containerSize.height() - childSize.height()) * alignment.topFraction;
        double r = l + childSize.width();
        double b = t + childSize.height();

        // TODO ha nem egész a container mérete, akkor a childnak sem kéne valszeg erőltetni
        //      hogy egész mérete legyen
        l = Math.floor(l);
        t = Math.floor(t);
        r = Math.ceil(r);
        b = Math.ceil(b);

        childPlaceable.placeAt(Rect.ofTopRightBottomLeft(t, r, b, l));

        return containerSize;
    }
}
