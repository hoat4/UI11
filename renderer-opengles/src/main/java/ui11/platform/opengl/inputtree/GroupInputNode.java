package ui11.platform.opengl.inputtree;

import ui11.geom.Vec4;
import ui11.observable.ObservableList;

public class GroupInputNode extends InputNode {

    /**
     * Legalább 2 eleme legyen. Ha csak 0 lenne, használjunk {@link TransparentInputNode}-ot helyette, ha pedig 1, akkor
     * használjuk azt a {@linkplain GroupInputNode} helyett. Ne legyen benne {@linkplain TransparentInputNode}.
     */
    public final ObservableList<InputNode> children = new ObservableList<>();

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        for (InputNode n : children.reversed()) {
            if (n.pick(pickContext, p))
                return true;
        }
        return false;
    }
}
