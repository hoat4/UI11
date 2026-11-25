package ui11.layout.impl;

import ui11.Widget;
import ui11.geom.*;
import ui11.layout.Insets;
import ui11.layout.helper.SingleChildLayout;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.singlechild.Padding;
import ui11.text.TextStyle;

public class DefaultPaddingImpl extends Widget {

    private final Padding padding;

    @Inject private TextStyle ts;

    public DefaultPaddingImpl(Padding padding) {
        this.padding = padding;
    }

    @Override
    protected Widget build() {
        Size allPadding = new Size(
                evalLen(padding.insets().sum(Axis.HORIZONTAL)),
                evalLen(padding.insets().sum(Axis.VERTICAL))
        );

        return new SingleChildLayout(padding.content(), new SingleChildLayout.SingleChildLayoutDelegate() {
            @Override
            public BoxConstraints computeChildConstraints(BoxConstraints containerConstraints) {
                return containerConstraints.subtract(allPadding);
            }

            @Override
            public Size computeContainerSize(BoxConstraints containerConstraints, Size childSize) {
                return childSize.add(allPadding);
            }

            @Override
            public Vec2 computeChildPosition(Size containerSize, Size childSize) {
                Insets pad = padding.insets();
                return Rect.of(containerSize).inset(evalLen(pad.top()), evalLen(pad.right()),
                        evalLen(pad.bottom()), evalLen(pad.left())).topLeft();
            }
        });
    }

    private double evalLen(Length l) {
        if (l.rel() != 0)
            throw new UnsupportedOperationException("TODO");
        return l.px() + l.em() * ts.size();
    }
}
