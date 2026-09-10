package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.BufferPool;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.renderer.Shaders;
import ui11.platform.opengl.renderer.displaylist.DisplayList;
import ui11.platform.opengl.renderer.displaylist.SolidTrianglesItem;
import ui11.renderer.input.PickContext;

public class FillTrianglesWithColorNode extends GLNode {

    // lásd SolidPolygonShader.
    // 2 float pozíció, majd 4 byte szín
    public final MutableObservable<BufferPool.ReleaseableBuffer> vertices = MutableObservable.ofNullable();

    public final MutableObservable<Shape2D> shape = MutableObservable.ofNullable(); // inputra van használva

    @Override
    public void addToDisplayList(Mat4 transform, DisplayList displayList) {
        BufferPool.ReleaseableBuffer buffer = vertices.get();
        if (buffer != null)
            displayList.items.add(new SolidTrianglesItem(transform, buffer));
    }

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        if (shape.get().contains(p.x(), p.y()))
            return pickContext.addResult();
        else
            return false;
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("vertices", Shaders.SolidPolygonShader.debugPrint(vertices.snoop().buffer()));
    }
}
