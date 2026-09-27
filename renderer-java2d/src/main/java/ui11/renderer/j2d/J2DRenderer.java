package ui11.renderer.j2d;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.geom.Shape;
import ui11.geom.Size;
import ui11.platform.awt.AWTFrameSurface;
import ui11.renderer.FramebufferSize;
import ui11.renderer.Renderer;
import ui11.renderer.TextRenderer;
import ui11.renderer.layer.Layer;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class J2DRenderer implements Renderer<J2DLayer>, TextRenderer {

    private final AWTFrameSurface surface;

    // egyelőre csak AWTFrameSurface lehet. később majd más is.
    public J2DRenderer(AWTFrameSurface surface) {
        this.surface = surface;
    }

    @Override
    public J2DLayer createLayer(@Nullable Layer previous, @NonNull Shape visiblePart) {
        J2DLayer layer;
        if (previous instanceof J2DLayer l)
            layer = l;
        else
            layer = new J2DLayer(surface.coordinateSpace()); // TODO coordinateSpace
        Size size = visiblePart.bounds(surface.coordinateSpace()).floorCeil().size();
        layer.bufferSize = new FramebufferSize((int) size.width(), (int) size.height());
        return layer;
    }

    @Override
    public void initializeRenderThreadLocals() {
        // nop
    }

    @Override
    public void render(J2DLayer rootLayer, int viewportWidth, int viewportHeight) {
        /*
        if (false) {
            System.out.println();
            System.out.println("Render tree: ");
            System.out.print(new J2DRenderTreePrinter().toString(root));
            System.out.println("Render tree end");
            System.out.println();
        }
         */

        int leftInset = surface.leftInset();
        int topInset = surface.topInset();

        Graphics2D g = (Graphics2D) surface.bufferStrategy().getDrawGraphics();
        g.setTransform(new AffineTransform());
        g.setBackground(Color.WHITE);
        g.clearRect(leftInset, topInset, rootLayer.bufferSize.width(), rootLayer.bufferSize.height());
        g.drawImage(rootLayer.content.get(), leftInset, topInset, null);
        g.dispose();
        surface.bufferStrategy().show();
    }

    @Override
    public @NonNull TextLayoutCalculator createTextLayoutCalculator(@Nullable TextLayoutCalculator textLayoutCalculator) {
        if (textLayoutCalculator instanceof J2DTextLayoutCalculator c)
            return c;
        else
            return new J2DTextLayoutCalculator();
    }
}
