package ui11.control.defaultlook;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.control.*;
import ui11.control.Button.ButtonState;

public class DefaultControlLookProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.addPeerIndependent(Button.class, DefaultButtonBehavior::new);
        r.addPeerIndependent(ButtonState.class, DefaultButtonLook::new);
        r.addPeerIndependent(CheckBox.class, DefaultCheckBoxImpl::new);
        r.addPeerIndependent(ComboBox.class, DefaultComboBoxLook::new);
        r.addPeerIndependent(PlainTextEditor.class, DefaultPlainTextEditorImpl::new);
        r.addPeerIndependent(Hyperlink.class, DefaultHyperlinkPeer::new);
        r.addPeerIndependent(Slider.class, DefaultSliderLook::new);
        r.addPeerIndependent(TabbedPane.class, DefaultTabbedPaneLook::new);
        r.addPeerIndependent(Table.class, DefaultTableImpl::new);
        r.addPeerIndependent(TextField.class, DefaultTextFieldImpl::new);
    }
}
