package ui11.layout.helper;

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
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutResult;

import java.util.Objects;

import static ui11.graphics.Empty.empty;

public class SingleChildLayout extends Widget {

    private final Widget child;
    private final SingleChildLayoutDelegate delegate;

    @Inject(required = false) private BoxLayoutResult.SizeRequest sizeRequest; // TODO Set<SizeRequest>
    @Inject(required = false) private Surface surface;

    public SingleChildLayout(@NonNull Widget child, @NonNull SingleChildLayoutDelegate delegate) {
        this.child = Objects.requireNonNull(child);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    protected Widget build() {
        BoxConstraints containerConstraints = containerConstraints();

        BoxConstraints childConstraints = delegate.computeChildConstraints(containerConstraints);
        Objects.requireNonNull(childConstraints);

        BoxLayoutResult.SizeRequest sizeReq = new BoxLayoutResult.SizeRequest(childConstraints);
        return PeerRequestor.ofSingle(child, sizeReq, result -> {
            return switch (result.peer()) {
                case BoxLayoutResult.OfGone _ -> empty(); // mert overlay(gone()) is ugyanaz mint empty()
                case BoxLayoutResult.OfChosenSize r -> {
                    Size childSize = r.size();
                    if (!childConstraints.isSatisfiedBy(childSize))
                        throw new RuntimeException("child size not satisfied by child constraints: " +
                                childConstraints + ", " + childSize + ", " + child);

                    Size containerSize = delegate.computeContainerSize(containerConstraints, childSize);
                    Objects.requireNonNull(containerSize);
                    containerSize = containerConstraints.clamp(containerSize);

                    Vec2 childTopLeft = delegate.computeChildPosition(containerSize, childSize);
                    Objects.requireNonNull(childTopLeft);

                    Rect childBounds = new Rect(childTopLeft, childSize);
                    childBounds = snapToPixels(childBounds);
                    Widget resultWidget = transformWidgetToBounds(result.widget(), childBounds);

                    if (sizeRequest != null)
                        resultWidget = sizeRequest.createResponse(
                                new BoxLayoutResult.OfChosenSize(containerSize), resultWidget);

                    yield resultWidget;
                }
            };
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

    // TODO ideiglenesen publikus, mert DefaultLinearLayoutImpl-nek kell
    public static @NonNull Transform transformWidgetToBounds(Widget childWithSlot, Rect bounds) {
        return new Transform(
                new RectangleShaped(
                        childWithSlot,
                        bounds.size()
                ),
                Mat4.ofTranslation(bounds.topLeft())
        );
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

    public interface SingleChildLayoutDelegate {

        BoxConstraints computeChildConstraints(BoxConstraints containerConstraints);

        Size computeContainerSize(BoxConstraints containerConstraints, Size childSize);

        Vec2 computeChildPosition(Size containerSize, Size childSize);
    }
}
