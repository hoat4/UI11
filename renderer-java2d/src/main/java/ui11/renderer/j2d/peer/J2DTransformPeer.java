package ui11.renderer.j2d.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Mat4;
import ui11.graphics.Surface;
import ui11.graphics.effect.Transform;
import ui11.renderer.j2d.J2DSurface.TransformedJ2DSurface;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.rendertree.EmptyNode;
import ui11.renderer.j2d.rendertree.J2DNode;
import ui11.renderer.j2d.rendertree.TransformNode;

import java.awt.geom.AffineTransform;

public class J2DTransformPeer extends Widget {

    private final Transform transform;

    @Inject private Surface parentSurface;
    @Inject private J2DVisualContentRequest parentRequest;

    @Remember private TransformedJ2DSurface childSurface;
    @Remember private TransformNode node;

    public J2DTransformPeer(Transform transform) {
        this.transform = transform;
    }

    @Override
    protected void initState() {
        childSurface = new TransformedJ2DSurface();
        node = new TransformNode();
    }

    @Override
    protected Widget build() {
        // TODO mi történjen, ha 0-ra scaleelünk egy ColorFillt vagy hasonlót (aminek végtelen a mérete)?
        //      most jelenleg eltüntetjük. ha mégsem kéne eltüntetni, módosítsuk lent a kódot.

        boolean nonDegenerateTransform = childSurface.update(
                parentSurface, transform.transformation());

        // ezt a size beállítás után kell, hogy child tudja hivatkozni VisualContentRequest.size-on keresztül.
        // degenerateTransform esetén is végrehajtjuk, mert általában animáció közben keletkezhetnek
        // pl. 0-s scaleek, ettől nem kell a child widgetnek pause meg resume-ot kapnia.

        J2DVisualContentRequest childReq = new J2DVisualContentRequest(childSurface);
        return PeerRequest.requestSingle(transform.content(), childReq, result -> {
            return parentRequest.createResponse(
                    nonDegenerateTransform ?
                            makeNode(result) :
                            EmptyNode.INSTANCE
            );
        });
    }

    private J2DNode makeNode(J2DNode childNode) {
        if (transform.transformation().isIdentity() || childNode instanceof EmptyNode)
            return childNode;

        AffineTransform tx = new AffineTransform();
        Mat4 t = transform.transformation(); // TODO 3D esetén kéne fallback, nem eldobni
        tx.setTransform(
                t.m00(), t.m01(),
                t.m10(), t.m11(),
                t.m30(), t.m31()
        );

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
}
