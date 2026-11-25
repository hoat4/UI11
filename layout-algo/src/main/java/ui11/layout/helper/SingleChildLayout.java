package ui11.layout.helper;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.layout.protocol.BoxConstraints;

import java.util.Objects;

public class SingleChildLayout extends SubstitutedWidget {

    private final Widget child;
    private final SingleChildLayoutDelegate delegate;

    public SingleChildLayout(@NonNull Widget child, @NonNull SingleChildLayoutDelegate delegate) {
        this.child = Objects.requireNonNull(child);
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    protected SubstitutedWidget forSubstitution() {
        return new SingleChildLayout(withID("child", child), delegate);
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
