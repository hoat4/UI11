package ui11.layout.impl;

import org.jspecify.annotations.NonNull;
import ui11.PeerRequestor;
import ui11.Widget;
import ui11.geom.Mat4;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.effect.Transform;
import ui11.graphics.shaper.RectangleShaped;
import ui11.layout.helper.SingleChildLayout;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;

import java.util.Objects;

import static ui11.graphics.Empty.empty;

public abstract class DefaultSingleChildLayoutImpl extends Widget {

    protected final SingleChildLayout singleChildLayout;

    public DefaultSingleChildLayoutImpl(SingleChildLayout singleChildLayout) {
        this.singleChildLayout = singleChildLayout;
    }

    @Override
    protected Widget build() {
        SingleChildLayout.SingleChildLayoutDelegate delegate = singleChildLayout.delegate();

        BoxConstraints containerConstraints = containerConstraints();

        BoxConstraints childConstraints = delegate.computeChildConstraints(containerConstraints);
        Objects.requireNonNull(childConstraints);

        BoxLayoutResult.SizeRequest sizeReq = new BoxLayoutResult.SizeRequest(childConstraints);
        return PeerRequestor.ofSingle(singleChildLayout.child(), sizeReq, result -> {
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

                    yield makeLayoutResult(containerSize, childSize);
                }
            };
        });
    }

    protected Rect snapToPixels(Rect childBounds) {
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

    protected abstract BoxConstraints containerConstraints();

    protected abstract Widget makeLayoutResult(Size containerSize, Size childSize);

    public static final class Sizer extends DefaultSingleChildLayoutImpl {

        private final BoxLayoutResult.SizeRequest sizeRequest;

        public Sizer(SingleChildLayout singleChildLayout, BoxLayoutResult.SizeRequest sizeRequest) {
            super(singleChildLayout);
            this.sizeRequest = sizeRequest;
        }

        @Override
        protected BoxConstraints containerConstraints() {
            return sizeRequest.constraints();
        }

        @Override
        protected Widget makeLayoutResult(Size containerSize, Size childSize) {
            return sizeRequest.createResponse(new BoxLayoutResult.OfChosenSize(containerSize));
        }
    }

    public static final class Arranger extends DefaultSingleChildLayoutImpl {

        private final Surface surface;

        public Arranger(SingleChildLayout singleChildLayout, Surface surface) {
            super(singleChildLayout);
            this.surface = surface;
        }

        @Override
        protected BoxConstraints containerConstraints() {
            return BoxConstraints.tight(surface.size());
        }

        @Override
        protected Widget makeLayoutResult(Size containerSize, Size childSize) {
            SingleChildLayout.SingleChildLayoutDelegate delegate = singleChildLayout.delegate();
            Vec2 childTopLeft = delegate.computeChildPosition(containerSize, childSize);
            Objects.requireNonNull(childTopLeft);

            Rect childBounds = new Rect(childTopLeft, childSize);
            childBounds = snapToPixels(childBounds);
            return transformWidgetToBounds(singleChildLayout.child(), childBounds);
        }
    }
}
