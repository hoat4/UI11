package ui11.platform.awt.j2d.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.input.pointer.PointerRegion;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.inputtree.ListenerInputNode;

public class J2DPointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;
    private final J2DSurface surface;

    @Remember private ListenerInputNode inputNode;

    public J2DPointerRegionPeer(PointerRegion pointerRegion, J2DSurface surface) {
        this.pointerRegion = pointerRegion;
        this.surface = surface;
    }

    @Override
    protected void initState() {
        inputNode = new ListenerInputNode();
    }

    @Override
    protected Widget build() {
        // TODO ezzel valamit csin�lni k�ne, hogy ne J2DWidgetResolver.tryResolveGenericbe
        //      kelljen be�rni, hogy a t�bbi req t�pus legyen contentnek tov�bb�tva

        Widget content = pointerRegion.content();
        return PeerRequest.requestSingle(content, surface, result -> {
            inputNode.child.set(result.inputNode());
            inputNode.listener = pointerRegion;
            J2DNodeHolder h = new J2DNodeHolder(result.renderNode(), inputNode);
            return surface.createResponse(h);
        });
    }
}
