package ui11.renderer.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.effect.Transform;
import ui11.renderer.layer.Item;
import ui11.renderer.subsurface.TransformedSurface;

public class TransformPeer extends Widget {

    private final Transform transform;

    @Inject private Surface parentSurface;
    @Inject private Item.ItemRequest parentRequest;

    @Remember private TransformedSurface childSurface;

    public TransformPeer(Transform transform) {
        this.transform = transform;
    }

    @Override
    protected void initState() {
        childSurface = new TransformedSurface();
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

        Item.ItemRequest childReq = new Item.ItemRequest(childSurface);
        return ExposeRequest.requestSingle(transform.content(), childReq, result -> {
            Item peer = nonDegenerateTransform ?
                    result :
                    Item.EMPTY;
            return new Expose<>(parentRequest, peer, transform.content());
        });
    }
}
