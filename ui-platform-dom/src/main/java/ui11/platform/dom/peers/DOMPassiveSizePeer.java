package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Slot;
import ui11.layout.singlechild.PassiveSize;
import ui11.platform.dom.*;

import java.util.List;

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
    protected List<? extends HTMLElement> children() {
        return List.of(
                contentSlot.instantiate(new DOMWidgetWrapper(passiveSize.content())).
                        lookup(DOMElementHolder.class).element()
        );
    }
}
