package ui11.platform.opengl.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.geom.Mat4;
import ui11.graphics.Surface;
import ui11.graphics.effect.Transform;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.rendertree.EmptyNode;
import ui11.platform.opengl.rendertree.GLNode;
import ui11.platform.opengl.rendertree.TransformNode;
import ui11.platform.opengl.subsurface.TransformedSurface;

public class GLTransformPeer extends Widget {

    private final Transform transform;

    @Inject private Surface parentSurface;
    @Inject private GLVisualContentRequest parentRequest;

    @Remember private TransformedSurface childSurface;
    @Remember private TransformNode node;

    public GLTransformPeer(Transform transform) {
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

        boolean nonDegenerateTransform = childSurface.update(
                parentSurface, transform.transformation());

        // ezt a size beállítás után kell, hogy child tudja hivatkozni VisualContentRequest.size-on keresztül.
        // degenerateTransform esetén is végrehajtjuk, mert általában animáció közben keletkezhetnek
        // pl. 0-s scaleek, ettől nem kell a child widgetnek pause meg resume-ot kapnia.

        GLVisualContentRequest childReq = new GLVisualContentRequest(childSurface);
        return ExposeRequest.requestSingle(transform.content(), childReq, result -> {
            GLNode peer = nonDegenerateTransform ?
                    makeNode(result) :
                    EmptyNode.INSTANCE;
            return new Expose<>(parentRequest, peer);
        });
    }

    private GLNode makeNode(GLNode childNode) {
        if (transform.transformation().isIdentity() || childNode instanceof EmptyNode)
            return childNode;

        Mat4 tx = transform.transformation();
        Mat4 inverseMatrix = tx.inverse();

        if (childNode instanceof TransformNode childTransformNode) {
            node.child.set(childTransformNode.child.get());
            tx = tx.mul(childTransformNode.transformation.get());
            node.transformation.set(tx);
            node.inverseMatrix.set(childTransformNode.inverseMatrix.get().mul(inverseMatrix));

            // lehetne még pl. Transform-Clip-Transform-... láncokat összevonni
        } else {
            node.child.set(childNode);
            node.transformation.set(tx);
            node.inverseMatrix.set(inverseMatrix);
        }

        return node;
    }
}
