package ui11.renderer.j2d.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.shaper.Stroke;
import ui11.renderer.j2d.J2DSurface.StrokeJ2DSurface;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.text.TextStyle;

import java.awt.*;

public class J2DStrokePeer extends Widget {

    private final Stroke stroke;

    @Inject private Surface parentSurface;
    @Inject private J2DVisualContentRequest parentRequest;
    @Inject private TextStyle textStyle;

    @Remember private StrokeJ2DSurface childSurface;

    public J2DStrokePeer(Stroke stroke) {
        this.stroke = stroke;
    }

    @Override
    protected void initState() {
        childSurface = new StrokeJ2DSurface();
    }

    @Override
    protected Widget build() {
        double thickness = stroke.thickness().px() + stroke.thickness().em() * textStyle.size();
        // relative része nincs a thicknessnek, ld. Stroke konstruktora

        childSurface.parent.set(parentSurface);
        childSurface.updateShape(stroke.path(), new BasicStroke((float) thickness));

        return ExposeRequest.requestSingle(
                stroke.texture(),
                new J2DVisualContentRequest(childSurface),
                peer -> new Expose<>(parentRequest, peer)
        );
    }
}
