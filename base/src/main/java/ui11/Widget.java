package ui11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.observable.ObservableBase;
import ui11.observable.Scope;
import ui11.provide.Provider;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.joining;

// TODO most nem adható meg Provider-nek egy használatban lévő Widget, mert ha lecseréljük egy másik példányra
//      azonos input mező értékekkel, akkor előfordulhat, hogy beragad az IV-kben a régi, míg a hozzá
//      tartozó Elementben már az új lesz. Ilyen volt például CommonWidgetsBetweenLobbyTabs interface,
//      amit PregameView implementált, és a lobbi tabok megkapták volna IV-ben. Bár hogy hogyan ragadt be az IV-be
//      a régi példány, azt nem tudom.
//      Ezt vagy el kéne fogadni és dokumentálni, vagy meg kéne oldani.
//      De utóbbit nem látom, hogy hogyan lehetne. Esetleg ha megakadályoznánk az IV-be beragadást az alapján,
//      hogy ki lett szedve a fából.
//      Ráadásul r28077-ben írtak szerint amúgy sem jó ötlet:
//      "konstruktor legyen widgetekben az egyedüli publikus API, meg statikus factory methodok.
//       Ha hivatkoztott valahol a kód ancestor widgetekre, akkor az általában rosszul sült el. Helyette bővíteni
//       kell ilyenkor a property-k listáját, hogy ne kelljen az ancestor widgetre hivatkozni."

// TODO lehetne warningolni, ha egyszer már felhasznált RSW-t egy másik helyen használjuk

// most hogy nincs már @Input, ez annyiból megtévesztő, hogy nem hívja fel a figyelmet arra, hogy equals/hashCodeba
// bele fog számítani. ez akkor nem nyilvánvaló, ha van öröklődési hierarchia, lásd pl. DOMLayoutPeerBase és ott a két
// boolean mezőt.

// TODO talán mégis meg kéne próbálni cloneozást, mert így nem lehet pl. Collections.nCopiest se használni
//      (GameClock timeUpMark)
//      meg Adorján is nemrég elrakott volna egy ikont egy lokális változóba, amit két helyet akart kijelezni.
//      A klónozás legutolsó revertjekor valami olyasmi volt írva, hogy azért lett, mert nehéz átlátni hogy
//      kétféle szerepben vannak a widgetek. De azóta eltelt sok idő, és most már megszoktam hogy nincs
//      létrehozás utáni API-juk a widgeteknek.

/**
 * This class represents the basic building block for user interface components. A widget can be for example a simple
 * geometric shape, a layout which arranges other widgets, a control, or a region an application.
 * <p>
 * A widget describes part of the user interface by building a constellation of other widgets that describe the user
 * interface more concretely. The building process continues recursively until the description of the user interface is
 * fully concrete (TODO ezt definiálni kéne).
 * <p>
 * Every non-static fields of the subtypes of this subclass must be either {@code final} or annotated with
 * {@linkplain Inject @Inject} or {@linkplain Remember @Remember}.
 */
// TODO írni terminológiáról (hagyományos értelemben Widget csak a controlokat jelenti)
public abstract class Widget implements Cloneable {

    private static final Logger logger = LoggerFactory.getLogger(Widget.class);

    /**
     * Ennek lehetséges értékei:
     * <ul>
     *     <li>{@code null} vagy {@linkplain WidgetAccessor} detached marker nélkül: nincs attacholva
     *     {@linkplain Element}hez, és
     *     nem is volt még</li>
     *     <li>{@linkplain Element}: attacholva van</li>
     *     <li>{@linkplain WidgetAccessor} detached markerrel: volt attacholva, de már nincs</li>
     * </ul>
     */
    private Object stateHolderOrDef;
    // TODO detached markeres izé valszeg törölhető, mert az új klónozásos logikában már nem
    //      lesz egy detached state role-ú widget példány újra attached, ezért egyszerűbb
    //      egy érvénytelen értékre állítani e mezőt

    /**
     * elvileg csak model szerepű widgetekben lehetne nemnull ez, de mivel egy widget példány lehet egyszerre state és
     * model szerepű is, ezért mindkettőben lehet nemnull ez
     */
    ListenerProxyBase.LPModelData lpModelData;
    // mivel a widgetek többségében nincs listener proxy, ezért lehet hogy jobban megéri külön mező helyett
    // inkább úgy tárolni, hogy stateHolderOrDef értéke lenne egy objektum ami tartalmazza
    // LPModelData-t és hivatkozik az accessorra is.

    // lehet hogy kéne egy explicit üres protected konstruktor javadoc miatt

    // TODO valamit csinálni kéne, hogy jobban felhívjuk a figyelmet arra hogy initState-ben hibás
    //      @Inject mezőket olvasni (kivéve ha initial value-ként akarjuk használni).
    //      pl. meg lehetne szüntetni initState-et, és helyette initialValue(Supplier<T>)-et csinálni, mint
    //      a remember() volt r28918-ban.
    //      vagy lehetne hogy initState-ben még null legyen minden, de akkor meg initial value-s felhasználásra
    //      ez nem jó.

    /**
     * Called when the widget is added to the tree and no previous state is available.
     * <p>
     * After calling this method, {@link #onResume()} then {@linkplain #build()} will be called. Then
     * {@linkplain #build()} will be called again and again whenever an input property or an
     * {@link ui11.observable.Observable observable value} changes.
     * <p>
     * Note: Don't pass a reference to {@code this} value to other objects which last longer than this
     * {@linkplain Widget} instance, because while the same conceptual widget appears on the screen and shares the same
     * state, the actual instances may be recreated whoever instantiates this widget.
     */
    // azért lett default üres implementációja (eredetileg abstract volt),
    // hogy késztessük a usert arra build scopeján kívül is csináljon dolgokat,
    // mert pl. lambdák nem működnek rendesen (this-t captureölik, miközben változni fog az objektum)
    protected void initState() {
    }

    /**
     * Called when the widget is added to the tree.
     * <p>
     * Before calling this method, {@link #initState()} is called if no previous state is available. After calling this
     * method then {@linkplain #build()} will be called. Then {@linkplain #build()} will be called again and again
     * whenever an input property or a subscribed {@link ui11.observable.Observable observable value} changes.
     * <p>
     * Note: Don't pass a reference to {@code this} value to other objects which last longer than this
     * {@linkplain Widget} instance, because while the same conceptual widget appears on the screen and shares the same
     * state, the actual instances may be recreated whoever instantiates this widget.$
     */
    // alternatív név: onShow, onMount
    // TODO végig kéne menni ennek a felülírásain, és ellenőrizni, hogy nem olvas-e input fieldet,
    //      aminek a megváltozása esetén újra végre kéne hajtani valamit.
    //      ha igen, akkor át kell rakni build()-be
    protected void onResume() {
    }

    // TODO input values fogalmat linkeljük valahonnan

    /**
     * Builds the content of the widget.
     * <p>
     * The framework calls method annotated with this annotation when this widget is inserted into the tree newly, or if
     * its input values was changed, or the {@link ObservableBase observable dependencies} of this widget change. This
     * method can potentially be called in every frame and should not have any side effects beyond building a widget.
     * <p>
     * This method must not return null.
     */
    // TODO ellenőrizni kéne hogy ez nem ad-e vissza thist, vagy egyéb módon okoz-e rekurziót
    // TODO a side effect szövegrészlet nem feltétlen releváns
    // @NonNull ha ezt ideírom, akkor minden implementációnál warningolni fog. nem tudom, mi legyen vele.
    protected abstract Widget build();

    // onPause nem feltétlen kell, mert ott va rá untilPause().onClose

    /**
     * Returns a {@linkplain Scope} that will be open until the next {@link #build()} rebuild.
     * <p>
     * This method can be only called from {@linkplain #build()}.
     * <p>
     * At the moment when this scope is closed, the inherited value observables obtained via {@linkplain Inject @Inject}
     * will be the same in the last build.
     *
     * @throws IllegalStateException if called not in {@linkplain #build()}
     */
    // TODO ez mit csináljon, ha onResume-ból van hívva?
    // TODO pontosítsuk, hogy mikor záródik be
    // TODO nézzük meg, hogy tényleg csak buildből hívható-e
    protected final Scope untilNextRebuild() throws IllegalStateException {
        return element().untilWidgetStateNextRebuild(this);
    }

    /**
     * Returns a {@linkplain Scope} that will be open until this widget is removed from the widget tree.
     * <p>
     * This method can be only called after the widget is added to the tree and {@link #initState()} has been finished.
     * <p>
     * At the moment when this scope is closed, the inherited value observables obtained via {@linkplain Inject @Inject}
     * can still be seen, but their values might be obsolete.
     *
     * @throws IllegalStateException if called when this widget is not in the widget tree, or {@linkplain #build()}
     *                               hasn't ran yet
     */
    // init()-ben lehetne engedni, de nem tudok rá use-caset
    protected final Scope untilPause() throws IllegalStateException {
        return element().untilWidgetStatePause(this);
    }

    // @Listenernél volt egy komment:
    //     alternatív nevek: @Callback, @InterfaceProxy, @EventListener

    // TODO anonymous vagy local classoknál hogy lehessen listenerproxy-t használni?
    //      mindenképp generál javac egy másik synthetic fieldet a captureölt változónak,
    //      hiába nem használjuk a field initializer blokkon kívül sehol

    /**
     * The specified object will be replaced by a proxy object, which implements the interface, and forwards all method
     * calls to the element's current widget's value of the annotated field. This allows to change event listener
     * implementations without rebuilding the widgets.
     * <p>
     * If the listener argument is {@code null}, then it won't be replaced by a proxy object.
     *
     * @see #listenerProxy(Consumer)
     */
    protected final Runnable listenerProxy(Runnable listener) {
        if (roleIsState())
            // TODO exception üzenet
            throw new IllegalStateException("lp on state widget (R): " + this); // vagy UOE?
        if (listener == null)
            return null;
        return new ListenerProxyBase.RunnableListenerProxy(this, listener);
    }

    /**
     * @see #listenerProxy(Runnable)
     */
    protected final <T> Consumer<T> listenerProxy(Consumer<T> listener) {
        if (roleIsState())
            throw new IllegalStateException("lp on state widget (C): " + this);
        if (listener == null)
            return null;
        return new ListenerProxyBase.ConsumerListenerProxy<>(this, listener);
    }

    // régi komment listener proxy-kkal kapcsolatban, ElementDefReflectorból:
    //     eredetileg úgy volt, hogy tetszőleges interface-ek lehetnek event listenerek.
    //     de lehet hogy jobb így, hogy csak Runnable meg egy-két másik lehet, mert így biztosítani lehet,
    //     hogy csak void return type-ú SAM-ok.
    //     2025-12-06:
    //     majd lehet hogy ki kell terjeszteni tetszőleges interfacere (pl. mouseeventek esetén a tipikus a
    //     sok függvényes interface, vagy lehet hogy kell visszaadni értéket), de egyelőre elég ez a kettő.

    public final Widget withSlot(Slot slot) {
        // TODO ha ez egy KeyWrapper, akkor elég lenne csak this-t visszaadni
        //      de akkor végig lehetne menni Provide-okon is végülis
        // régen (Slot.use-ban) ellenőriztük, hogy a slot owner widgetje még aktív volt-e.
        // de mivel key-ek már megszűntek, ezért végülis nem is kell.
        return new KeyWrapper(slot, this);
    }

    // equals/hashCodera final kell?
    // valszeg érdemes hagyni felülírhatóra. de akkor meg kéne csinálni egy másik felülírhatót, amit
    // a modellváltozás észlelésének módosítására lehet felülírni.

    // kell equals, mert lehet hogy nem egyszerű mezőben, hanem Listben, Mapben vagy egyéb struktúrában hivatkozik
    // egyik widget egy másikra

    // TODO lehet hogy ezt equalst kéne használni a modellváltozás ellenőrzésekor is.
    //      de az se jó, mert az a listenereket nem nézi.

    // TODO a listeneres speciális működésre kéne teszt

    /**
     * Compares the input fields of the other widget. Input field means a final non-static field of a
     * {@linkplain Widget} subtype, which is not annotated with {@linkplain Inject} or {@linkplain Remember}. This also
     * includes non-static final fields annotated with containing a {@link #listenerProxy(Runnable) listener proxy}.
     * <p>
     * Note that this method is not used for determining that a widget needs {@link #build() rebuild}. Instead, for
     * determining that a widget needs rebuild or not a slightly different approach used which differs only in the
     * treatment of {@linkplain #listenerProxy(Runnable) listener proxy} fields: they are not compared by value (or the
     * delegate of the proxy object in them), but only the nullness is compared to the same field of the other object.
     *
     * @return true of the specified object is the same class as this object and the values of all non-listener input
     * fields are equal (according to {@link Objects#equals(Object, Object) Objects.equals}) to the same fields of this
     * object
     */
    @Override
    public boolean equals(Object obj) {
        // TODO ha ez state role-ú, akkor equals/hashCode identity equals/hashCode legyen.
        //      de az se jó, mert lehet hogy egyszerre próbálja state és model role-ban is használni.

        // TODO a listenereket miért hasonlítjuk itt össze?

        if (obj == null || obj.getClass() != getClass())
            return false;

        // TODO ha identity equals, akkor lehetne egyből true-t visszaadni.
        //      ha felülírják az equalst, akkor ezt a tulajdonságot lehet hogy verifikálni kéne.

        WidgetAccessor<Widget> accessor;
        try {
            accessor = accessor();
        } catch (InvalidWidgetDefinitionException e) {
            logger.error("Can't calculate equals, because because the widget definition of " +
                    getClass().getName() + " is invalid", e);
            return this == obj;
        }

        return accessor.inputFieldsEquals(this, (Widget) obj);
    }

    /**
     * Creates a hash code of the input fields of this widget. Input field means a final non-static field of a
     * {@linkplain Widget} subtype, which is not annotated with {@linkplain Inject} or {@linkplain Remember}. This also
     * includes non-static final fields containing {@link #listenerProxy(Runnable) listener proxies}.
     */
    @Override
    public int hashCode() {
        WidgetAccessor<Widget> accessor;
        try {
            accessor = accessor();
        } catch (InvalidWidgetDefinitionException e) {
            logger.error("Can't calculate hashCode, because because the widget definition of " +
                    getClass().getName() + " is invalid", e);
            // mivel úgyse lesz használható a widget, ezért nem baj ha hülyeség a hashcode
            return System.identityHashCode(e);
        }
        return accessor.inputFieldsHashCode(this);
    }

    @Override
    public String toString() {
        if (!(this instanceof SubstitutedWidget))
            // TODO ide lehetve valami infót berakni, pl. role
            return super.toString();

        // azért csak SubstitutedWidgetnél vannak a mezők kiírva, mert itt kisebb eséllyel "szenzitív adat" az input
        // mezők tartalma
        StringBuilder sb = new StringBuilder();
        Object[] props = accessor().inputFieldsToString(this);
        sb.append(getClass().getSimpleName()).append(" (");
        if (stateHolderOrNull() == null)
            sb.append("no state holder");
        else
            sb.append(element().elementState);
        sb.append(") {");
        if (props.length == 0)
            sb.append("}");
        else {
            sb.append("\n");
            for (int i = 0; i < props.length; i += 2) {
                sb.append("  ").append(props[i]);
                Object val = props[i + 1];
                if (val == null)
                    // nem ugyanaz a karakter ilyenkor, mint a valós érték előtt, mert akkor nem lehetne
                    // megkülönböztetni nullt a "null" stringtől
                    sb.append(": null");
                else {
                    sb.append(" = ");
                    String valStr = switch (val) {
                        case Collection<?> coll -> coll.isEmpty() ? "[]" : coll.stream().
                                map(String::valueOf).collect(joining(", \n  ", "[\n  ", "\n]"));
                        case Map<?, ?> map -> map.isEmpty() ? "{}" : map.entrySet().stream().
                                map(String::valueOf).collect(joining(", \n  ", "{\n  ", "\n}"));
                        default -> val.toString();
                    };
                    int firstNewline = valStr.indexOf('\n');
                    if (firstNewline == -1)
                        sb.append(valStr);
                    else if (firstNewline > 0 && valStr.charAt(firstNewline - 1) == '{' && valStr.endsWith("}"))
                        sb.append(valStr.replace("\n", "\n  "));
                    else // pl. Mat4
                        sb.append("\n    ").append(valStr.replace("\n", "\n    "));
                }
                sb.append(i == props.length - 2 ? "\n}" : ", \n");
            }
        }
        return sb.toString();
    }

    WidgetAccessor<Widget> accessor() {
        WidgetAccessor<?> result;
        if (stateHolderOrDef == null) {
            result = ElementAccessorFactory.accessorFor(getClass());
            stateHolderOrDef = result;
            Objects.requireNonNull(result);
        } else if (stateHolderOrDef instanceof Element e) {
            result = e.accessor(this);
            Objects.requireNonNull(result);
        } else {
            result = (WidgetAccessor<?>) stateHolderOrDef;
            Objects.requireNonNull(result);
        }

        @SuppressWarnings("unchecked")
        WidgetAccessor<Widget> casted =
                (WidgetAccessor<Widget>) result;
        return casted;
    }

    Element element() {
        if (!(stateHolderOrDef instanceof Element sh))
            // toStringet lehet hogy felülírják egy olyannal ami inherited valuet olvasna (pl. MultiChildLayout)
            // ami nem fog működni ha element() exceptiont dob
            throw new IllegalStateException("RSW no SH: " + super.toString() + ", " + stateHolderOrDef);
        return sh;
    }

    @SuppressWarnings("unchecked")
    Element stateHolderOrNull() {
        if (stateHolderOrDef instanceof Element sh)
            return sh;
        else
            return null;
    }

    /**
     * Lecserélődik az adott {@linkplain Element}ben lévő widget state, ezért ez az objektum nem lesz használva
     * többé.
     */
    final void disposeFromStateRole(Element stateHolder) {
        if (!(this.stateHolderOrDef instanceof Element prevHolder))
            throw new IllegalStateException("not has state holder: " + this + ", expected: " + stateHolder);
        if (prevHolder != stateHolder)
            throw new IllegalStateException("has different state holder: " + this + ", expected: " + stateHolder + ", " +
                    "actual: " + prevHolder);

        this.stateHolderOrDef = stateHolder.currentState.accessor.asDetachedMarker(true);

        // TODO ilyenkor a state/inject fieldeket ki lehetne nullozni, mert pl. MultiChildLayout toStringje az
        //      inject fieldet akar olvasni ha nem null, így IllegalStateExceptionre lyukad
    }

    /*protected*/ String debug_getRefreshStack() {
        return element().refreshStackToString(Map.of());
    }

    boolean roleIsState() {
        return stateHolderOrDef instanceof Element ||
                stateHolderOrDef instanceof WidgetAccessor<?> acc &&
                        acc == acc.asDetachedMarker(true);
    }

    Widget makeCloneToBeStateRole(Element e) {
        Widget clone = makeClone();
        clone.stateHolderOrDef = e;
        return clone;
    }

    private Widget makeClone() {
        try {
            return (Widget) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    void initListenerProxyData() {
        if (lpModelData == null && accessor().prepareListenerProxies(this))
            lpModelData = new ListenerProxyBase.LPModelData(this);
    }

    // "rebuilding" vagy "recomposition"-nak nevezzük?

    // TODO lehetne figyelmeztetést kiírni, ha egy input mezőben egy ismert mutable osztály (pl. ArrayList)
    //      van az egyikben és megváltozott a tartalma (ehhez nyilván kell akkor csinálni minden ilyen típushoz
    //      kézzel egy deep clone supportot).

    // Jó lenne Observable<...>-séget törölni @Inject-ből.
    // Azonban ekkor ha egy inner class widgetben hivatkozunk rá (lásd még: r24586), akkor
    // az nem fog refreshelődni. Ez input mezőknél nem probléma, mivel Widget::equals false-t ad vissza, ha
    // megváltoztak az input mezők, és mivel a this$0 is input field, ezért lesz refreshSelf a belső widgeten.
    // Mivel @Inject-es mezők nem finalok, ezért nem is lehet úgy megbuherálni equalst, hogy false-t adjon vissza a
    // megváltozásukkor, mivel lehet hogy a widget ugyanaz az objektum maradt és úgy változtak meg.
    // Esetleg egy threadlocalba elmenteni minden widgetet, ami equals által érintett volt az input mezők
    // összehasonlításakor, majd utána figyelni hogy melyiknél változtak az inputmezők, az talán működhet.
    // Object.equals javadocja ugyan konzisztenciáról igen, de side-effectekről nem ír, ezért még az is
    // lehet hogy szabályos is.

    // TODO nem kéne mindenképp kikeresni az illető inherited valuet, csak ha meghívjuk
    //      Observable::get-et. bár akkor meg nem derül ki, ha hiányzik.

    /**
     * Az ezzel annotált mezők típusa csak interface lehet, és a mezőknek nem szabad finalnak lenniük. Ha egy
     * {@linkplain ui11.observable.Observable Observable} típusú mezőt annotálunk ezzel, akkor az Observable
     * típusváltozójában megadott típusú inherited valuet fogjuk keresni.
     * <p>
     * Ha annotálva van ezzel, akkor {@linkplain Remember} annotációval már nem lehet.
     */
    @Target(FIELD)
    @Retention(RUNTIME)
    protected @interface Inject {
        // régebben "optional" volt ennek a neve, Springben nem optional van, hanem required, úgyhogy kipróbáljuk
        // egy ideig hogy required=false-ot kell írni optional=true helyett:
        // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/annotation/Autowired.html
        boolean required() default true;

        // TODO mit jelent required=false, ha a típus Slot vagy MultiSlot vagy interfaceproxy?

        // TODO lehetne olyan változat, ami felteszi hogy nem fog változni, és akkor nem kell Observable-be wrappelni
        //      akkor se ha nem interface (és ha mégis változik, akkor nem engedi a widgetet buildelni / exceptiont dob)
        //      ld. például LottieWebAnimationPeerban DOMEnvironment
    }

    // Eredetileg Remember-nek State volt a neve. de Adorján r29407-ben input mezőként deklarált egy mezőt
    // (azaz annotáció nélkül), ami state mező lett volna valójában. Mivel egy annotáció nélküli mező deklarálása
    // kisebb erőfeszítés mintha annotálnia kéne, ezért valszeg nagyobb aránnyal próbálkozik valaki input mezőként
    // definiálással, aki még bizonytalan az input/inject/state fogalmakban. Ezért megpróbálkozunk
    // ezzel a Remember névvel, ami a semleges State szónál pozitívabb, és amúgyis gyakran szeretnek az
    // emberek beállítani olyan dolgokat, amik valamiféle cache-elésre vagy perzisztenciára utalnak.

    /**
     * Az ezzel annotált mezőknek nem szabad {@code final}-nak lenniük, és az {@linkplain #initState()} meghívásáig nem
     * szabad a mező default értékén (objektumok esetén null, primitív típusok esetén 0 vagy {@code false}) kívül mást
     * felvenniük.
     * <p>
     * Ha annotálva van ezzel, akkor {@linkplain Inject} annotációval már nem lehet.
     * <p>
     * This is similar to the
     * <a href="https://developer.android.com/develop/ui/compose/state#state-in-composables">{@code remember} function
     * </a> in Jetpack Compose.
     */
    @Target(FIELD)
    @Retention(RUNTIME)
    protected @interface Remember {}
}
