package ui11.layout.opt;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.layout.multichild.LinearLayout;

public class LayoutOptProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.addPeerDependent(LinearLayoutOpt.CollapsedLLRequest.class, LinearLayout.class,
                (ll, req) -> new LinearLayoutOpt(ll, null, req));
        r.addTransformer(LinearLayout.class,
                (ll, t) -> new LinearLayoutOpt(ll, t, null));
    }
}
