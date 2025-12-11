package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.Hyperlink;
import ui11.input.gesture.ClickListener;
import ui11.window.Shell;

public final class DefaultHyperlinkPeer extends Widget {

    private final Hyperlink hyperlink;

    @Inject private Shell shell;

    public DefaultHyperlinkPeer(Hyperlink hyperlink) {
        this.hyperlink = hyperlink;
    }

    @Override
    protected Widget build() {
        return new ClickListener(
                hyperlink.content(),
                () -> shell.openURL(hyperlink.target())
        );
    }
}
