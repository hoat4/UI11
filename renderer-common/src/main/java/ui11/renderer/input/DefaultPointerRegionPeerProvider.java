package ui11.renderer.input;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.input.pointer.PointerRegion;

public class DefaultPointerRegionPeerProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.register(PointerRegion.class, DefaultPointerRegionPeer::new);
    }
}
