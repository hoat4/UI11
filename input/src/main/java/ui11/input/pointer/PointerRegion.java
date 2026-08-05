package ui11.input.pointer;

import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.input.pointer.Pointer.Button;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.Objects;

public abstract class PointerRegion extends SubstitutedWidget {

    private final @NonNull Widget content;

    @Remember private Slot2 contentSlot;

    public PointerRegion(Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    public final @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }

    /**
     * Egy {@link Pointer mutató} lenyomásáról értesítés.
     */
    @Nullable
    public abstract PointerListener onPointerDown(Pointer pointer, Button button);

    // TODO olyat szabad kapnia, hogy több onPointerMove inside=false-szal?
    public abstract void onPointerMove(Pointer pointer, boolean inside);

    /**
     * Scrollozási folyamat megkezdéséről szóló esemény.
     * <p>
     * Egérrel görgetéskor egy scrollozási folyamat rögtön {@link ScrollCallback#finish() be is fejeződik}, viszont
     * érintőkijelzőn ujjhúzással történő scrollozás csak akkor, miután felengedtük az ujjunkat.
     */
    @Nullable
    public ScrollCallback onBeginPhysicalScroll() {
        return null;
    }

    // TODO nézzük meg, hogy létezik-e olyan input rendszer, ahol van kiterjedése a pointernek,
    //      és az is hovernek számít, ha az elem nem tartalmazza az érintés pontját, hanem csak az érintés
    //      környezetének egy pontját

    public interface PointerListener {

        // TODO kimenés az elemből, bemenés
        //      illetve majd ellenőrizzük hogy ugyanez hover esetén is megvan-e

        /**
         * Ez ez első gomb lenyomásánál nem hívódik meg, mert ott {@link PointerRegion#onPointerDown(Pointer, Button)}
         * által már megvolt az értesítés
         */
        void onPress(Button button);

        /**
         * A mutató elmozdult egy új pontra.
         */
        // ez hover esetén is meghívódik?
        void onMove();

        /**
         * A mutató egy gombját felengedték.
         */
        void onRelease(Button button);

        /**
         * A lenyomás végetért, megszakítással vagy anélkül.
         * Ha megszakítással, akkor nem szabad végrehajtani akciót. Ilyen megszakítás lehet a
         * mutató elemről való kimozgatása, vagy az Escape billentyű használata.
         */
        // TODO scrollbar esetén az elemről való kimozgatásnak nem kéne visszacsinálnia a scrollbar mozgatást,
        //      viszont az escape lenyomásának lehet hogy igen, ezért nem világos hogy mikor számít cancelnek valami.
        //      ellenvetés, hogy se IntelliJ-ben, se Notepadben, se böngészőben nem csinál semmit az escape.
        //      Régebbi GUI-kban scrollbarnál a túlzott kimozgatás viszont jelentett a megszakítást (ezáltal pl.
        //      Edge-ben is, mert az a szabványos Windows scrollbart használja). Viszont új GUI-ban ezt nem csinálják.
        //      De valamilyen megszakítási lehetőség mégis jó lenne, ezért lehetne az escape lenyomására megszakítani a
        //      scrollozást.
        // TODO adjuk át hogy cancelelve lett-e
        void onFinish();

        // TODO default Transferable dragData() { return null; }
    }

    public interface ScrollCallback {
        // TODO a deltának milyen koordinátarendszerben kéne lennie?
        // TODO inertia
        // TODO itt használjuk LogicalScrollt
        void scroll(double deltaX, double deltaY, double totalDeltaX, double totalDeltaY);

        default void finish() {
        }
    }
}
