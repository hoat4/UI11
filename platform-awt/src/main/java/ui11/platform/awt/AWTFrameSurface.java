package ui11.platform.awt;

import ui11.geom.*;
import ui11.geom.Shape;
import ui11.observable.InvalidationPoint;
import ui11.renderer.RenderableSurface;

import java.awt.*;
import java.awt.image.BufferStrategy;

public final class AWTFrameSurface implements RenderableSurface {

    private final AWTWindow.AWTWindowImpl frame;
    private final BufferStrategy bufferStrategy;
    private final Location.CoordinateSpaceRoot coordinateSpaceRoot = new Location.CoordinateSpaceRoot();

    AWTFrameSurface(AWTWindow.AWTWindowImpl frame, BufferStrategy bufferStrategy) {
        this.frame = frame;
        this.bufferStrategy = bufferStrategy;
    }

    @Override
    public int width() {
        return frame.innerWidth();
    }

    @Override
    public int height() {
        return frame.innerHeight();
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

    public BufferStrategy bufferStrategy() {
        return bufferStrategy;
    }

    public GraphicsConfiguration graphicsConfiguration() {
        return frame.getGraphicsConfiguration();
    }

    // TODO ezek az insetszámolások valszeg off-by-one hibásak
    public int leftInset() {
        double scaleX = graphicsConfiguration().getDefaultTransform().getScaleX();
        return (int) Math.round(frame.getInsets().left * scaleX);
    }

    public int topInset() {
        double scaleY = graphicsConfiguration().getDefaultTransform().getScaleY();
        return (int) Math.round(frame.getInsets().top * scaleY);
    }
}
