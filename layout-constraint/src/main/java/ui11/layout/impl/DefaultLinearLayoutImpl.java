package ui11.layout.impl;

import ui11.Widget;
import ui11.geom.Axis;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.layout.helper.MultiChildLayout;
import ui11.layout.helper.MultiChildLayout.MultiChildLayoutCallback;
import ui11.layout.helper.MultiChildLayout.MultiChildLayoutCallback.Placeable;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.multichild.LinearLayout.Item;
import ui11.layout.protocol.BoxConstraints;

import java.util.Objects;

public final class DefaultLinearLayoutImpl extends Widget {

    private final LinearLayout linearLayout;

    public DefaultLinearLayoutImpl(LinearLayout linearLayout) {
        this.linearLayout = Objects.requireNonNull(linearLayout);
    }

    @Override
    protected Widget build() {
        return new MultiChildLayout(this::doLayout);
    }

    private Size doLayout(BoxConstraints constraints, MultiChildLayoutCallback callback) {
        // TODO gap figyelembe vétele

        if (linearLayout.items().isEmpty())
            // heightnak 0-t számolna ki a lenti algoritmus szerint, ha nincs egyetlen elem se az LL-ben
            return constraints.min();

        Axis mainAxis = linearLayout.axis();
        Axis crossAxis = mainAxis.cross();
        int itemCount = linearLayout.items().size();
        double[] weights = new double[itemCount];
        double[] widths = new double[itemCount];
        double sumWidth = 0, sumWeight = 0;
        double height = 0;
        Placeable[] placeables = new Placeable[itemCount];
        boolean canFlex = constraints.max(mainAxis) != Double.POSITIVE_INFINITY;
        int i = 0;
        for (int indexInWidgets = 0; indexInWidgets < linearLayout.items().size(); indexInWidgets++) {
            Widget widget = linearLayout.items().get(indexInWidgets);
            double weight = Item.weight(widget);
            Placeable placeable = callback.asPlaceableOrNull(indexInWidgets, widget);
            if (placeable == null)
                continue;
            placeables[i] = placeable;
            weights[i] = weight;
            if (weight == 0 || !canFlex) {
                Size size = placeable.measure(BoxConstraints.of(
                        mainAxis,
                        0, constraints.min(crossAxis),
                        Double.POSITIVE_INFINITY, constraints.max(crossAxis)
                ));

                double mainLen = size.length(mainAxis);
                mainLen = Math.ceil(mainLen); // snap to pixel
                sumWidth += widths[i] = mainLen;
                height = Math.max(height, size.length(crossAxis));
            } else {
                sumWeight += weight;
            }
            i++;
        }
        itemCount = i;

        double containerWidth;

        if (canFlex) {
            if (sumWeight == 0) {
                double minWidth = constraints.min(mainAxis);
                double remaining = minWidth - sumWidth;
                if (remaining > 0) {
                    containerWidth = minWidth;
                    for (i = 0; i < itemCount; i++) {
                        // snap to pixel
                        double additionalWidth = Math.min(remaining, Math.ceil(remaining / (itemCount - i)));
                        widths[i] += additionalWidth;
                        remaining -= additionalWidth;
                    }
                } else
                    containerWidth = Math.min(constraints.max(mainAxis), sumWidth);
            } else {
                containerWidth = constraints.max(mainAxis);
                double remainingSpace = containerWidth - sumWidth;
                double remainingWeight = sumWeight;
                for (i = 0; i < itemCount; i++) {
                    if (weights[i] != 0) {
                        // snap to pixel
                        double additionalWidth = Math.min(remainingSpace, Math.ceil(remainingSpace * weights[i] / remainingWeight));
                        widths[i] += additionalWidth;
                        remainingSpace -= additionalWidth;
                        remainingWeight -= weights[i];
                        height = Math.max(height, placeables[i].measure(BoxConstraints.of(
                                mainAxis,
                                widths[i], constraints.min(crossAxis),
                                widths[i], constraints.max(crossAxis)
                        )).length(crossAxis));
                        // numerikus pontatlanságokkal kezdjünk majd valamit
                    }
                }
            }
        } else
            containerWidth = Math.min(constraints.max(mainAxis), sumWidth);

        double x = 0;
        for (i = 0; i < itemCount; i++) {
            double w = widths[i];
            placeables[i].placeAt(Rect.of(mainAxis, x, 0, w, height));
            x += w;
        }

        return Size.of(mainAxis, containerWidth, height);
    }
}
