package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.renderer.displaylist.DisplayList;

public class TransformRenderNode extends RenderNode {

    public final MutableObservable<Mat4> transformation = MutableObservable.ofNullable();
    public final MutableObservable<RenderNode> child = MutableObservable.ofNullable();

    @Override
    public void addToDisplayList(Mat4 transform, DisplayList displayList) {
        child.get().addToDisplayList(transform.mul(transformation.get()), displayList);
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("transformation", transformation.get());
        out.child("child", child.get());
    }
}
