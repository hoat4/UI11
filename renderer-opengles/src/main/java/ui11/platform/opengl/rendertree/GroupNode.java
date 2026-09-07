package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.geom.Vec4;
import ui11.observable.ObservableList;
import ui11.platform.opengl.renderer.displaylist.DisplayList;
import ui11.renderer.input.InputNode;

public class GroupNode extends GLNode {

    /**
     * Legalább 2 eleme legyen. Ha csak 0 lenne, használjunk {@link EmptyNode}-ot helyette, ha pedig 1, akkor
     * használjuk azt a {@linkplain GroupNode} helyett. Ne legyen benne {@linkplain EmptyNode}.
     */
    public final ObservableList<GLNode> children = new ObservableList<>();

    @Override
    public void addToDisplayList(Mat4 transform, DisplayList displayList) {
        for (GLNode n : children)
            n.addToDisplayList(transform, displayList);
    }

    @Override
    public boolean pick(InputNode.PickContext pickContext, Vec4 p) {
        for (GLNode n : children.reversed()) {
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

    /*
    TODO

    private Widget doBuild(List<? extends GLNode> children) {
        List<GLNode> childRenderNodes = new ArrayList<>();

        List<FillTrianglesWithColorNode> fillTrianglesNodes = new ArrayList<>();
        for (GLNode h : children) {
            switch (h) {
                case EmptyNode emptyRenderNode -> {
                    // nothing to do
                }
                case FillTrianglesWithColorNode fillTriangles -> {
                    fillTrianglesNodes.add(fillTriangles);
                }
                default -> {
                    mergeInto(fillTrianglesNodes, childRenderNodes);
                    fillTrianglesNodes.clear();
                    childRenderNodes.add(h);
                }
            }
        }
        mergeInto(fillTrianglesNodes, childRenderNodes);
        mergedNodeCache.values().retainAll(childRenderNodes);

        return parentSurface.createResponse(
                switch (childRenderNodes.size()) {
                    case 0 -> EmptyNode.INSTANCE;
                    case 1 -> childRenderNodes.getFirst();
                    default -> {
                        groupNode.children.setAll(childRenderNodes);
                        yield groupNode;
                    }
                }
        );
    }

    private void mergeInto(List<FillTrianglesWithColorNode> nodes, List<GLNode> out) {
        if (nodes.isEmpty())
            return;
        if (nodes.size() == 1) {
            out.add(nodes.getFirst());
            return;
        }
        int sumSize = 0;
        for (FillTrianglesWithColorNode f : nodes) {
            ByteBuffer b = f.vertices.get().buffer();
            assert b.position() == 0;
            sumSize += b.limit();
        }

        BufferPool.GrowableVertexBuffer buf = bufferPool.get().allocate(sumSize);
        for (FillTrianglesWithColorNode n : nodes) {
            buf.put(n.vertices.get().buffer());
        }

        FillTrianglesWithColorNode newNode = mergedNodeCache.computeIfAbsent(
                Set.copyOf(nodes), __ -> new FillTrianglesWithColorNode());
        newNode.vertices.set(buf.finish());
        out.add(newNode);
    }
     */
}
