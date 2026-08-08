package ui11.control.defaultlook;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.*;
import ui11.control.*;
import ui11.control.Button.ButtonState;

public class DefaultControlLookProvider extends WidgetResolver {

    @Override
    protected Class<PeerRequest<?>> requestType() {
        // TODO
        return (Class<PeerRequest<?>>) (Class<?>) PeerRequest.class;
    }

    @Override
    protected @Nullable Widget resolveOrNull(@NonNull SubstitutedWidget widget,
                                             @NonNull PeerRequest<?> request) {
        return switch (widget) {
            case Button button -> new DefaultButtonBehavior(button);
            case ButtonState buttonState -> new DefaultButtonLook(buttonState);
            case CheckBox checkBox -> new DefaultCheckBoxImpl(checkBox);
            case ComboBox<?> comboBox -> new DefaultComboBoxLook<>(comboBox);
            case PlainTextEditor plainTextEditor -> new DefaultPlainTextEditorImpl(plainTextEditor);
            case Hyperlink hyperlink -> new DefaultHyperlinkPeer(hyperlink);
            case Slider slider -> new DefaultSliderLook(slider);
            case TabbedPane table -> new DefaultTabbedPaneLook(table);
            case Table<?> table -> new DefaultTableImpl<>(table);
            case TextField textField -> new DefaultTextFieldImpl(textField);
            default -> null;
        };
    }
}
