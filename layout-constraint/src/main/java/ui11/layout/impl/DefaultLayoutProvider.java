package ui11.layout.impl;

import ui11.*;
import ui11.decoration.Box;
import ui11.graphics.Empty;
import ui11.graphics.Surface;
import ui11.graphics.fill.ColorFill;
import ui11.layout.Gone;
import ui11.layout.helper.SingleChildLayout;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.layout.protocol.BoxLayoutResult.SizeRequest;
import ui11.layout.singlechild.Align;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.singlechild.Padding;

import java.util.Set;

public class DefaultLayoutProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.addPeerIndependent(Align.class, DefaultAlignImpl::new);
        r.addPeerIndependent(Box.class, DefaultBoxImpl::new);
        r.addPeerIndependent(Padding.class, DefaultPaddingImpl::new);
        r.addPeerDependent(Surface.class, LinearLayout.class, DefaultLinearLayoutImpl.Arranger::new);
        r.addPeerDependent(Surface.class, SingleChildLayout.class, DefaultSingleChildLayoutImpl.Arranger::new);
        r.addPeerDependent(SizeRequest.class, Gone.class, (gone, sizeRequest) ->
                sizeRequest.createResponse(new BoxLayoutResult.OfGone()));
        r.addPeerDependent(SizeRequest.class, Set.of(ColorFill.class, Empty.class),
                sizeRequest -> sizeRequest.createResponse(
                        new BoxLayoutResult.OfChosenSize(sizeRequest.constraints().min())));
        r.addPeerDependent(SizeRequest.class, LinearLayout.class, DefaultLinearLayoutImpl.Sizer::new);
        r.addPeerDependent(SizeRequest.class, SingleChildLayout.class, DefaultSingleChildLayoutImpl.Sizer::new);

        // case Grid grid -> new WidgetStateRequest<>(DefaultGrid::new, grid);
        // case PassiveHeight passiveHeight -> new WidgetStateRequest<>(DefaultPassiveHeight::new, passiveHeight);
        // case PassiveSize passiveSize -> new WidgetStateRequest<>(DefaultPassiveSize::new, passiveSize);
    }
}
