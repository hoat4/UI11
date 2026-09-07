package ui11.renderer.j2d.rendertree;

import ui11.geom.Vec4;
import ui11.observable.ObservableList;
import ui11.renderer.input.InputNode;
import ui11.renderer.j2d.RenderingContext;

public class GroupNode extends J2DNode {

    /**
     * Legalább 2 eleme legyen. Ha csak 0 lenne, használjunk {@link EmptyNode}-ot helyette, ha pedig 1, akkor
     * használjuk azt a {@linkplain GroupNode} helyett. Ne legyen benne {@linkplain EmptyNode}.
     */
    public final ObservableList<J2DNode> children = new ObservableList<>();

    @Override
    public void render(RenderingContext ctx) {
        for (J2DNode n : children)
            n.render(ctx);
    }

    @Override
    public boolean pick(InputNode.PickContext pickContext, Vec4 p) {
        for (J2DNode n : children.reversed()) {
            if (n.pick(pickContext, p))
                return true;
        }
        return false;
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        for (int i = 0; i < children.size(); i++)
            out.child("#" + i, children.get(i));
    }
}
