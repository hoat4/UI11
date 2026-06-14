package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.geom.Vec2;
import ui11.color.Color;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.BufferPool;
import ui11.platform.opengl.renderer.Shaders;
import ui11.platform.opengl.renderer.displaylist.DisplayList;
import ui11.platform.opengl.renderer.displaylist.SolidTrianglesItem;

import java.nio.ByteBuffer;

public class FillTrianglesWithColorRenderNode extends RenderNode {

    // lásd SolidPolygonShader.
    // 2 float pozíció, majd 4 byte szín
    public final MutableObservable<BufferPool.ReleaseableBuffer> vertices = MutableObservable.ofNullable();

    @Override
    public void addToDisplayList(Mat4 transform, DisplayList displayList) {
        displayList.items.add(new SolidTrianglesItem(transform, vertices.get()));
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("vertices", Shaders.SolidPolygonShader.debugPrint(vertices.snoop().buffer()));
    }
}
