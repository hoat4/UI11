package ui11.platform.awt.j2d.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Mat4;
import ui11.geom.Size;
import ui11.graphics.effect.Transform;
import ui11.observable.MutableObservable;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.J2DSurface.J2DSurfaceWithOwnShape;
import ui11.platform.awt.j2d.inputtree.InputNode;
import ui11.platform.awt.j2d.inputtree.TransformInputNode;
import ui11.platform.awt.j2d.inputtree.TransparentInputNode;
import ui11.platform.awt.j2d.rendertree.EmptyRenderNode;
import ui11.platform.awt.j2d.rendertree.RenderNode;
import ui11.platform.awt.j2d.rendertree.TransformRenderNode;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

public class J2DTransformPeer extends Widget {

    private final Transform transform;

    @Inject private J2DSurface parentSurface;

    @Remember private TransformedSurface childSurface;
    @Remember private TransformRenderNode node;
    @Remember private TransformInputNode inputNode;

    public J2DTransformPeer(Transform transform) {
        this.transform = transform;
    }

    @Override
    protected void initState() {
        childSurface = new TransformedSurface();
        node = new TransformRenderNode();
        inputNode = new TransformInputNode();
    }

    @Override
    protected Widget build() {
        // TODO mi történjen, ha 0-ra scaleelünk egy ColorFillt vagy hasonlót (aminek végtelen a mérete)?
        //      most jelenleg eltüntetjük. ha mégsem kéne eltüntetni, módosítsuk lent a kódot.

        childSurface.parent.set(parentSurface);
        boolean nonDegenerateTransform = childSurface.update(transform.transformation());

        // ezt a size beállítás után kell, hogy child tudja hivatkozni Surface.size-on keresztül.
        // degenerateTransform esetén is végrehajtjuk, mert általában animáció közben keletkezhetnek
        // pl. 0-s scaleek, ettől nem kell a child widgetnek pause meg resume-ot kapnia.

        return PeerRequest.requestSingle(transform.content(), childSurface, result -> {
            return new J2DNodeHolder(
                    nonDegenerateTransform ?
                            makeRenderNode(result.renderNode()) :
                            EmptyRenderNode.INSTANCE,
                    nonDegenerateTransform ?
                            makeInputNode(result.inputNode()) :
                            TransparentInputNode.INSTANCE
            );
        });
    }

    private RenderNode makeRenderNode(RenderNode childNode) {
        if (transform.transformation().isIdentity() || childNode instanceof EmptyRenderNode)
            return childNode;

        AffineTransform tx = childSurface.awtAffineTransformation;

        if (childNode instanceof TransformRenderNode childTransformNode) {
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

    private InputNode makeInputNode(InputNode childNode) {
        if (transform.transformation().isIdentity() || childNode == TransparentInputNode.INSTANCE)
            return childNode;

        AffineTransform tx = childSurface.awtAffineTransformation;

        if (childNode instanceof TransformInputNode childTransformNode) {
            inputNode.child.set(childTransformNode.child.get());
            tx = new AffineTransform(tx);
            tx.concatenate(childTransformNode.transformation.get());
            inputNode.transformation.set(tx);

            // lehetne még pl. Transform-Clip-Transform-... láncokat összevonni
        } else {
            inputNode.child.set(childNode);
            inputNode.transformation.set(tx);
        }
        return inputNode;
    }

    private static class TransformedSurface extends J2DSurfaceWithOwnShape {

        private final AffineTransform awtAffineTransformation = new AffineTransform();
        private Shape prevParentShape;
        private final MutableObservable<Shape> shape = MutableObservable.ofNullable();

        boolean update(Mat4 t) {
            Shape parentShape = parent.get().shape();
            if (parentShape == J2DSurface.INFINITE_SHAPE) {
                prevParentShape = J2DSurface.INFINITE_SHAPE;
                shape.set(J2DSurface.INFINITE_SHAPE);
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
                    shape.set(J2DSurface.INFINITE_SHAPE);
                }
                prevParentShape = parentShape;
            }
            return shape.get() != J2DSurface.INFINITE_SHAPE;
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
