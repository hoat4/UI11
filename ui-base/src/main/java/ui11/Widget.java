package ui11;

import ui11.observable.ObservableBase;
import ui11.observable.Scope;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// TODO nem-statikus inner class support?

// TODO lehetne warningolni, ha egyszer már felhasznált RSW-t egy másik helyen használjuk

// most hogy nincs már @Input, ez annyiból megtévesztő, hogy nem hívja fel a figyelmet arra, hogy equals/hashCodeba
// bele fog számítani. ez akkor nem nyilvánvaló, ha van öröklődési hierarchia, lásd pl. DOMLayoutPeerBase és ott a két
// boolean mezőt.

// ez a serializable-s hack lehet hogy mégsem volt jó ötlet, mert a javadocot teleszemeteli "Serialized Form"-mal

/**
 * This class represents the basic building block for user interface components. A widget can be for example a simple
 * geometric shape, a layout which arranges other widgets, a control, or a region an application.
 * <p>
 * A widget describes part of the user interface by building a constellation of other widgets that describe the user
 * interface more concretely. The building process continues recursively until the description of the user interface is
 * fully concrete (i.e. only consists of {@linkplain ui11.provide.UpValueWrapper} with no next).
 * <p>
 * Every non-static fields of the subtypes of this subclass must be either {@code final} or annotated with
 * {@linkplain Inject @Inject} or {@linkplain State @State}.
 * <p>
 * This class implements {@link java.io.Serializable java.io.Serializable}, but is not really serializable, the reason
 * for implementing it is to allow {@link Listener @Listener} to work in future JDK versions (see <a
 * href="https://openjdk.org/jeps/500">JEP 500</a>)
 */
// TODO írni terminológiáról (hagyományos értelemben Widget csak a controlokat jelenti)
public abstract class Widget implements Serializable {

    /**
     * Ha ez null vagy {@linkplain WidgetAccessor} van benne, akkor nincs attacholva {@linkplain RSWStateHolder}.
     * Különben van.
     */
    private Object stateHolderOrDef;
    boolean injectFieldsInitialized;
    /**
     * nem olyan értelemben vett "state" mint a StateHoldernél, hanem inkább status
     */
    private WidgetState state = WidgetState.INITIAL;

    // lehet hogy kéne egy explicit üres protected konstruktor javadoc miatt

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
    // @Nonnull ha ezt ideírom, akkor minden implementációnál warningolni fog. nem tudom, mi legyen vele.
    protected abstract Widget build();

    // onPause nem feltétlen kell, mert ott va rá untilPause().onClose

    /**
     * Returns a {@linkplain Scope} that will be open until the next {@link #build()} rebuild.
     * <p>
     * This method can be only called from {@linkplain #build()}.
     * <p>
     * At the moment when this scope is closed, the inherited value observables obtained via {@linkplain Inject @Inject}
     * can still be seen, but their values might be obsolete.
     *
     * @throws IllegalStateException if called not in {@linkplain #build()}
     */
    // TODO ez mit csináljon, ha onResume-ból van hívva?
    // TODO pontosítsuk, hogy mikor záródik be
    // TODO nézzük meg, hogy tényleg csak buildből hívható-e
    protected final Scope untilNextRebuild() throws IllegalStateException {
        return stateHolder().untilNextRebuild();
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
        return stateHolder().untilUnmount();
    }

    protected final WidgetInstantiation instantiate(KeyWrapper widget) {
        Objects.requireNonNull(widget);

        RSWStateHolder<?> stateHolder = stateHolderOrNull();
        if (stateHolder == null || stateHolder.refreshState == null)
            // TODO így initStateből is lehet hívni
            throw new IllegalStateException(Widget.class.getSimpleName() +
                    ".instantiate can only be called inside " + Widget.class.getSimpleName() + ".build()");

        @SuppressWarnings("unchecked") final WidgetAccessor<Widget> castedAccessor =
                (WidgetAccessor<Widget>) stateHolder.accessor;
        Widget decoratedWidget = castedAccessor.decorate(this, widget, false);
        if (decoratedWidget == null)
            throw new RuntimeException("decorator returned null on " + this + " for " +
                    widget + " (slot: " + this + ")");

        return stateHolder.refreshState.instantiate(
                new Object(), // mivel widget instanceof KeyWrapper, ezért mindegy hogy mit adunk itt meg
                decoratedWidget);
    }

    /**
     * Starts a {@linkplain Component} as a child of this widget, if it hasn't already been started. If in the next
     * rebuild this method won't be called with the same argument, the specified component will be stopped.
     * <p>
     * Can be used only inside {@linkplain #build()}.
     */
    // TODO erre milyen API legyen?
    protected final void useComponent(Component component) {
        Objects.requireNonNull(component);

        RSWStateHolder<Widget> stateHolder = stateHolder();
        if (stateHolder.refreshState == null)
            // TODO így initStateből is lehet hívni
            throw new IllegalStateException(Widget.class.getSimpleName() +
                    ".useComponent can only be called inside build()");

        // TODO duplicate component detektálása

        // TODO kéne folytatni az átállást a nem-identitásos component modellre
        record ComponentIdentityKey(Component c) {
            @Override
            public boolean equals(Object obj) {
                return obj instanceof ComponentIdentityKey k && c == k.c;
            }
        }
        stateHolder.refreshState.instantiate(new ComponentIdentityKey(component), component).ensureFresh();
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
     * {@linkplain Widget} subtype, which is not annotated with {@linkplain Inject} or {@linkplain State}. This also
     * includes non-static final fields annotated with {@link Listener @Listener}.
     * <p>
     * Note that this method is not used for determining that a widget needs {@link #build() rebuild}. Instead, for
     * determining that a that a widget needs rebuild or not a slightly different approach used which differs only in
     * the treatment of {@linkplain Listener @Listener} fields: they are not compared by value (or the delegate of the
     * proxy object in them), but only the nullness is compared to the same field of the other object.
     *
     * @return true of the specified object is the same class as this object and the values of all non-listener input
     * fields are equal (according to {@link Objects#equals(Object, Object) Objects.equals}) to the same fields of this
     * object
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass())
            return false;

        // TODO ha identity equals, akkor lehetne egyből true-t visszaadni.
        //      ha felülírják az equalst, akkor ezt a tulajdonságot lehet hogy verifikálni kéne.

        return accessor().inputFieldsEquals(this, (Widget) obj);
    }

    /**
     * Creates a hash code of the input fields of this widget. Input field means a final non-static field of a
     * {@linkplain Widget} subtype, which is not annotated with {@linkplain Inject} or {@linkplain State}. This also
     * includes non-static final fields annotated with {@link Listener @Listener}.
     */
    @Override
    public int hashCode() {
        return accessor().inputFieldsHashCode(this);
    }

    WidgetAccessor<Widget> accessor() {
        WidgetAccessor<?> result;
        if (stateHolderOrDef == null) {
            result = ElementAccessorFactory.accessorFor(getClass());
            stateHolderOrDef = result;
        } else if (stateHolderOrDef instanceof RSWStateHolder<?> sh) {
            result = sh.accessor;
        } else {
            result = (WidgetAccessor<?>) stateHolderOrDef;
        }

        @SuppressWarnings("unchecked")
        WidgetAccessor<Widget> casted =
                (WidgetAccessor<Widget>) result;
        return casted;
    }

    @SuppressWarnings("unchecked")
    RSWStateHolder<Widget> stateHolder() {
        if (!(stateHolderOrDef instanceof RSWStateHolder<?> sh))
            throw new IllegalStateException("RSW no SH: " + this + ", " + state + ", " + stateHolderOrDef);
        return (RSWStateHolder<Widget>) sh;
    }

    @SuppressWarnings("unchecked")
    RSWStateHolder<Widget> stateHolderOrNull() {
        if (stateHolderOrDef instanceof RSWStateHolder<?> sh)
            return (RSWStateHolder<Widget>) sh;
        else
            return null;
    }

    Object getInheritedValueByIndex(int ivIndex) {
        return stateHolder().observedInheritedValues[ivIndex].get();
    }

    /**
     * ez nem hív olyan kódot, ami observable-kre feliratkozna
     */
    @SuppressWarnings("unchecked")
    final <W extends Widget /* this */> void attachStateHolder(RSWStateHolder<W> stateHolder, W copyStateFrom) throws DuplicateRSWInstantiationException {
        if (this.stateHolderOrDef != null) {
            WidgetAccessor<Widget> oldAccessor = accessor();
            if (!oldAccessor.equals(stateHolder.accessor))
                throw new RuntimeException("different RSW accessors: \n" +
                        "Old accesor: " + oldAccessor + "\n" +
                        "New accessor: " + stateHolder.accessor + "\n" +
                        "Widget: " + this);
            if (this.stateHolderOrDef == stateHolder)
                throw new IllegalStateException("already has same state holder: " + this +
                        ", state holder: " + stateHolder);
            else if (this.stateHolderOrDef instanceof RSWStateHolder<?> prevStateHolder)
                ((RSWStateHolder<W>) prevStateHolder).forcedDetachWidget((W) this);
        }

        this.stateHolderOrDef = stateHolder;

        // TODO ellenőrizni kéne, hogy a @Inject-es mezők üresek-e
        // TODO ha ez failol, akkor nem kéne kiszedni a stateHoldert a mezőből?
        stateHolder.accessor.initAndCopyState(copyStateFrom, (W) this);

        this.state = WidgetState.ATTACHED;

        // TODO gondoljuk végig, hogy mi történik, ha initAndCopyState-ben valamelyik meghívott equals-nek
        //      side-effectje van, pl. attacholja ezt a widgetet máshova
    }

    /**
     * Ha kikerül az elem a fából, akkor ez nem fog meghívódni.
     * <p>
     * Ez nem hív olyan kódot, ami observable-kre feliratkozna.
     */
    final void detachStateHolder(RSWStateHolder<?> stateHolder) {
        if (this.stateHolderOrDef == null || !(this.stateHolderOrDef instanceof RSWStateHolder<?> prevHolder))
            throw new IllegalStateException("not has state holder: " + this + ", expected: " + stateHolder);
        if (prevHolder != stateHolder)
            throw new IllegalStateException("has different state holder: " + this + ", expected: " + stateHolder + ", " +
                    "actual: " + prevHolder);

        this.stateHolderOrDef = stateHolder.accessor;
        this.state = WidgetState.DETACHED;
    }

    /*protected*/ String debug_getRefreshStack() {
        return stateHolder().refreshStackToString(Map.of());
    }

    // "rebuilding" vagy "recomposition"-nak nevezzük?

    /**
     * If a field in a {@linkplain Widget} annotated with this annotation, its value will be replaced with an interface
     * proxy before adding to the widget to a parent, which allows to replace the value without rebuilding the widget.
     * <p>
     * The specified object will be replaced by a proxy object, which implements the interface, and forwards all method
     * calls to the element's current widget's value of the annotated field.
     * <p>
     * If the listener argument is {@code null}, then it won't be replaced by a proxy object.
     * <p>
     * The type of field must be {@linkplain java.lang.Runnable} or {@linkplain java.util.function.Consumer}.
     */
    // alternatív nevek: @Callback, @InterfaceProxy, @EventListener
    @Target(FIELD)
    @Retention(RUNTIME)
    protected @interface Listener {
    }

    /**
     * Az ezzel annotált mezők típusa csak interface lehet, és a mezőknek nem szabad finalnak lenniük. Ha egy
     * {@linkplain ui11.observable.Observable Observable} típusú mezőt annotálunk ezzel, akkor az Observable
     * típusváltozójában megadott típusú inherited valuet fogjuk keresni.
     * <p>
     * Ha annotálva van ezzel, akkor {@linkplain State} annotációval már nem lehet.
     */
    @Target(FIELD)
    @Retention(RUNTIME)
    protected @interface Inject {
        // régebben "optional" volt ennek a neve, Springben nem optional van, hanem required, úgyhogy kipróbáljuk
        // egy ideig hogy required=false-ot kell írni optional=true helyett:
        // https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/annotation/Autowired.html
        boolean required() default true;

        // TODO mit jelent required=false, ha a típus Slot vagy MultiSlot vagy interfaceproxy?
    }

    /**
     * Az ezzel annotált mezőknek nem szabad {@code final}-nak lenniük, és az {@linkplain #initState()} meghívásáig nem
     * szabad a mező default értékén (objektumok esetén null, primitív típusok esetén 0 vagy {@code false}) kívül mást
     * felvenniük.
     * <p>
     * Ha annotálva van ezzel, akkor {@linkplain Inject} annotációval már nem lehet.
     */
    @Target(FIELD)
    @Retention(RUNTIME)
    protected @interface State {}

    static class DuplicateRSWInstantiationException extends Exception {
        public DuplicateRSWInstantiationException(String message) {
            super(message);
        }
    }

    private enum WidgetState {
        INITIAL, ATTACHED, DETACHED
    }
}
