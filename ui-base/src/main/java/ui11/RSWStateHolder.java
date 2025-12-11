package ui11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Widget.DuplicateRSWInstantiationException;
import ui11.observable.Observable;
import ui11.observable.Scope;
import ui11.observable.SimpleScope;
import ui11.provide.UpValue;
import ui11.provide.UpValueWrapper;
import ui11.resolution.GlobalViewProviders;
import ui11.resolution.WidgetResolver;

import java.util.Objects;

final class RSWStateHolder<W extends Widget> extends Element {

    private static final Logger logger = LoggerFactory.getLogger(RSWStateHolder.class);

    final WidgetAccessor<W> accessor;

    private W w;
    private boolean initNotDone = true; // ezt lehet hogy ElementStateből is lehet tudni valamennyire
    private boolean insideBuild, cantDetach;
    /**
     * ennek a bezárásakor még a régi IV-k látszódnak
     */
    private SimpleScope untilNextRebuild;

    // TODO lehetne csinálni egy külön tömböt a inheritedeknek, mert ebben most az
    //      egyéb injektáltak (Slot, MultiSlot) is benne van, és feleslegesen hozódik létre tömb illetve
    //      foglalják a helyet a nullok. bár azt is lehet, hogy ha csak nullokat tartalmazna, akkor nem
    //      hozzuk létre a tömböt, mert úgyse olvasná senki, az meg nem nagy probléma hogy ha nemnull elemeken
    //      kívül tartalmaz egy-két nullt.
    final Observable<?>[] observedInheritedValues;

    public RSWStateHolder(WidgetAccessor<W> accessor) {
        this.accessor = accessor;
        observedInheritedValues = accessor.observeInheritedValues(this);
    }

    @Override
    boolean updateUserVisibleModel() {
        if (model.getClass() != accessor.clazz()) // subclass sem lehet
            throw new RuntimeException("wrong model type, expected " +
                    accessor.clazz().getName() + ", but got " + model.getClass().getName());

        W newWidget = accessor.clazz().cast(model);
        Objects.requireNonNull(newWidget, "newWidget");

        if (w == newWidget)
            // egyrészt megspóroljuk canSubstitute ellenőrzést,
            // másrészt lent attachStateHoldert meghívnánk két helyen is this-szel, amire exceptiont dobna.
            return false;

        try {
            if (w == null || initNotDone) {
                // ezt w=newWidget; előtt, hogy ha exceptiont dob, akkor w ne legyen az új
                newWidget.attachStateHolder(this, null);
                if (w != null)
                    w.detachStateHolder(this);

                // TODO ilyenkor w state mezőit ki kéne nullozni
                w = newWidget;
                return true;
            }

            if (accessor.inputFieldsEqualsAndTransferListeners(w, newWidget)) {
                //System.out.println("transfer listeners from "+newWidget+" to "+w);
                // eldobjuk az új widgetet, régit használjuk tovább.
                return false;
            }

            // Ez akkor dobhat DuplicateRSWInstantiationExceptiont, ha alapból newWidget már másik stateHolderhez van kötve.
            // Ha eredetileg nem volt, de valamelyik input mező equals-e során hozzákötődött, akkor ez nem dob exceptiont,
            // mert ahhoz az erőszakoskodó stateHolder build()-je közben kéne lennünk (akkor true a cantDetach).
            newWidget.attachStateHolder(this, w);

            // ez elvileg nem failolhat, mert oldWidget != newWidget, és stateHolder != null
            w.detachStateHolder(this);

            w = newWidget;
            return true;
        } catch (DuplicateRSWInstantiationException e) {
            logger.error("Can't use widget " + newWidget + " in " + this + " because it used somewhere else " +
                    "in the widget tree", e);
            if (w != null)
                w.detachStateHolder(this);
            w = null;
            // build() majd dob exceptiont mivel w==null, amire doRefreshSelf majd beállítja a delegateet
            // delegateCreationFailed üzenetre
            return true;
        }
    }

    @Override
    Widget build() {
        if (w == null)
            throw new RuntimeException("widget was forcefully detached because it is used in another place");

        cantDetach = true;
        try {
            if (initNotDone) {
                // hibát dobunk ha valamelyik @State mezőben nem null van, hogy rávegyük,
                // hogy ne pakoljon a konstruktorba olyanokat amit nem kell vagy nem szabad
                // minden Widget példány létrehozáskor létrehoznia.
                // TODO lehet olyan is, hogy egy RSW példányt az Element fa egy másik helyén használunk újra.
                //      ilyenkor nem hibát kéne dobnunk, hanem kitöltenünk 0-val a mezőket az initState előtt.
                accessor.checkStateEmpty(w);

                w.initState();
                initNotDone = false;
            }
            if (refreshState.isStart)
                // ha ez true, akkor untilNextRebuild null.
                // ha untilNextRebuild lehet null egyébként is, mert csak akkor
                // van létrehozva, ha valaki olvassa.
                w.onResume();
            if (untilNextRebuild != null) {
                untilNextRebuild.close();
                untilNextRebuild = null;
            }

            Widget content;
            insideBuild = true;
            try {
                content = w.build();
            } finally {
                insideBuild = false;
            }
            if (content == null)
                throw new NullPointerException(getClass().getSimpleName() + ".build() returned null on " + this);

            // a decorate lehetne cantDetachon kívül is
            content = accessor.decorate(w, content, true);

            // régi komment, már (2025-12-07) nem tűnik érvényesnek, mert dekorátorok meg lesznek szüntetve:
            //      ha csak a dekoráció invalidálódik, akkor nem kéne build-et meghívni, mert
            //      váratlanul előjöhetne a build() implementáció esetleges nem-idempotenssége miatti hiba.
            //      utóbbit dekorációtól független módon kéne inkább kiszűrni, pl. definiálni valami debug módot,
            //      amikor mondjuk 2 másodpercenként invalidálódik az összes build

            // TODO UpValueWrapperek sorrendje? és dekoráción kívül vagy belül legyenek?
            content = GlobalViewProviders.instance().resolveAdditional(w, content);

            WidgetResolver widgetResolver = findInheritedValueForInjection(WidgetResolver.class, true, null);
            if (widgetResolver != null) {
                content = widgetResolver.resolveAdditional(w, content);
                if (content == null)
                    throw new NullPointerException(
                            WidgetResolver.class.getSimpleName() + ".resolveAdditional returned null\n" +
                                    "Widget: " + w + "\n" +
                                    "Resolver: " + widgetResolver);
            }

            return content;
        } finally {
            cantDetach = false;
        }
    }

    /**
     * már egy másik stateholdernél is szerepel ugyanaz a widget
     *
     * @throws IllegalStateException ha Element.build()-ben vagyunk
     */
    void forcedDetachWidget(W expected) throws DuplicateRSWInstantiationException {
        if (w == expected)
            if (cantDetach)
                throw new DuplicateRSWInstantiationException("can't detach widget at this moment: " + expected + ", from " + this);
            else
                w = null;
        else
            throw new IllegalArgumentException("expected " + expected + ", actual " + w);
    }

    @Override
    Class<W> modelType() {
        return accessor.clazz();
    }

    /**
     * @throws IllegalStateException ha nincs a fában van ez az elem vagy még nem futott le a widget initje
     */
    Scope untilNextRebuild() throws IllegalStateException {
        if (!insideBuild)
            // TODO exceptiont message-et át kéne fogalmazni, mert nem az a lényeg hogy
            //      szó szerint a build függvénytörzséből kell meghívni, hanem hogy annak a futása alatt
            throw new IllegalStateException(Widget.class.getSimpleName() + ".untilNextRebuild()" +
                    " can only be used in build(). Widget: " + w);

        if (untilNextRebuild == null) {
            Scope untilUnmount = scope(); // ez is dobhat ISE-t, de nem fog
            SimpleScope ss = untilNextRebuild = new SimpleScope(untilUnmount);
            untilNextRebuild.onClose(() -> {
                if (untilNextRebuild == ss)
                    untilNextRebuild = null;
                else
                    logger.error("untilNextRebuild changed: expected " + ss + ", actual " + untilNextRebuild);
            });
        }
        return untilNextRebuild;
    }

    Scope untilUnmount() throws IllegalStateException {
        if (initNotDone)
            throw new IllegalStateException(Widget.class.getSimpleName() + ".untilPause()" +
                    " can only be used after init() has been done");
        return scope(); // ez dob ISE-t, ha nincs a fában ez az elem
    }

    boolean isRefreshingSelfOrDescendants() {
        return elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN ||
                elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN ||
                elementState == ElementState.REFRESHING_CHILDREN_AFTER_SELF ||
                elementState == ElementState.REFRESHING_CHILDREN_AFTER_NO_SELF ||
                elementState == ElementState.REFRESHING_CHILDREN_AFTER_NO_SELF_BUT_SELF_REQUESTED_IN_DESCENDANTS ||
                elementState == ElementState.REFRESHING_CHILDREN_SECOND;
    }

    @Override
    public String toString() {
        return super.toString() + (w == null ? " (no current widget)" : ": " + w);
    }

    private W stateWidgetOrThrow() {
        if (w == null)
            throw new IllegalStateException("no state widget: " + this);
        return w;
    }
}
