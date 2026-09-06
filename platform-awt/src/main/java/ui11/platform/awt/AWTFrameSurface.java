package ui11.platform.awt;

import ui11.renderer.Surface;

import java.awt.*;
import java.awt.image.BufferStrategy;

public final class AWTFrameSurface implements Surface {

    private final AWTWindow.AWTWindowImpl frame;
    private final BufferStrategy bufferStrategy;

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
