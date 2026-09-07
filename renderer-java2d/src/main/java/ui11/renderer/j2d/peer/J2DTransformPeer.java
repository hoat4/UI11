package ui11.renderer.j2d.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Mat4;
import ui11.geom.Size;
import ui11.graphics.effect.Transform;
import ui11.observable.MutableObservable;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.J2DVisualContentRequest.J2DSurfaceWithOwnShape;
import ui11.renderer.j2d.rendertree.EmptyNode;
import ui11.renderer.j2d.rendertree.J2DNode;
import ui11.renderer.j2d.rendertree.TransformNode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

public class J2DTransformPeer extends Widget {

    private final Transform transform;

    @Inject private J2DVisualContentRequest parentSurface;

    @Remember private TransformedSurface childSurface;
    @Remember private TransformNode node;

    public J2DTransformPeer(Transform transform) {
        this.transform = transform;
    }

    @Override
    protected void initState() {
        childSurface = new TransformedSurface();
        node = new TransformNode();
    }

    @Override
    protected Widget build() {
        // TODO mi történjen, ha 0-ra scaleelünk egy ColorFillt vagy hasonlót (aminek végtelen a mérete)?
        //      most jelenleg eltüntetjük. ha mégsem kéne eltüntetni, módosítsuk lent a kódot.

        childSurface.parent.set(parentSurface);
        boolean nonDegenerateTransform = childSurface.update(transform.transformation());

        // ezt a size beállítás után kell, hogy child tudja hivatkozni VisualContentRequest.size-on keresztül.
        // degenerateTransform esetén is végrehajtjuk, mert általában animáció közben keletkezhetnek
        // pl. 0-s scaleek, ettől nem kell a child widgetnek pause meg resume-ot kapnia.

        return PeerRequest.requestSingle(transform.content(), childSurface, result -> {
            return parentSurface.createResponse(
                    nonDegenerateTransform ?
                            makeNode(result) :
                            EmptyNode.INSTANCE
            );
        });
    }

    private J2DNode makeNode(J2DNode childNode) {
        if (transform.transformation().isIdentity() || childNode instanceof EmptyNode)
            return childNode;

        AffineTransform tx = childSurface.awtAffineTransformation;

        if (childNode instanceof TransformNode childTransformNode) {
            node.child.set(childTransformNode.child.get());
            tx = new AffineTransform(tx);
            tx.concatenate(childTransformNode.transformation.get());
            node.transformation.set(tx);

            // lehetne még pl. Transform-Clip-Transform-... láncokat összevonni
        } else {
            node.child.set(childNode);
            // másolni kell, különben node.transformation nem venné észre a változást.
            // alternatíva, ha IP-t használunk, és akkor csak a double mezők értékét kell áttölteni a másikba
            node.transformation.set(new AffineTransform(tx));
        }
        return node;
    }

    private static class TransformedSurface extends J2DSurfaceWithOwnShape {

        private final AffineTransform awtAffineTransformation = new AffineTransform();
        private Shape prevParentShape;
        private final MutableObservable<Shape> shape = MutableObservable.ofNullable();

        boolean update(Mat4 t) {
            Shape parentShape = parent.get().shape();
            if (parentShape == J2DVisualContentRequest.INFINITE_SHAPE) {
                prevParentShape = J2DVisualContentRequest.INFINITE_SHAPE;
                shape.set(J2DVisualContentRequest.INFINITE_SHAPE);
                return false;
            }
            if (!parentShape.equals(prevParentShape) ||
                    !equals2DComponents(awtAffineTransformation, t)) {
                awtAffineTransformation.setTransform(
                        t.m00(), t.m01(),
                        t.m10(), t.m11(),
                        t.m30(), t.m31()
                );
                try {
                    shape.set(awtAffineTransformation.createInverse().createTransformedShape(parentShape));
                } catch (NoninvertibleTransformException noninvertibleTransformException) {
                    shape.set(J2DVisualContentRequest.INFINITE_SHAPE);
                }
                prevParentShape = parentShape;
            }
            return shape.get() != J2DVisualContentRequest.INFINITE_SHAPE;
        }

        @Override
        public Shape shape() {
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

        private static boolean equals2DComponents(AffineTransform a, Mat4 t) {
            return Double.doubleToLongBits(t.m00()) == Double.doubleToLongBits(a.getScaleX()) &&
                    Double.doubleToLongBits(t.m01()) == Double.doubleToLongBits(a.getShearY()) &&
                    Double.doubleToLongBits(t.m10()) == Double.doubleToLongBits(a.getShearX()) &&
                    Double.doubleToLongBits(t.m11()) == Double.doubleToLongBits(a.getScaleY()) &&
                    Double.doubleToLongBits(t.m30()) == Double.doubleToLongBits(a.getTranslateX()) &&
                    Double.doubleToLongBits(t.m31()) == Double.doubleToLongBits(a.getTranslateY());
        }
    }
}
