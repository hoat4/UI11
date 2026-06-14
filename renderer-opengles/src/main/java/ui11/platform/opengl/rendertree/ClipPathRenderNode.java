package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.renderer.displaylist.DisplayList;

public class ClipPathRenderNode extends RenderNode {

    public final MutableObservable<Shape2D> shape = MutableObservable.ofNullable();
    public final MutableObservable<RenderNode> content = MutableObservable.ofNullable();

    @Override
    public void addToDisplayList(Mat4 transform, DisplayList displayList) {
        throw new RuntimeException("TODO");
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("shape", shape.get());
        out.child("content", content.get());
    }
}
