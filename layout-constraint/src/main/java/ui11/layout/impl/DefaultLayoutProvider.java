package ui11.layout.impl;

import org.jspecify.annotations.NonNull;
import ui11.PeerCreationRequest;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.decoration.Box;
import ui11.graphics.Empty;
import ui11.graphics.effect.Overlay;
import ui11.graphics.fill.ColorFill;
import ui11.layout.Gone;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.layout.singlechild.Align;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.singlechild.Padding;
import ui11.WidgetResolver;

import org.jspecify.annotations.Nullable;

import static ui11.graphics.Empty.empty;

public class DefaultLayoutProvider extends WidgetResolver {

    @Override
    protected Class<? extends SubstitutedWidget> supportedTargetType() {
        return SubstitutedWidget.class; // TODO
    }

    @Override
    public @Nullable Widget resolveOrNull(@NonNull Widget widget) {
        return switch (widget) {
            case Align align -> new DefaultAlignImpl(align);
            case Box box -> new DefaultBoxImpl(box);
            case LinearLayout linearLayout -> new DefaultLinearLayoutImpl.Sizer(linearLayout);
            case Padding padding -> new DefaultPaddingImpl(padding);
            case Gone _ -> new BoxLayoutResult.OfGone(empty());
            // case Grid grid -> new WidgetStateRequest<>(DefaultGrid::new, grid);
            // case PassiveHeight passiveHeight -> new WidgetStateRequest<>(DefaultPassiveHeight::new, passiveHeight);
            // case PassiveSize passiveSize -> new WidgetStateRequest<>(DefaultPassiveSize::new, passiveSize);
            default -> null;
        };
    }

    @Override
    protected @NonNull Widget resolveAdditional(@NonNull SubstitutedWidget widget,
                                                @NonNull PeerCreationRequest<?> peerCreationRequest,
                                                @NonNull Widget peer) {
        if (peerCreationRequest instanceof BoxLayoutResult.SizeRequest sizeRequest) {
            switch (widget) {
                case ColorFill _, Empty _ -> {
                    return new BoxLayoutResult.OfChosenSize(sizeRequest.constraints().min(), peer);
                }
                case Overlay overlay -> {
                    return new DefaultOverlayLayoutImpl(overlay, sizeRequest, peer);
                }
                default -> {
                }
            }
        }
        return peer;
    }
}
