package ui11.layout.impl;

import org.jspecify.annotations.NonNull;
import ui11.*;
import ui11.decoration.Box;
import ui11.graphics.Empty;
import ui11.graphics.Surface;
import ui11.graphics.effect.Overlay;
import ui11.graphics.fill.ColorFill;
import ui11.layout.Gone;
import ui11.layout.helper.SingleChildLayout;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.layout.singlechild.Align;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.singlechild.Padding;

import org.jspecify.annotations.Nullable;

public class DefaultLayoutProvider extends WidgetResolver {

    @Override
    protected @Nullable Widget tryResolveGeneric(@NonNull SubstitutedWidget widget) {
        return switch (widget) {
            case Align align -> new DefaultAlignImpl(align);
            case Box box -> new DefaultBoxImpl(box);
            case Padding padding -> new DefaultPaddingImpl(padding);
            default -> null;
        };
    }

    @Override
    protected @Nullable Widget tryResolveRequestSpecific(@NonNull SubstitutedWidget widget,
                                                         PeerRequestor.@NonNull Request<?> request) {
        if (request instanceof Surface s) {
            switch (widget) {
                case LinearLayout linearLayout -> {
                    return new DefaultLinearLayoutImpl.Arranger(linearLayout, s);
                }
                case SingleChildLayout singleChildLayout -> {
                    return new DefaultSingleChildLayoutImpl.Arranger(singleChildLayout, s);
                }
                default -> {
                }
            }
        }

        if (!(request instanceof BoxLayoutResult.SizeRequest sizeRequest))
            return null;

        return switch (widget) {
            case Gone _ -> sizeRequest.createResponse(new BoxLayoutResult.OfGone());

            case ColorFill _, Empty _ -> sizeRequest.createResponse(
                    new BoxLayoutResult.OfChosenSize(sizeRequest.constraints().min()));

            case Overlay overlay -> new DefaultOverlayLayoutImpl(overlay, sizeRequest);

            case LinearLayout linearLayout -> new DefaultLinearLayoutImpl.Sizer(linearLayout, sizeRequest);

            case SingleChildLayout singleChildLayout ->
                    new DefaultSingleChildLayoutImpl.Sizer(singleChildLayout, sizeRequest);

            // case Grid grid -> new WidgetStateRequest<>(DefaultGrid::new, grid);
            // case PassiveHeight passiveHeight -> new WidgetStateRequest<>(DefaultPassiveHeight::new, passiveHeight);
            // case PassiveSize passiveSize -> new WidgetStateRequest<>(DefaultPassiveSize::new, passiveSize);

            default -> null;
        };
    }
}
