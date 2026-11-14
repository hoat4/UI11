package ui11.layout.opt;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.layout.multichild.LinearLayout;

public class LayoutOptProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.registerTransformer(LinearLayout.class, LinearLayoutOpt::new);
    }

    @Override
    public Priority priority() {
        return Priority.POLYFILL; // not applicable because no resolvers registered
    }
}
