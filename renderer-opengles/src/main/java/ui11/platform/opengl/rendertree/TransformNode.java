package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.renderer.displaylist.DisplayList;
import ui11.renderer.input.PickContext;

import java.util.Objects;

public class TransformNode extends GLNode {

    public final MutableObservable<Mat4> transformation = MutableObservable.ofNullable();
    public final MutableObservable<Mat4> inverseMatrix = MutableObservable.ofNullable();
    public final MutableObservable<GLNode> child = MutableObservable.ofNullable();

    @Override
    public void addToDisplayList(Mat4 transform, DisplayList displayList) {
        child.get().addToDisplayList(transform.mul(transformation.get()), displayList);
    }

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        Mat4 m = inverseMatrix.get();
        Objects.requireNonNull(m);

        Vec4 result = m.mul(p);
        return child.get().pick(pickContext, result);
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("transformation", transformation.get());
        out.child("child", child.get());
    }
}
