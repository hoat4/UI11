package ui11.layout.impl;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.decoration.Box;
import ui11.graphics.Empty;
import ui11.graphics.fill.ColorFill;
import ui11.layout.Gone;
import ui11.layout.helper.SingleChildLayout;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.protocol.BoxLayoutResult.SizeRequest;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Padding;

import java.util.List;

public class DefaultLayoutProvider implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        r.register(Align.class, DefaultAlignImpl::new);
        r.register(Box.class, DefaultBoxImpl::new);
        r.register(Padding.class, DefaultPaddingImpl::new);
        r.register(LinearLayout.class, DefaultLinearLayoutImpl::new);
        r.register(SingleChildLayout.class, DefaultSingleChildLayoutImpl::new);
        r.registerPeerResolver(SizeRequest.class, Gone.class, gone -> GoneImpl.INSTANCE);
        r.registerPeerResolver(SizeRequest.class, ColorFill.class, gone -> PreferredSizeIsMinimum.INSTANCE);
        r.registerPeerResolver(SizeRequest.class, Empty.class, gone -> PreferredSizeIsMinimum.INSTANCE);
        // TODO Overlay
        // case Grid grid -> new WidgetStateRequest<>(DefaultGrid::new, grid);
        // case PassiveHeight passiveHeight -> new WidgetStateRequest<>(DefaultPassiveHeight::new, passiveHeight);
        // case PassiveSize passiveSize -> new WidgetStateRequest<>(DefaultPassiveSize::new, passiveSize);
    }
}
