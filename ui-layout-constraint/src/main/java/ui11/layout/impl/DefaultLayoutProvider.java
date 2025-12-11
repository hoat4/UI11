package ui11.layout.impl;

import ui11.Widget;
import ui11.decoration.Box;
import ui11.graphics.Empty;
import ui11.graphics.effect.Overlay;
import ui11.graphics.fill.ColorFill;
import ui11.layout.singlechild.Align;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutProtocol;
import ui11.layout.singlechild.Padding;
import ui11.provide.UpValueWrapper;
import ui11.resolution.WidgetResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DefaultLayoutProvider implements WidgetResolver {
    @Nullable
    @Override
    public Widget resolveOrNull(Widget widget, ResolutionContext resolutionContext) {
        return switch (widget) {
            case Align align -> new DefaultAlignImpl(align);
            case Box box -> new DefaultBoxImpl(box);
            case LinearLayout linearLayout -> new DefaultLinearLayoutImpl(linearLayout);
            case Padding padding -> new DefaultPaddingImpl(padding);
            // case Grid grid -> new WidgetStateRequest<>(DefaultGrid::new, grid);
            // case PassiveHeight passiveHeight -> new WidgetStateRequest<>(DefaultPassiveHeight::new, passiveHeight);
            // case PassiveSize passiveSize -> new WidgetStateRequest<>(DefaultPassiveSize::new, passiveSize);
            default -> null;
        };
    }

    @Nonnull
    @Override
    public Widget resolveAdditional(@Nonnull Widget widget, @Nonnull Widget content) {
        if (widget instanceof ColorFill || widget instanceof Empty)
            return new UpValueWrapper((BoxLayoutProtocol) BoxConstraints::min, content);
        if (widget instanceof Overlay overlay)
            return new DefaultOverlayLayoutImpl(overlay, content);

        return content;
    }
}
