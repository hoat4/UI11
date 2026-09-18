package ui11.renderer.j2d.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.input.pointer.PointerRegion;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.rendertree.PointerListenerNode;

public class J2DPointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;

    @Inject private J2DVisualContentRequest surface;

    @Remember private PointerListenerNode node;

    public J2DPointerRegionPeer(PointerRegion pointerRegion) {
        this.pointerRegion = pointerRegion;
    }

    @Override
    protected void initState() {
        node = new PointerListenerNode();
    }

    @Override
    protected Widget build() {
        Widget content = pointerRegion.content();
        return ExposeRequest.requestSingle(content, surface, result -> {
            node.child.set(result);
            node.listener = pointerRegion;
            return new Expose<>(surface, node, content);
        });
    }
}
