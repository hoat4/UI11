package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.MultiSlot;
import ui11.Widget;
import ui11.layout.multichild.flow.Flow;
import ui11.platform.dom.*;

import java.util.ArrayList;
import java.util.List;

public class DOMFlowLayoutPeer extends DOMLayoutPeerBase {

    static final String CLASS_FLOW = "fl";

    private final Flow flow;

    @Inject private MultiSlot<Integer> slots;

    public DOMFlowLayoutPeer(Flow flow) {
        super(false, false);
        this.flow = flow;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(CLASS_FLOW);
    }

    @Override
    protected List<? extends HTMLElement> children() {
        List<HTMLElement> childElements = new ArrayList<>();
        List<? extends Widget> widgets = flow.items();
        for (int i = 0; i < widgets.size(); i++) {
            Widget o = widgets.get(i);
            if (o != null)
                childElements.add(slots.instantiate(i, new DOMWidgetWrapper(o)).lookup(DOMElementHolder.class).element());
        }
        return childElements;
    }
}
