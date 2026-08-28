package ui11.layout.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Axis;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.multichild.LinearLayout.WeightMarker;
import ui11.layout.multichild.LinearLayout.JustifyContent;
import ui11.layout.multichild.LinearLayout.WeightMarker.WeightRequest;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Alignment;

import java.util.*;

import static ui11.graphics.Empty.empty;
import static ui11.graphics.effect.Overlay.overlay;
import static ui11.layout.multichild.LinearLayout.expanded;
import static ui11.layout.multichild.LinearLayout.withWeight;

public abstract class DefaultLinearLayoutImpl extends Widget {

    private static final Logger logger = LoggerFactory.getLogger(DefaultLinearLayoutImpl.class);

    protected final LinearLayout linearLayout;

    public DefaultLinearLayoutImpl(LinearLayout linearLayout) {
        this.linearLayout = Objects.requireNonNull(linearLayout);
    }

    @Override
    protected Widget build() {
        List<? extends Widget> items = linearLayout.items();
        if (items.isEmpty())
            // heightnak 0-t számolna ki a lenti algoritmus szerint, ha nincs egyetlen elem se az LL-ben
            return empty();

        items = applyMainAxisAlignment(items);
        List<? extends Widget> itemsFinal = items;

        BoxConstraints constraints = containerConstraints();
        Axis mainAxis = linearLayout.mainAxis();
        Axis crossAxis = mainAxis.cross();
        BoxLayoutResult.SizeRequest sizeReq = new BoxLayoutResult.SizeRequest(BoxConstraints.of(
                mainAxis,
                /* min width */ 0,
                /* min height */ linearLayout.crossAxisAlignment() == LinearLayout.AlignChildren.STRETCH ?
                        constraints.min(crossAxis) : 0,
                /* max width */ Double.POSITIVE_INFINITY,
                /* max height */ constraints.max(crossAxis)
        ));
        return PeerRequest.requestMultiple(items, Set.of(sizeReq, WeightRequest.INSTANCE),
                results -> layoutPhase2(itemsFinal,
                        (List<? extends BoxLayoutResult>) results.get(sizeReq),
                        (List<? extends WeightMarker>) results.get(WeightRequest.INSTANCE)));
    }

    private List<? extends Widget> applyMainAxisAlignment(List<? extends Widget> items) {
        items = switch (linearLayout.mainAxisAlignment()) {
            case STRETCH -> {
                // do nothing
                yield items;
            }
            case SPACE_EVENLY -> {
                List<Widget> l = new ArrayList<>();
                l.add(withWeight(1, empty()));
                for (Widget w : items) {
                    l.add(withWeight(0, w));
                    l.add(withWeight(1, empty()));
                }
                yield l;
            }
            case SPACE_AROUND -> {
                List<Widget> l = new ArrayList<>();
                for (Widget w : items) {
                    l.add(withWeight(1, empty()));
                    l.add(withWeight(0, w));
                    l.add(withWeight(1, empty()));
                }
                yield l;
            }
            case SPACE_BETWEEN -> {
                List<Widget> l = new ArrayList<>();
                Iterator<? extends Widget> iterator = items.iterator();

                l.add(withWeight(0, iterator.next()));
                while (iterator.hasNext()) {
                    Widget w = iterator.next();
                    l.add(withWeight(1, empty()));
                    l.add(withWeight(0, w));
                }
                yield l;
            }
            case START, CENTER, END -> {
                List<Widget> l = new ArrayList<>();
                if (linearLayout.mainAxisAlignment() != JustifyContent.START)
                    l.add(expanded(empty()));
                for (Widget w : items)
                    l.add(withWeight(0, w));
                if (linearLayout.mainAxisAlignment() != JustifyContent.END)
                    l.add(expanded(empty()));
                yield l;
            }
        };
        return items;
    }

    private Widget layoutPhase2(List<? extends Widget> items,
                                List<? extends BoxLayoutResult> boxLayoutResults,
                                List<? extends WeightMarker> weightResults) {
        BoxConstraints constraints = containerConstraints();
        Axis mainAxis = linearLayout.mainAxis();
        Axis crossAxis = mainAxis.cross();

        Alignment crossAxisAlignment = switch (linearLayout.crossAxisAlignment()) {
            case STRETCH -> null;
            case START -> switch (linearLayout.crossAxis()) {
                case VERTICAL -> Alignment.TOP;
                case HORIZONTAL -> Alignment.LEFT;
            };
            case CENTER -> switch (linearLayout.crossAxis()) {
                case VERTICAL -> Alignment.VCENTER;
                case HORIZONTAL -> Alignment.HCENTER;
            };
            case END -> switch (linearLayout.crossAxis()) {
                case VERTICAL -> Alignment.BOTTOM;
                case HORIZONTAL -> Alignment.RIGHT;
            };
        };

        // TODO gap figyelembe vétele

        int itemCount = items.size();
        double[] weights = new double[itemCount];
        double[] widths = new double[itemCount];
        double sumWidth = 0, sumWeight = 0;
        double height = 0;
        Widget[] placeables = new Widget[itemCount];
        boolean canFlex = constraints.max(mainAxis) != Double.POSITIVE_INFINITY;
        int i = 0;
        for (int indexInWidgets = 0; indexInWidgets < items.size(); indexInWidgets++) {
            Widget widget = items.get(indexInWidgets);
            BoxLayoutResult boxLayoutResult = boxLayoutResults.get(indexInWidgets);
            double weight = weightResults.get(indexInWidgets).weight();

            switch (boxLayoutResult) {
                case BoxLayoutResult.OfGone _ -> {
                    continue;
                }
                case BoxLayoutResult.OfChosenSize r -> {
                    if (crossAxisAlignment != null)
                        widget = Align.align(crossAxisAlignment, widget);
                    placeables[i] = widget;
                    weights[i] = weight;
                    if (weight == 0 || !canFlex) {
                        Size size = r.size();

                        double mainLen = size.length(mainAxis);
                        mainLen = Math.ceil(mainLen); // snap to pixel
                        sumWidth += widths[i] = mainLen;
                        height = Math.max(height, size.length(crossAxis));
                    } else {
                        // TODO ilyenkor miért nem adjuk hozzá width-hez?
                        sumWeight += weight;
                    }
                    i++;
                }
            }
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
                return layoutPhase3(itemCount, placeables, widths, height, containerWidth);
            } else {
                containerWidth = constraints.max(mainAxis);
                double remainingSpace = containerWidth - sumWidth;
                if (remainingSpace < 0) {
                    logger.warn("Layout overflow, container size is " + containerWidth +
                            ", but content size is " + sumWidth + ": " + this);
                    remainingSpace = 0;
                }

                List<Widget> reqWidgets = new ArrayList<>();
                List<BoxLayoutResult.SizeRequest> reqs = new ArrayList<>();
                double remainingWeight = sumWeight;
                for (i = 0; i < itemCount; i++) {
                    if (weights[i] != 0) {
                        // snap to pixel
                        double additionalWidth = Math.min(remainingSpace, Math.ceil(remainingSpace * weights[i] / remainingWeight));
                        widths[i] += additionalWidth;
                        remainingSpace -= additionalWidth;
                        remainingWeight -= weights[i];
                        reqWidgets.add(placeables[i]);
                        reqs.add(new BoxLayoutResult.SizeRequest(BoxConstraints.of(
                                mainAxis,
                                widths[i], constraints.min(crossAxis),
                                widths[i], constraints.max(crossAxis)
                        )));
                        // numerikus pontatlanságokkal kezdjünk majd valamit
                    }
                }
                double heightFinal = height;
                int itemCountFinal = itemCount;
                return PeerRequest.requestMultiple(reqWidgets, reqs, resolutionResults -> {
                    double height2 = heightFinal;
                    for (BoxLayoutResult layoutResult : resolutionResults) {
                        switch (layoutResult) {
                            case BoxLayoutResult.OfGone _ -> { // TODO ez legális?
                                // 0x0-nak tekintjük, ezért nem kell height-ot változtatni
                            }
                            case BoxLayoutResult.OfChosenSize r -> {
                                height2 = Math.max(height2, r.size().length(crossAxis));
                            }
                        }
                    }
                    return layoutPhase3(itemCountFinal, placeables, widths, height2, containerWidth);
                });
            }
        } else {
            containerWidth = Math.min(constraints.max(mainAxis), sumWidth);
            return layoutPhase3(itemCount, placeables, widths, height, containerWidth);
        }
    }

    protected abstract Widget layoutPhase3(int itemCount, Widget[] placeables, double[] widths, double height, double containerWidth);

    protected abstract BoxConstraints containerConstraints();

    static class Sizer extends DefaultLinearLayoutImpl {

        private final BoxLayoutResult.SizeRequest sizeRequest;

        public Sizer(LinearLayout linearLayout, BoxLayoutResult.SizeRequest sizeRequest) {
            super(linearLayout);
            this.sizeRequest = sizeRequest;
        }

        @Override
        protected BoxConstraints containerConstraints() {
            return sizeRequest.constraints();
        }

        @Override
        protected Widget layoutPhase3(int itemCount, Widget[] placeables, double[] widths, double height, double containerWidth) {
            Axis mainAxis = linearLayout.mainAxis();
            Size containerSize = Size.of(mainAxis, containerWidth, height);
            BoxLayoutResult.OfChosenSize chosenSize = new BoxLayoutResult.OfChosenSize(containerSize);
            return sizeRequest.createResponse(chosenSize);
        }
    }

    static class Arranger extends DefaultLinearLayoutImpl {

        private final Surface surface;

        public Arranger(LinearLayout linearLayout, Surface surface) {
            super(linearLayout);
            this.surface = surface;
        }

        @Override
        protected BoxConstraints containerConstraints() {
            return BoxConstraints.tight(surface.size());
        }

        @Override
        protected Widget layoutPhase3(int itemCount, Widget[] placeables, double[] widths, double height, double containerWidth) {
            Axis mainAxis = linearLayout.mainAxis();

            return overlay(o -> {
                double x = 0;
                for (int i = 0; i < itemCount; i++) {
                    double w = widths[i];
                    Rect bounds = Rect.of(mainAxis, x, 0, w, height);
                    o.accept(DefaultSingleChildLayoutImpl.transformWidgetToBounds(placeables[i], bounds));
                    x += w;
                }
            });
        }
    }
}
