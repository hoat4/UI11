package ui.platform.glass;

import com.sun.glass.ui.View;
import org.jspecify.annotations.NonNull;
import ui11.geom.Location;
import ui11.geom.Rect;
import ui11.geom.Shape;
import ui11.geom.Size;
import ui11.renderer.NativeWindowSurface;
import ui11.renderer.RenderableSurface;

import java.lang.foreign.MemorySegment;

public class GlassSurface implements RenderableSurface, NativeWindowSurface {

    private final View view;
    private final WindowImpl windowImpl;
    private final Location.CoordinateSpaceRoot coordinateSpaceRoot = new Location.CoordinateSpaceRoot();

    public GlassSurface(View view, WindowImpl windowImpl) {
        this.view = view;
        this.windowImpl = windowImpl;
    }

    @SuppressWarnings("Since15") // IntelliJ bug
    @Override
    public @NonNull MemorySegment nativeWindowHandle() {
        long nativeView = view.getNativeView();
        if (nativeView == 0)
            throw new IllegalStateException();
        return MemorySegment.ofAddress(nativeView);
    }

    @Override
    public int width() {
        return windowImpl.innerSize.get().width();
    }

    @Override
    public int height() {
        return windowImpl.innerSize.get().height();
    }

    @Override
    public double devicePixelRatio() {
        return 1;
    }

    @Override
    public Shape layoutShape() {
        return Shape.ofRect(Rect.of(new Size(width(), height())), coordinateSpace());
    }

    @Override
    public Shape clipShape() {
        return layoutShape(); // TODO
    }

    @Override
    public Shape inputShape() {
        return layoutShape(); // TODO
    }

    @Override
    public Location.CoordinateSpace coordinateSpace() {
        return coordinateSpaceRoot.origin;
    }
}
