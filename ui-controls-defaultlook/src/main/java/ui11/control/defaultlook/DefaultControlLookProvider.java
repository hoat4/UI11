package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.*;
import ui11.control.Button.ButtonState;
import ui11.resolution.WidgetResolver;

import javax.annotation.Nullable;

public class DefaultControlLookProvider implements WidgetResolver {

    @Nullable
    @Override
    public Widget resolveOrNull(Widget widget, ResolutionContext resolutionContext) {
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
