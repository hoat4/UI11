package ui11.layout.impl;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.decoration.Box;
import ui11.graphics.Empty;
import ui11.graphics.effect.Overlay;
import ui11.graphics.fill.ColorFill;
import ui11.layout.singlechild.Align;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.singlechild.Padding;
import ui11.WidgetResolver;

import org.jspecify.annotations.Nullable;

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
            case LinearLayout linearLayout -> new DefaultLinearLayoutImpl(linearLayout);
            case Padding padding -> new DefaultPaddingImpl(padding);
            // case Grid grid -> new WidgetStateRequest<>(DefaultGrid::new, grid);
            // case PassiveHeight passiveHeight -> new WidgetStateRequest<>(DefaultPassiveHeight::new, passiveHeight);
            // case PassiveSize passiveSize -> new WidgetStateRequest<>(DefaultPassiveSize::new, passiveSize);
            default -> null;
        };
    }

    @Override
    public @NonNull Widget resolveAdditional(@NonNull SubstitutedWidget widget, @NonNull Widget content) {
        if (widget instanceof ColorFill || widget instanceof Empty)
            return new PreferredSizeIsMinimal(content);
        if (widget instanceof Overlay overlay)
            return new DefaultOverlayLayoutImpl(overlay, content);

        return content;
    }
}
