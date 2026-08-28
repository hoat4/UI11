package ui11.control.defaultlook;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.control.*;
import ui11.control.Button.ButtonState;

public class DefaultControlLookProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.add(Button.class, DefaultButtonBehavior::new);
        r.add(ButtonState.class, DefaultButtonLook::new);
        r.add(CheckBox.class, DefaultCheckBoxImpl::new);
        r.add(ComboBox.class, DefaultComboBoxLook::new);
        r.add(PlainTextEditor.class, DefaultPlainTextEditorImpl::new);
        r.add(Hyperlink.class, DefaultHyperlinkPeer::new);
        r.add(Slider.class, DefaultSliderLook::new);
        r.add(TabbedPane.class, DefaultTabbedPaneLook::new);
        r.add(Table.class, DefaultTableImpl::new);
        r.add(TextField.class, DefaultTextFieldImpl::new);
    }
}
