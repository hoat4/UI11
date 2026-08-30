package ui11.layout.impl;

import ui11.*;
import ui11.decoration.Box;
import ui11.graphics.Empty;
import ui11.graphics.fill.ColorFill;
import ui11.layout.Gone;
import ui11.layout.helper.SingleChildLayout;
import ui11.layout.protocol.BoxLayoutResult.SizeRequest;
import ui11.layout.singlechild.Align;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.singlechild.Padding;

import java.util.List;

public class DefaultLayoutProvider implements ResolverProvider {

    @Override
    public List<ResolutionRule<?>> rules() {
        return List.of(
                new ResolutionRule<>(Align.class, DefaultAlignImpl::new),
                new ResolutionRule<>(Box.class, DefaultBoxImpl::new),
                new ResolutionRule<>(Padding.class, DefaultPaddingImpl::new),
                new ResolutionRule<>(LinearLayout.class, DefaultLinearLayoutImpl::new),
                new ResolutionRule<>(SingleChildLayout.class, DefaultSingleChildLayoutImpl::new),
                new ResolutionRule<>(Gone.class, gone -> GoneImpl.INSTANCE).
                        requires(SizeRequest.class).coexistWithOtherResolvers(),
                new ResolutionRule<>(ColorFill.class, colorFill -> PreferredSizeIsMinimum.INSTANCE).
                        requires(SizeRequest.class).coexistWithOtherResolvers(),
                new ResolutionRule<>(Empty.class, empty -> PreferredSizeIsMinimum.INSTANCE).
                        requires(SizeRequest.class).coexistWithOtherResolvers()
        );

        // case Grid grid -> new WidgetStateRequest<>(DefaultGrid::new, grid);
        // case PassiveHeight passiveHeight -> new WidgetStateRequest<>(DefaultPassiveHeight::new, passiveHeight);
        // case PassiveSize passiveSize -> new WidgetStateRequest<>(DefaultPassiveSize::new, passiveSize);
    }
}
