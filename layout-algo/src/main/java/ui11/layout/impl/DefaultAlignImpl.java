package ui11.layout.impl;

import ui11.Widget;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.layout.helper.SingleChildLayout;
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
        return new SingleChildLayout(align.content(), new SingleChildLayout.SingleChildLayoutDelegate() {
            @Override
            public BoxConstraints computeChildConstraints(BoxConstraints containerConstraints) {
                BoxConstraints c2 = containerConstraints;
                Alignment alignment = align.alignment();
                if (alignment.horizSum != 0)
                    c2 = c2.loosenHorizontally();
                if (alignment.vertSum != 0)
                    c2 = c2.loosenVertically();
                return c2;
            }

            @Override
            public Size computeContainerSize(BoxConstraints containerConstraints, Size childSize) {
                return Size.max(containerConstraints.min(), childSize);
            }

            @Override
            public Vec2 computeChildPosition(Size containerSize, Size childSize) {
                double l = (containerSize.width() - childSize.width()) * align.alignment().leftFraction;
                double t = (containerSize.height() - childSize.height()) * align.alignment().topFraction;
                return new Vec2(l, t);
            }
        });
    }
}
