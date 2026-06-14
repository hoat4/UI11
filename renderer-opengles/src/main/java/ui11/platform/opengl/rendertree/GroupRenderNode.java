package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.observable.ObservableList;
import ui11.platform.opengl.renderer.displaylist.DisplayList;

public class GroupRenderNode extends RenderNode {

    /**
     * Legalább 2 eleme legyen. Ha csak 0 lenne, használjunk {@link EmptyRenderNode}-ot helyette, ha pedig 1, akkor
     * használjuk azt a {@linkplain GroupRenderNode} helyett. Ne legyen benne {@linkplain EmptyRenderNode}.
     */
    public final ObservableList<RenderNode> children = new ObservableList<>();

    @Override
    public void addToDisplayList(Mat4 transform, DisplayList displayList) {
        for (RenderNode n : children)
            n.addToDisplayList(transform, displayList);
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        for (int i = 0; i < children.size(); i++)
            out.child("#" + i, children.get(i));
    }
}
