package ui11.renderer.input;

import ui11.Widget;
import ui11.input.pointer.PointerRegion;

public class DefaultPointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;

    public DefaultPointerRegionPeer(PointerRegion pointerRegion) {
        this.pointerRegion = pointerRegion;
    }

    @Override
    protected Widget build() {
        throw new RuntimeException("TODO");
    }
}
