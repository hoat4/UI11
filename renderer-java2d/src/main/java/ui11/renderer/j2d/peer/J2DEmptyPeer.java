package ui11.renderer.j2d.peer;

import ui11.Widget;
import ui11.graphics.fill.ColorFill;
import ui11.renderer.j2d.J2DUtil;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.rendertree.EmptyNode;
import ui11.renderer.j2d.rendertree.FillPathNode;

import java.awt.*;

public class J2DEmptyPeer extends Widget {

    public static final J2DEmptyPeer INSTANCE = new J2DEmptyPeer();

    @Inject private J2DVisualContentRequest surface;

    private J2DEmptyPeer() {
    }

    @Override
    protected Widget build() {
        return surface.createResponse(EmptyNode.INSTANCE);
    }
}
