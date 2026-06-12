package ui11.layout.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Widget;
import ui11.geom.Axis;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.layout.helper.MultiChildLayout;
import ui11.layout.helper.MultiChildLayout.MultiChildLayoutCallback;
import ui11.layout.helper.MultiChildLayout.MultiChildLayoutCallback.Placeable;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.multichild.LinearLayout.Item;
import ui11.layout.multichild.LinearLayout.JustifyContent;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Alignment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static ui11.graphics.Empty.empty;
import static ui11.layout.multichild.LinearLayout.expanded;
import static ui11.layout.multichild.LinearLayout.withWeight;

public final class DefaultLinearLayoutImpl extends Widget {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLinearLayoutImpl.class);

    private final LinearLayout linearLayout;

    public DefaultLinearLayoutImpl(LinearLayout linearLayout) {
        this.linearLayout = Objects.requireNonNull(linearLayout);
    }

    @Override
    protected Widget build() {
        return new MultiChildLayout(this::doLayout);
    }

    private static List<? extends Widget> alignChildren(List<? extends Widget> items, Alignment alignment) {
        return items.stream().map(w -> {
            if (w instanceof Item item)
                return withWeight(Item.weight(item), Align.align(alignment, item));
            else
                return Align.align(alignment, w);
        }).toList();
    }

    private Size doLayout(BoxConstraints constraints, MultiChildLayoutCallback callback) {
        // TODO gap figyelembe vétele

        List<? extends Widget> items = linearLayout.items();
        if (items.isEmpty())
            // heightnak 0-t számolna ki a lenti algoritmus szerint, ha nincs egyetlen elem se az LL-ben
            return constraints.min();

        items = switch (linearLayout.crossAxisAlignment()) {
            case STRETCH -> {
                // do nothing
                yield items;
            }
            case START -> alignChildren(items, switch (linearLayout.crossAxis()) {
                case VERTICAL -> Alignment.TOP;
                case HORIZONTAL -> Alignment.LEFT;
            });
            case CENTER -> alignChildren(items, switch (linearLayout.crossAxis()) {
                case VERTICAL -> Alignment.VCENTER;
                case HORIZONTAL -> Alignment.HCENTER;
            });
            case END -> alignChildren(items, switch (linearLayout.crossAxis()) {
                case VERTICAL -> Alignment.BOTTOM;
                case HORIZONTAL -> Alignment.RIGHT;
            });
        };

        items = switch (linearLayout.mainAxisAlignment()) {
            case STRETCH -> {
                // do nothing
                yield items;
            }
            case SPACE_EVENLY -> {
                List<Widget> l = new ArrayList<>();
                l.add(new Item(1, empty()));
                for (Widget w : items) {
                    l.add(new Item(0, Item.content(w)));
                    l.add(new Item(1, empty()));
                }
                yield l;
            }
            case SPACE_AROUND -> {
                List<Widget> l = new ArrayList<>();
                for (Widget w : items) {
                    l.add(new Item(1, empty()));
                    l.add(new Item(0, Item.content(w)));
                    l.add(new Item(1, empty()));
                }
                yield l;
            }
            case SPACE_BETWEEN -> {
                List<Widget> l = new ArrayList<>();
                Iterator<? extends Widget> iterator = items.iterator();

                l.add(new Item(0, Item.content(iterator.next())));
                while (iterator.hasNext()) {
                    Widget w = iterator.next();
                    l.add(new Item(1, empty()));
                    l.add(new Item(0, Item.content(w)));
                }
                yield l;
            }
            case START, CENTER, END -> {
                List<Widget> l = new ArrayList<>();
                if (linearLayout.mainAxisAlignment() != JustifyContent.START)
                    l.add(expanded(empty()));
                for (Widget w : items)
                    l.add(withWeight(0, Item.content(w)));
                if (linearLayout.mainAxisAlignment() != JustifyContent.END)
                    l.add(expanded(empty()));
                yield l;
            }
        };

        Axis mainAxis = linearLayout.mainAxis();
        Axis crossAxis = mainAxis.cross();
        int itemCount = items.size();
        double[] weights = new double[itemCount];
        double[] widths = new double[itemCount];
        double sumWidth = 0, sumWeight = 0;
        double height = 0;
        Placeable[] placeables = new Placeable[itemCount];
        boolean canFlex = constraints.max(mainAxis) != Double.POSITIVE_INFINITY;
        int i = 0;
        for (int indexInWidgets = 0; indexInWidgets < items.size(); indexInWidgets++) {
            Widget widget = items.get(indexInWidgets);
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
                if (remainingSpace < 0) {
                    logger.warn("Layout overflow, container size is " + containerWidth +
                            ", but content size is " + sumWidth + ": " + this);
                    remainingSpace = 0;
                }
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
