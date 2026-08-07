package ui11.platform.opengl.peer;

import ui11.PeerRequestor;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.effect.Overlay;
import ui11.observable.Observable;
import ui11.platform.opengl.BufferPool;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.GLSurface;
import ui11.platform.opengl.GLSurface.ShapeInheritingGLSurface;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.inputtree.*;
import ui11.platform.opengl.rendertree.EmptyRenderNode;
import ui11.platform.opengl.rendertree.FillTrianglesWithColorRenderNode;
import ui11.platform.opengl.rendertree.GroupRenderNode;
import ui11.platform.opengl.rendertree.RenderNode;
import ui11.provide.Provider;

import java.nio.ByteBuffer;
import java.util.*;

public class GLOverlayPeer extends Widget {

    private final Overlay overlay;
    private final GLSurface parentSurface;

    @Inject private Observable<BufferPool> bufferPool;

    @Remember private List<GLSurface> childSurfaces;
    @Remember private GroupRenderNode groupNode;
    @Remember private GroupInputNode groupInputNode;
    @Remember private Map<Set<FillTrianglesWithColorRenderNode>, FillTrianglesWithColorRenderNode> mergedNodeCache;

    public GLOverlayPeer(Overlay overlay, GLSurface surface) {
        this.overlay = overlay;
        this.parentSurface = surface;
    }

    @Override
    protected void initState() {
        childSurfaces = new ArrayList<>();
        groupNode = new GroupRenderNode();
        groupInputNode = new GroupInputNode();
        mergedNodeCache = new HashMap<>();
    }

    @Override
    protected Widget build() {
        for (int i = 0; i < overlay.items().size(); i++) {
            if (i == childSurfaces.size())
                childSurfaces.add(new ShapeInheritingGLSurface());

            GLSurface surface = childSurfaces.get(i);
            surface.parent.set(parentSurface);
        }

        if (childSurfaces.size() > overlay.items().size())
            childSurfaces.subList(overlay.items().size(), childSurfaces.size()).clear();

        return PeerRequestor.ofMultiple(overlay.items(), childSurfaces,
                results -> doBuild(results.stream().
                        map(PeerRequestor.Result::peer).toList()));
    }

    private Widget doBuild(List<? extends GLNodeHolder> children) {
        List<RenderNode> childRenderNodes = new ArrayList<>();
        List<InputNode> childInputNodes = new ArrayList<>();

        Shape2D shape = ((GLSurface) parentSurface).shape();

        List<FillTrianglesWithColorRenderNode> fillTrianglesNodes = new ArrayList<>();
        for (GLNodeHolder h : children) {
            switch (h.renderNode()) {
                case EmptyRenderNode emptyRenderNode -> {
                    // nothing to do
                }
                case FillTrianglesWithColorRenderNode fillTriangles -> {
                    fillTrianglesNodes.add(fillTriangles);
                }
                default -> {
                    mergeInto(fillTrianglesNodes, childRenderNodes);
                    fillTrianglesNodes.clear();
                    childRenderNodes.add(h.renderNode());
                }
            }
            if (!(h.inputNode() instanceof TransparentInputNode)) {
                if (isOpaque(h.inputNode(), shape))
                    childInputNodes.clear();
                childInputNodes.add(h.inputNode());
            }
        }
        mergeInto(fillTrianglesNodes, childRenderNodes);
        mergedNodeCache.values().retainAll(childRenderNodes);

        return parentSurface.createResponse(new GLNodeHolder(
                switch (childRenderNodes.size()) {
                    case 0 -> EmptyRenderNode.INSTANCE;
                    case 1 -> childRenderNodes.getFirst();
                    default -> {
                        groupNode.children.setAll(childRenderNodes);
                        yield groupNode;
                    }
                },
                switch (childInputNodes.size()) {
                    case 0 -> TransparentInputNode.INSTANCE;
                    case 1 -> childInputNodes.getFirst();
                    default -> {
                        groupInputNode.children.setAll(childInputNodes);
                        yield groupInputNode;
                    }
                }
        ));
    }

    private void mergeInto(List<FillTrianglesWithColorRenderNode> nodes, List<RenderNode> out) {
        if (nodes.isEmpty())
            return;
        if (nodes.size() == 1) {
            out.add(nodes.getFirst());
            return;
        }
        int sumSize = 0;
        for (FillTrianglesWithColorRenderNode f : nodes) {
            ByteBuffer b = f.vertices.get().buffer();
            assert b.position() == 0;
            sumSize += b.limit();
        }

        BufferPool.GrowableVertexBuffer buf = bufferPool.get().allocate(sumSize);
        for (FillTrianglesWithColorRenderNode n : nodes) {
            buf.put(n.vertices.get().buffer());
        }

        FillTrianglesWithColorRenderNode newNode = mergedNodeCache.computeIfAbsent(
                Set.copyOf(nodes), __ -> new FillTrianglesWithColorRenderNode());
        newNode.vertices.set(buf.finish());
        out.add(newNode);
    }

    private static boolean isOpaque(InputNode node, Shape2D shape) {
        while (node instanceof ListenerInputNode listenerInputNode)
            node = listenerInputNode.child.get();
        return node instanceof OpaqueInputNode opaqueInputNode && opaqueInputNode.shape.get().equals(shape);
    }
}
