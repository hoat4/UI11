package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.renderer.displaylist.DisplayList;
import ui11.renderer.input.InputNode;

public class ClipNode extends GLNode {

    public final MutableObservable<Shape2D> shape = MutableObservable.ofNullable();
    public final MutableObservable<GLNode> content = MutableObservable.ofNullable();

    @Override
    public void addToDisplayList(Mat4 transform, DisplayList displayList) {
        throw new RuntimeException("TODO");
    }

    @Override
    public boolean pick(InputNode.PickContext pickContext, Vec4 p) {
        if (!shape.get().contains(p.x(), p.y()))
            return false;
        return content.get().pick(pickContext, p);
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("shape", shape.get());
        out.child("content", content.get());
    }
}
