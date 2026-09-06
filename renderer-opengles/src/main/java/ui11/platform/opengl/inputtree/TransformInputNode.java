package ui11.platform.opengl.inputtree;

import ui11.geom.Mat4;
import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.renderer.input.InputNode;

import java.util.Objects;

public class TransformInputNode extends InputNode {

    public final MutableObservable<Mat4> inverseMatrix = MutableObservable.ofNullable();
    public final MutableObservable<InputNode> child = MutableObservable.ofNullable();

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        Mat4 m = inverseMatrix.get();
        Objects.requireNonNull(m);

        Vec4 result = m.mul(p);
        return child.get().pick(pickContext, result);
    }
}
