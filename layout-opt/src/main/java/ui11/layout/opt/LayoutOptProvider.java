package ui11.layout.opt;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.layout.multichild.LinearLayout;

public class LayoutOptProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        // TODO ha ezt az addPeerDependentet kikommentezem, akkor nem megszűnik a transzformáció, hanem
        //      "Resolution failed for ui11.ResolutionRequest@473d4775" hibák jelennek meg
        r.addPeerDependent(LinearLayoutOpt.CollapsedLLRequest.class, LinearLayout.class,
                (ll, req) -> new LinearLayoutOpt(ll, null, req));
        r.addTransformer(LinearLayout.class,
                (ll, t) -> new LinearLayoutOpt(ll, t, null));
    }
}
