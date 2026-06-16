package ui11.platform.dom.peers;

import ui11.Slot;
import ui11.Widget;
import ui11.layout.singlechild.PassiveSize;
import ui11.platform.dom.DOMLayoutPeerBase;

public class DOMPassiveSizePeer extends DOMLayoutPeerBase {

    private static final String CLASS_PASSIVE_SIZE_CONTAINER = "PS";

    private final PassiveSize passiveSize;

    @Inject private Slot contentSlot;

    public DOMPassiveSizePeer(PassiveSize passiveSize) {
        super(false, false);
        this.passiveSize = passiveSize;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(CLASS_PASSIVE_SIZE_CONTAINER);
    }

    @Override
    protected Widget doBuild() {
        return updateToSingleChild(passiveSize.content());
    }
}
