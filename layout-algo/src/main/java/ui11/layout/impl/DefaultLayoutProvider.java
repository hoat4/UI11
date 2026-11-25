package ui11.layout.impl;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.ResolverRegistry.ResolutionRule;
import ui11.SubstitutedWidget;
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
        r.register(new ResolutionRule<>(Align.class, DefaultAlignImpl::new));
        r.register(new ResolutionRule<>(Box.class, DefaultBoxImpl::new));
        r.register(new ResolutionRule<>(Padding.class, DefaultPaddingImpl::new));
        r.register(new ResolutionRule<>(LinearLayout.class, DefaultLinearLayoutImpl::new));
        r.register(new ResolutionRule<>(SingleChildLayout.class, DefaultSingleChildLayoutImpl::new));
        r.register(new ResolutionRule<>(Gone.class, gone -> GoneImpl.INSTANCE).
                parallelWithOtherResolvers().onlyOnRequestTypes(SizeRequest.class));
        r.register(new ResolutionRule<>(ColorFill.class, colorFill -> PreferredSizeIsMinimum.INSTANCE).
                parallelWithOtherResolvers().onlyOnRequestTypes(SizeRequest.class));
        r.register(new ResolutionRule<>(Empty.class, empty -> PreferredSizeIsMinimum.INSTANCE).
                parallelWithOtherResolvers().onlyOnRequestTypes(SizeRequest.class));
        // TODO Overlay
        // case Grid grid -> new WidgetStateRequest<>(DefaultGrid::new, grid);
        // case PassiveHeight passiveHeight -> new WidgetStateRequest<>(DefaultPassiveHeight::new, passiveHeight);
        // case PassiveSize passiveSize -> new WidgetStateRequest<>(DefaultPassiveSize::new, passiveSize);
    }

    @Override
    public Priority priority() {
        return Priority.POLYFILL;
    }
}
