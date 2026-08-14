package ui11.control.defaultlook;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.control.*;
import ui11.control.Button.ButtonState;

public class DefaultControlLookProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.addPeerIndependent(null, Button.class, DefaultButtonBehavior::new);
        r.addPeerIndependent(null, ButtonState.class, DefaultButtonLook::new);
        r.addPeerIndependent(null, CheckBox.class, DefaultCheckBoxImpl::new);
        r.addPeerIndependent(null, ComboBox.class, DefaultComboBoxLook::new);
        r.addPeerIndependent(null, PlainTextEditor.class, DefaultPlainTextEditorImpl::new);
        r.addPeerIndependent(null, Hyperlink.class, DefaultHyperlinkPeer::new);
        r.addPeerIndependent(null, Slider.class, DefaultSliderLook::new);
        r.addPeerIndependent(null, TabbedPane.class, DefaultTabbedPaneLook::new);
        r.addPeerIndependent(null, Table.class, DefaultTableImpl::new);
        r.addPeerIndependent(null, TextField.class, DefaultTextFieldImpl::new);
    }
}
