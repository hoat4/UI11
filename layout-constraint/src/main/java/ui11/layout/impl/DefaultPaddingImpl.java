package ui11.layout.impl;

import ui11.Widget;
import ui11.geom.Axis;
import ui11.geom.Length;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.layout.Insets;
import ui11.layout.helper.MultiChildLayout;
import ui11.layout.helper.MultiChildLayout.MultiChildLayoutCallback;
import ui11.layout.helper.MultiChildLayout.MultiChildLayoutCallback.Placeable;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.singlechild.Padding;
import ui11.observable.Observable;
import ui11.text.TextStyle;

public class DefaultPaddingImpl extends Widget {

    private final Padding padding;

    @Inject private Observable<TextStyle> ts;

    public DefaultPaddingImpl(Padding padding) {
        this.padding = padding;
    }

    @Override
    protected void initState() {
    }

    @Override
    protected Widget build() {
        return new MultiChildLayout(this::doLayout);
    }

    private Size doLayout(BoxConstraints constraints, MultiChildLayoutCallback callback) {
        Placeable contentPlaceable = callback.asPlaceable("content", padding.content());

        Size containerSize;
        Size allPadding = new Size(
                evalLen(padding.insets().sum(Axis.HORIZONTAL)),
                evalLen(padding.insets().sum(Axis.VERTICAL))
        );
        BoxConstraints childConstraints = constraints.subtract(allPadding);
        containerSize = contentPlaceable.measure(childConstraints).add(allPadding);
        containerSize = constraints.clamp(containerSize);

        Rect contentBounds = Rect.of(containerSize);

        Insets pad = padding.insets();
        if (!pad.isZero())
            contentBounds = contentBounds.inset(evalLen(pad.top()), evalLen(pad.right()),
                    evalLen(pad.bottom()), evalLen(pad.left()));

        contentPlaceable.placeAt(contentBounds);

        return containerSize;
    }

    private double evalLen(Length l) {
        if (l.rel() != 0)
            throw new UnsupportedOperationException("TODO");
        return l.px() + l.em() * ts.get().size();
    }
}
