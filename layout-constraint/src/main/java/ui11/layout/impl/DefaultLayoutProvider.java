package ui11.layout.impl;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.ResolverRegistry.Priority;
import ui11.decoration.Box;
import ui11.graphics.Empty;
import ui11.graphics.fill.ColorFill;
import ui11.layout.Gone;
import ui11.layout.helper.SingleChildLayout;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Padding;

public class DefaultLayoutProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.add(Priority.EMULATED, Align.class, DefaultAlignImpl::new).
                offers(BoxLayoutResult.class).consumes(BoxLayoutResult.class);
        r.add(Priority.EMULATED, Box.class, DefaultBoxImpl::new).
                offers(BoxLayoutResult.class).consumes(BoxLayoutResult.class);
        r.add(Priority.EMULATED, Padding.class, DefaultPaddingImpl::new).
                offers(BoxLayoutResult.class).consumes(BoxLayoutResult.class);
        r.add(Priority.EMULATED, Gone.class, gone -> BoxLayoutResult.OfGone.INSTANCE).
                offers(BoxLayoutResult.class);
        r.add(Priority.EMULATED, LinearLayout.class, DefaultLinearLayoutImpl::new).
                offers(BoxLayoutResult.class).consumes(BoxLayoutResult.class);
        r.add(Priority.EMULATED, SingleChildLayout.class, DefaultSingleChildLayoutImpl::new).
                offers(BoxLayoutResult.class).consumes(BoxLayoutResult.class);
        r.add(Priority.EMULATED, ColorFill.class, _ -> new PreferredSizeIsMinimal()).
                offers(BoxLayoutResult.class);
        r.add(Priority.EMULATED, Empty.class, _ -> new PreferredSizeIsMinimal()).
                offers(BoxLayoutResult.class);

        // case Grid grid -> new WidgetStateRequest<>(DefaultGrid::new, grid);
        // case PassiveHeight passiveHeight -> new WidgetStateRequest<>(DefaultPassiveHeight::new, passiveHeight);
        // case PassiveSize passiveSize -> new WidgetStateRequest<>(DefaultPassiveSize::new, passiveSize);
    }
}
