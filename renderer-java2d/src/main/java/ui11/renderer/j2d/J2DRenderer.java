package ui11.renderer.j2d;

import ui11.geom.Location;
import ui11.geom.Size;
import ui11.graphics.VisualContentRequest;
import ui11.observable.Observable;
import ui11.platform.awt.AWTFrameSurface;
import ui11.renderer.Renderer;
import ui11.renderer.input.InputNode;
import ui11.renderer.j2d.rendertree.RenderNode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class J2DRenderer implements Renderer<J2DNodeHolder, RenderNode> {

    private final AWTFrameSurface surface;

    // egyelőre csak AWTFrameSurface lehet. később majd más is.
    public J2DRenderer(AWTFrameSurface surface) {
        this.surface = surface;
    }

    @Override
    public VisualContentRequest<J2DNodeHolder> createRootContentRequest(
            Location.CoordinateSpaceRoot coordinateSpaceRoot,
            Observable<Size> size) {
        return new J2DVisualContentRequest.RootJ2DSurface(coordinateSpaceRoot, size);
    }

    @Override
    public InputNode inputNode(J2DNodeHolder holder) {
        return holder.inputNode();
    }

    @Override
    public RenderNode renderNode(J2DNodeHolder holder) {
        return holder.renderNode();
    }

    @Override
    public void render(RenderNode root) {
        if (false) {
            System.out.println();
            System.out.println("Render tree: ");
            System.out.print(RenderNode.RenderTreePrinter.toString(root));
            System.out.println("Render tree end");
            System.out.println();
        }

        RenderingContext ctx = new RenderingContext(surface.width(), surface.height());
        root.render(ctx);
        BufferedImage image = ctx.finish();
        // constructor inits Graphics2D renderingHints

        Graphics2D g = (Graphics2D) surface.bufferStrategy().getDrawGraphics();
        g.setTransform(new AffineTransform());
        int x = surface.leftInset();
        int y = surface.topInset();
        g.drawImage(image, x, y, null);

        g.dispose();
        surface.bufferStrategy().show();
    }
}
