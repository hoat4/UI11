package ui11.layout.impl;

import ui11.Slot;
import ui11.Widget;
import ui11.color.Color;
import ui11.decoration.Box;
import ui11.decoration.Box.BorderSpec;
import ui11.decoration.Box.BoxShadow;
import ui11.geom.*;
import ui11.graphics.Surface;
import ui11.graphics.effect.Overlay;
import ui11.graphics.fill.LinearGradient;
import ui11.graphics.fill.LinearGradient.Stop;
import ui11.graphics.shaper.RoundedCorners;
import ui11.graphics.shaper.Stroke;
import ui11.layout.LayoutSize;
import ui11.layout.helper.SingleChildLayout;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.text.TextStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ui11.geom.Length.px;

// TODO withSize-nál relatív méret nem működik
public class DefaultBoxImpl extends Widget {

    private final Box box;

    @Inject private TextStyle ts;
    @Inject(required = false) private BoxLayoutResult.SizeRequest sizeRequest;
    @Inject(required = false) private Surface surface;
    @Inject private Slot contentSlot;
    @Inject private Slot contentWithRoundedCornersSlot;
    @Inject private Slot backgroundSlot;
    @Inject private Slot borderSlot;
    @Inject private Slot shadowTopSlot;
    @Inject private Slot shadowBottomSlot;
    @Inject private Slot shadowLeftSlot;
    @Inject private Slot shadowRightSlot;

    public DefaultBoxImpl(Box box) {
        this.box = box;
    }

    @Override
    protected Widget build() {
        BoxConstraints constraints = containerConstraints();

        Size containerSize = null;
        LayoutSize fs = box.fixedSize();
        if (fs != null) {
            if (fs.width() != null)
                if (fs.height() != null)
                    containerSize = new Size(
                            evalLen(fs.width()),
                            evalLen(fs.height())
                    );
                else {
                    // w van, h nincs
                    constraints = constraints.withTightWidth(constraints.clampWidth(evalLen(fs.width())));
                }
            else if (fs.height() != null) {
                // w nincs, h van
                constraints = constraints.withTightHeight(constraints.clampHeight(evalLen(fs.height())));
            }
        } else {
            LayoutSize minSize = box.minSize();
            if (minSize != null)
                constraints = new BoxConstraints(
                        minSize.width() == null ? constraints.minWidth() : constraints.clampWidth(evalLen(minSize.width())),
                        minSize.height() == null ? constraints.minHeight() : constraints.clampHeight(evalLen(minSize.height())),
                        constraints.maxWidth(),
                        constraints.maxHeight()
                );
        }

        if (containerSize == null) {
            Size allPadding = new Size(
                    borderSizeSum(Axis.HORIZONTAL) * 2,
                    borderSizeSum(Axis.VERTICAL) * 2
            );
            BoxConstraints childConstraints = constraints.subtract(allPadding);
            return new BoxLayoutResult.SizeRequest(childConstraints).executedOn(box.content().withSlot(contentSlot), r -> {
                Size childSize = switch (r) {
                    case BoxLayoutResult.OfGone _ -> Size.ZERO;
                    case BoxLayoutResult.OfNoConstraints _ -> {
                        throw new RuntimeException("unexpected " +
                                BoxLayoutResult.class.getSimpleName() + ": " + r);
                    }
                    case BoxLayoutResult.OfChosenSize ofChosenSize -> ofChosenSize.size();
                };
                Size newContainerSize = childSize.add(allPadding);
                return layoutPhase2(newContainerSize);
            });
        } else
            return layoutPhase2(containerSize);
    }

    private Widget layoutPhase2(Size containerSize) {
        BoxConstraints constraints = containerConstraints();

        containerSize = constraints.clamp(containerSize);

        // pixelhatárokat preferáljuk, ha elfér a konténerben.
        // kisebbíteni nem lehet, mert nem férünk el, ezért csak ceil-lel próbálkozunk.
        if (constraints.isSatisfiedBy(containerSize.ceil()))
            containerSize = containerSize.ceil();

        Rect outerBounds = Rect.of(containerSize);
        Widget borderShape;
        Rect contentBounds;
        BorderSpec border = box.border();
        if (border != null) {
            BorderInfo borderInfo = computeBorderShape(outerBounds, border.fill());
            contentBounds = borderInfo.contentShape;
            borderShape = borderInfo.borderStroke;
        } else {
            contentBounds = outerBounds;
            borderShape = null;
        }

        double cornerRadius = evalLen(box.cornerRadius());

        Canvas canvas = new Canvas();

        if (box.boxShadow() != null)
            makeBoxShadow(containerSize, box.boxShadow(), canvas);

        if (box.background() != null) {
            Widget background = box.background();
            if (cornerRadius >= 0.001)
                background = RoundedCorners.withRoundedCorners(px(cornerRadius), background);

            canvas.add(background.withSlot(backgroundSlot), contentBounds);
        }

        Widget content = box.content().withSlot(contentSlot);
        if (cornerRadius >= 0.001)
            content = RoundedCorners.withRoundedCorners(px(cornerRadius), content);

        canvas.add(content.withSlot(contentWithRoundedCornersSlot), contentBounds);

        if (border != null)
            canvas.add(borderShape.withSlot(borderSlot), Rect.of(containerSize));

        Widget w = canvas.build();
        if (sizeRequest != null)
            w = new BoxLayoutResult.OfChosenSize(containerSize, w);
        return w;
    }

    private double borderSizeSum(Axis axis) {
        if (box.border() == null)
            return 0;

        double em = ts.size();
        Length l = box.border().thickness().sum(axis);
        if (l.rel() != 0)
            throw new UnsupportedOperationException("TODO");

        return l.px() + l.em() * em;
    }

    private double evalLen(Length l) {
        if (l.rel() != 0)
            throw new UnsupportedOperationException("TODO");
        return l.px() + l.em() * ts.size();
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private BorderInfo computeBorderShape(Rect bounds, Widget borderFill) {
        double em = ts.size();
        Length topL = box.border().thickness().top();
        Length rightL = box.border().thickness().right();
        Length bottomL = box.border().thickness().bottom();
        Length leftL = box.border().thickness().left();

        double top = topL.px() + topL.em() * em + bounds.height() * topL.rel() / 2, topHalf = top / 2;
        double right = rightL.px() + rightL.em() * em + bounds.height() * rightL.rel() / 2, rightHalf = right / 2;
        double bottom = bottomL.px() + bottomL.em() * em + bounds.height() * bottomL.rel() / 2, bottomHalf = bottom / 2;
        double left = leftL.px() + leftL.em() * em + bounds.height() * leftL.rel() / 2, leftHalf = left / 2;

        List<Vec2> vertices = new ArrayList<>();
        vertices.add(bounds.topLeft().plus(leftHalf, topHalf));
        vertices.add(bounds.topRight().plus(-rightHalf, topHalf));
        vertices.add(bounds.bottomRight().plus(-rightHalf, -bottomHalf));
        vertices.add(bounds.bottomLeft().plus(leftHalf, -bottomHalf));
        vertices.add(bounds.topLeft().plus(leftHalf, topHalf));

        return new BorderInfo(new Stroke(borderFill, px(top), Path.ofVertices(vertices)),
                bounds.inset(top, top, top, top));
    }

    private record BorderInfo(Widget borderStroke, Rect contentShape) {
    }

    private void makeBoxShadow(Size s, BoxShadow m, Canvas canvas) {
        double emSize = ts.size();
        Length len = m.blur();
        if (len.rel() != 0)
            throw new RuntimeException("box shadow doesn't support sizes that are relative to parent");
        double blur = len.em() * emSize + len.px();

        canvas.add(new LinearGradient(180, List.of(
                new Stop(Color.TRANSPARENT, Length.percent(0)),
                new Stop(m.color(), Length.percent(100))
        )).withSlot(shadowTopSlot), new Rect(0, -blur, s.width(), blur));

        canvas.add(new LinearGradient(270, List.of(
                new Stop(Color.TRANSPARENT, Length.percent(0)),
                new Stop(m.color(), Length.percent(100))
        )).withSlot(shadowRightSlot), new Rect(s.width(), 0, blur, s.height()));

        canvas.add(new LinearGradient(0, List.of(
                new Stop(Color.TRANSPARENT, Length.percent(0)),
                new Stop(m.color(), Length.percent(100))
        )).withSlot(shadowBottomSlot), new Rect(0, s.height(), s.width(), blur));

        canvas.add(new LinearGradient(90, List.of(
                new Stop(Color.TRANSPARENT, Length.percent(0)),
                new Stop(m.color(), Length.percent(100))
        )).withSlot(shadowLeftSlot), new Rect(-blur, 0, blur, s.height()));

        // TODO sarkok
    }

    private BoxConstraints containerConstraints() {
        BoxConstraints constraints = sizeRequest != null ? sizeRequest.constraints() : null;

        if (constraints == null) {
            if (surface == null)
                throw new IllegalStateException("no " + Surface.class.getSimpleName() + " or " +
                        BoxConstraints.class.getSimpleName() + " provided for " + this);
            constraints = BoxConstraints.tight(surface.size());
        }
        return constraints;
    }


    // ebből majd lehetne egy publikus osztály.
    // csak akkor lent a buildbe new FixedSize(Length.zero(), ...) kéne
    // (most azért nem lehet, mert végtelen rekurziót eredményezne, mivel
    // FixedSize is Box-szal van implementálva)
    private static final class Canvas {

        private final List<Widget> widgets = new ArrayList<>();

        public void add(Widget w, Rect bounds) {
            Objects.requireNonNull(w);
            Objects.requireNonNull(bounds);
            widgets.add(SingleChildLayout.transformWidgetToBounds(w, bounds));
        }

        public Widget build() {
            return new Overlay(widgets);
        }
    }

}
