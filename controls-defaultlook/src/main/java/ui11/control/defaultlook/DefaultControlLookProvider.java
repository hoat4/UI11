package ui11.control.defaultlook;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.ResolverRegistry.Priority;
import ui11.control.*;
import ui11.control.Button.ButtonState;

public class DefaultControlLookProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.add(Priority.THEME, Button.class, DefaultButtonBehavior::new);
        r.add(Priority.THEME, ButtonState.class, DefaultButtonLook::new);
        r.add(Priority.THEME, CheckBox.class, DefaultCheckBoxImpl::new);
        r.add(Priority.THEME, ComboBox.class, DefaultComboBoxLook::new);
        r.add(Priority.THEME, PlainTextEditor.class, DefaultPlainTextEditorImpl::new);
        r.add(Priority.THEME, Hyperlink.class, DefaultHyperlinkPeer::new);
        r.add(Priority.THEME, Slider.class, DefaultSliderLook::new);
        r.add(Priority.THEME, TabbedPane.class, DefaultTabbedPaneLook::new);
        r.add(Priority.THEME, Table.class, DefaultTableImpl::new);
        r.add(Priority.THEME, TextField.class, DefaultTextFieldImpl::new);
    }
}
