package ui11.platform.dom.peers;

import ui11.Widget;
import ui11.layout.multichild.flow.Flow;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;

public class DOMFlowLayoutPeer extends DOMLayoutPeerBase {

    static final String CLASS_FLOW = "fl";

    private final Flow flow;

    public DOMFlowLayoutPeer(Flow flow) {
        super(false, false);
        this.flow = flow;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(CLASS_FLOW);
    }

    @Override
    protected Widget doBuild() {
        return makePeers(flow.items(), hList ->
                updateChildren(hList.stream().map(DOMElementHolder::element).toList()));
    }
}
