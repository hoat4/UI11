package ui11.control.defaultlook;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.control.*;
import ui11.control.Button.ButtonState;

public class DefaultControlLookProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.register(Button.class, DefaultButtonBehavior::new);
        r.register(ButtonState.class, DefaultButtonLook::new);
        r.register(CheckBox.class, DefaultCheckBoxImpl::new);
        r.register(ComboBox.class, DefaultComboBoxLook::new);
        r.register(PlainTextEditor.class, DefaultPlainTextEditorImpl::new);
        r.register(Hyperlink.class, DefaultHyperlinkPeer::new);
        r.register(Slider.class, DefaultSliderLook::new);
        r.register(TabbedPane.class, DefaultTabbedPaneLook::new);
        r.register(Table.class, DefaultTableImpl::new);
        r.register(TextField.class, DefaultTextFieldImpl::new);
    }
}
