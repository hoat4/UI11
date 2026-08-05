package ui11.layout.impl;

import ui11.PeerRequestor;
import ui11.Slot;
import ui11.Slot2;
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

    @Remember private Slot2 contentWithRoundedCornersSlot;
    @Remember private Slot2 backgroundSlot;
    @Remember private Slot2 borderSlot;
    @Remember private Slot2 shadowTopSlot;
    @Remember private Slot2 shadowBottomSlot;
    @Remember private Slot2 shadowLeftSlot;
    @Remember private Slot2 shadowRightSlot;

    public DefaultBoxImpl(Box box) {
        this.box = box;
    }

    @Override
    protected void initState() {
        contentWithRoundedCornersSlot = new Slot2();
        backgroundSlot = new Slot2();
        borderSlot = new Slot2();
        shadowTopSlot = new Slot2();
        shadowBottomSlot = new Slot2();
        shadowLeftSlot = new Slot2();
        shadowRightSlot = new Slot2();
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
            BoxLayoutResult.SizeRequest sizeReq = new BoxLayoutResult.SizeRequest(childConstraints);
            return PeerRequestor.ofSingle(box.content(), sizeReq, r -> {
                Size childSize = switch (r.peer()) {
                    case BoxLayoutResult.OfGone _ -> Size.ZERO;
                    case BoxLayoutResult.OfChosenSize ofChosenSize -> ofChosenSize.size();
                };
                Size newContainerSize = childSize.add(allPadding);
                return layoutPhase2(newContainerSize, box.content());
            });
        } else
            return layoutPhase2(containerSize, box.content());
    }

    private Widget layoutPhase2(Size containerSize, Widget content) {
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

            canvas.add(backgroundSlot.with(background), contentBounds);
        }

        if (cornerRadius >= 0.001)
            content = RoundedCorners.withRoundedCorners(px(cornerRadius), content);

        canvas.add(contentWithRoundedCornersSlot.with(content), contentBounds);

        if (border != null)
            canvas.add(borderSlot.with(borderShape), Rect.of(containerSize));

        Widget w = canvas.build();
        if (sizeRequest != null)
            w = sizeRequest.createResponse(new BoxLayoutResult.OfChosenSize(containerSize), w);
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

        canvas.add(shadowTopSlot.with(new LinearGradient(180, List.of(
                new Stop(Color.TRANSPARENT, Length.percent(0)),
                new Stop(m.color(), Length.percent(100))
        ))), new Rect(0, -blur, s.width(), blur));

        canvas.add(shadowRightSlot.with(new LinearGradient(270, List.of(
                new Stop(Color.TRANSPARENT, Length.percent(0)),
                new Stop(m.color(), Length.percent(100))
        ))), new Rect(s.width(), 0, blur, s.height()));

        canvas.add(shadowBottomSlot.with(new LinearGradient(0, List.of(
                new Stop(Color.TRANSPARENT, Length.percent(0)),
                new Stop(m.color(), Length.percent(100))
        ))), new Rect(0, s.height(), s.width(), blur));

        canvas.add(shadowLeftSlot.with(new LinearGradient(90, List.of(
                new Stop(Color.TRANSPARENT, Length.percent(0)),
                new Stop(m.color(), Length.percent(100))
        ))), new Rect(-blur, 0, blur, s.height()));

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
