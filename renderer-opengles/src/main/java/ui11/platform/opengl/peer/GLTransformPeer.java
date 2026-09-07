package ui11.platform.opengl.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Mat4;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.graphics.effect.Transform;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.GLVisualContentRequest.GLSurfaceWithOwnShape;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.rendertree.EmptyNode;
import ui11.platform.opengl.rendertree.GLNode;
import ui11.platform.opengl.rendertree.TransformNode;

public class GLTransformPeer extends Widget {

    private final Transform transform;

    @Inject private GLVisualContentRequest parentSurface;

    @Remember private TransformedSurface surface;
    @Remember private TransformNode node;

    public GLTransformPeer(Transform transform) {
        this.transform = transform;
    }

    @Override
    protected void initState() {
        surface = new TransformedSurface();
        node = new TransformNode();
    }

    @Override
    protected Widget build() {
        // TODO mi történjen, ha 0-ra scaleelünk egy ColorFillt vagy hasonlót (aminek végtelen a mérete)?
        //      most jelenleg eltüntetjük. ha mégsem kéne eltüntetni, módosítsuk lent a kódot.

        surface.parent.set(parentSurface);
        boolean nonDegenerateTransform = surface.update(transform.transformation());

        if (surface.renderNodeTranslation.snoop() != null) {
            // ilyenkor nem kell TransformNode
            return PeerRequest.requestSingle(transform.content(), surface, parentSurface::createResponse);
        }

        // ezt a size beállítás után kell, hogy child tudja hivatkozni VisualContentRequest.size-on keresztül.
        // degenerateTransform esetén is végrehajtjuk, mert általában animáció közben keletkezhetnek
        // pl. 0-s scaleek, ettől nem kell a child widgetnek pause meg resume-ot kapnia.

        return PeerRequest.requestSingle(transform.content(), surface, result -> {
            return parentSurface.createResponse(
                    nonDegenerateTransform ?
                            makeNode(result) :
                            EmptyNode.INSTANCE
            );
        });
    }

    private GLNode makeNode(GLNode childNode) {
        if (transform.transformation().isIdentity() || childNode instanceof EmptyNode)
            return childNode;

        Mat4 tx = surface.matrix;

        if (childNode instanceof TransformNode childTransformNode) {
            node.child.set(childTransformNode.child.get());
            tx = tx.mul(childTransformNode.transformation.get());
            node.transformation.set(tx);
            node.inverseMatrix.set(childTransformNode.inverseMatrix.get().mul(surface.inverseMatrix));

            // lehetne még pl. Transform-Clip-Transform-... láncokat összevonni
        } else {
            node.child.set(childNode);
            node.transformation.set(tx);
            node.inverseMatrix.set(surface.inverseMatrix);
        }

        return node;
    }

    private static class TransformedSurface extends GLSurfaceWithOwnShape {

        private Mat4 matrix, inverseMatrix;
        private Shape2D prevParentShape;
        private final MutableObservable<Shape2D> shape = MutableObservable.ofNullable();
        private final MutableObservable<Vec2> renderNodeTranslation = MutableObservable.ofNullable();

        boolean update(Mat4 t) {
            Shape2D parentShape = parent.get().shape();
            if (parentShape == Shape2D.InfinitePlane.INFINITE_PLANE) {
                prevParentShape = Shape2D.InfinitePlane.INFINITE_PLANE;
                shape.set(Shape2D.InfinitePlane.INFINITE_PLANE);
                renderNodeTranslation.set(null);
                return false;
            }
            if (!parentShape.equals(prevParentShape) || !t.equals(matrix)) {
                this.matrix = t;

                inverseMatrix = t.inverseOrNull();

                if (inverseMatrix == null)
                    shape.set(Shape2D.InfinitePlane.INFINITE_PLANE);
                else
                    shape.set(parentShape.transform(inverseMatrix));

                prevParentShape = parentShape;
            }

            if (t.isAtMost2DTranslation())
                renderNodeTranslation.set(new Vec2(t.m30(), t.m31()).plus(parent.get().renderNodeTranslation()));
            else
                renderNodeTranslation.set(null);

            return shape.get() != Shape2D.InfinitePlane.INFINITE_PLANE;
        }

        @Override
        public Shape2D shape() {
            if (shape.get() == null)
                throw new IllegalStateException();
            return shape.get();
        }

        @Override
        public Size size() {
            throw new RuntimeException("TODO");
        }

        @Override
        public CoordinateSpace coordinateSpace() {
            throw new RuntimeException("TODO");
        }

        @Override
        public Vec2 renderNodeTranslation() {
            Vec2 t = renderNodeTranslation.get();
            return t == null ? Vec2.ZERO : t;
        }
    }
}
