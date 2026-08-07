package ui11.layout.helper;

import org.jspecify.annotations.NonNull;
import ui11.Key;
import ui11.PeerRequestor;
import ui11.SubstitutedWidget;
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

public class SingleChildLayout extends SubstitutedWidget {

    private final Widget child;
    private final SingleChildLayoutDelegate delegate;

    @Remember private Key childKey;

    public SingleChildLayout(@NonNull Widget child, @NonNull SingleChildLayoutDelegate delegate) {
        this.child = Objects.requireNonNull(child);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    protected void initState() {
        childKey = Key.create();
    }

    @Override
    protected SubstitutedWidget forSubstitution() {
        return new SingleChildLayout(child.withKey(childKey), delegate);
    }

    public Widget child() {
        return child;
    }

    public SingleChildLayoutDelegate delegate() {
        return delegate;
    }

    public interface SingleChildLayoutDelegate {

        BoxConstraints computeChildConstraints(BoxConstraints containerConstraints);

        Size computeContainerSize(BoxConstraints containerConstraints, Size childSize);

        Vec2 computeChildPosition(Size containerSize, Size childSize);
    }
}
