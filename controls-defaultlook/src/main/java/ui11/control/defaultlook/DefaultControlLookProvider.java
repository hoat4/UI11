package ui11.control.defaultlook;

import org.jspecify.annotations.NonNull;
import ui11.Widget;
import ui11.control.*;
import ui11.control.Button.ButtonState;
import ui11.resolution.PeerCreationRequest;
import ui11.resolution.WidgetResolver;

import org.jspecify.annotations.Nullable;

public class DefaultControlLookProvider implements WidgetResolver {

    @Override
    public @Nullable Widget resolveOrNull(@NonNull Widget widget, @NonNull PeerCreationRequest<?> peerCreationRequest) {
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
