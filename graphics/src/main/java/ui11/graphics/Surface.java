package ui11.graphics;

import ui11.PeerRequest;
import ui11.geom.Location;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Rect;
import ui11.geom.Size;

// TODO ez az interface nem jó, mert nem lehet rá értelmes equals/hashCodeot definiálni

/**
 * Megadja, hogy milyen mérete van egy illető Elementnek, és mi a lokális koordináta-rendszere.
 */
public abstract class Surface<P> extends PeerRequest<P> {

    protected Surface(Class<P> peerType) {
        super(peerType);
    }

    /**
     * a visszaadott érték az elem saját koordináta-rendszerében értendő; tehát felmenők által végzett
     * transzformációk előtti méret
     */
    public abstract Size size();

    public abstract CoordinateSpace coordinateSpace();

    // TODO ehelyett valszeg egy shape getter kéne, csak nem világos hogy mi legyen a típusa
    // meg valójában többféle shapeje lehet egy elementnek:
    // - hitbox
    // - visual bounds
    // - layout bounds
    public boolean hitTest(Location point) {
        // TODO ez csak téglalap alakú dolgoknál ad vissza helyes értéket
        return Rect.of(size()).contains(point.in(coordinateSpace()));
    }
}
