package ui11.layout.impl;

import org.jspecify.annotations.NonNull;
import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Mat4;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.graphics.effect.Transform;
import ui11.graphics.shaper.RectangleShaped;
import ui11.layout.helper.LayoutHelper;
import ui11.layout.helper.SingleChildLayout;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;

import java.util.Objects;

import static ui11.graphics.Empty.empty;

public final class DefaultSingleChildLayoutImpl extends Widget {

    private final SingleChildLayout singleChildLayout;

    public DefaultSingleChildLayoutImpl(SingleChildLayout singleChildLayout) {
        this.singleChildLayout = singleChildLayout;
    }

    @Override
    protected Widget build() {
        SingleChildLayout.SingleChildLayoutDelegate delegate = singleChildLayout.delegate();

        // TODO túl sok a két ág között a duplikáció, ki kéne emelni pár dolgot
        return new LayoutHelper(new LayoutHelper.LayoutHelperDelegate() {
            @Override
            public @NonNull Widget computePreferredSize(BoxLayoutResult.SizeRequest sizeRequest) {
                BoxConstraints containerConstraints = sizeRequest.constraints();

                BoxConstraints childConstraints = delegate.computeChildConstraints(containerConstraints);
                Objects.requireNonNull(childConstraints);

                BoxLayoutResult.SizeRequest sizeReq = new BoxLayoutResult.SizeRequest(childConstraints);
                return PeerRequest.requestSingle(singleChildLayout.child(), sizeReq, result -> {
                    return switch (result) {
                        case BoxLayoutResult.OfGone _ -> empty(); // mert overlay(gone()) is ugyanaz mint empty()
                        case BoxLayoutResult.OfChosenSize r -> {
                            Size childSize = r.size();
                            if (!childConstraints.isSatisfiedBy(childSize))
                                throw new RuntimeException("child size not satisfied by child constraints: " +
                                        childConstraints + ", " + childSize + ", " + singleChildLayout.child());

                            Size containerSize = delegate.computeContainerSize(containerConstraints, childSize);
                            Objects.requireNonNull(containerSize);
                            containerSize = containerConstraints.clamp(containerSize);

                            yield sizeRequest.createResponse(new BoxLayoutResult.OfChosenSize(containerSize));
                        }
                    };
                });
            }

            @Override
            public @NonNull Widget computeChildArrangement(Size containerSize) {
                BoxConstraints containerConstraints = BoxConstraints.tight(containerSize);

                BoxConstraints childConstraints = delegate.computeChildConstraints(containerConstraints);
                Objects.requireNonNull(childConstraints);

                BoxLayoutResult.SizeRequest sizeReq = new BoxLayoutResult.SizeRequest(childConstraints);
                return PeerRequest.requestSingle(singleChildLayout.child(), sizeReq, result -> {
                    return switch (result) {
                        case BoxLayoutResult.OfGone _ -> empty(); // mert overlay(gone()) is ugyanaz mint empty()
                        case BoxLayoutResult.OfChosenSize r -> {
                            Size childSize = r.size();
                            if (!childConstraints.isSatisfiedBy(childSize))
                                throw new RuntimeException("child size not satisfied by child constraints: " +
                                        childConstraints + ", " + childSize + ", " + singleChildLayout.child());

                            SingleChildLayout.SingleChildLayoutDelegate delegate = singleChildLayout.delegate();
                            Vec2 childTopLeft = delegate.computeChildPosition(containerSize, childSize);
                            Objects.requireNonNull(childTopLeft);

                            Rect childBounds = new Rect(childTopLeft, childSize);
                            childBounds = snapToPixels(childBounds);
                            yield transformWidgetToBounds(singleChildLayout.child(), childBounds);
                        }
                    };
                });
            }
        });
    }

    private Rect snapToPixels(Rect childBounds) {
        // TODO ha nem egész a container mérete, akkor a childnak sem kéne erőltetni
        //      hogy egész mérete legyen, mert nem fér bele a
        return new Rect(
                childBounds.topLeft().floor(),
                childBounds.size().ceil()
        );
    }

    // TODO ezt ki kéne emelni közös osztályba, mert DefaultLinearLayoutImpl-nek kell
    static @NonNull Transform transformWidgetToBounds(Widget childWithSlot, Rect bounds) {
        return new Transform(
                new RectangleShaped(
                        childWithSlot,
                        bounds.size()
                ),
                Mat4.ofTranslation(bounds.topLeft())
        );
    }
}
