package ui11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Element.InheritedValueHolder.IVUsage;
import ui11.observable.*;
import ui11.provide.DynamicProvider;
import ui11.provide.Provider;
import ui11.provide.Provider.Mergeable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

// kéne olyan mechanizmus, amivel childrent/cachedPeereket megőrizhetjük stop/start között

// TODO itt a javadocban Lifecycle annotációk elavultak

// TODO ha egy nem-delegate childet vesznek el, akkor meg kéne próbálni visszarakni, és ha upvalue módosul, akkor
//      refreshSelfet igényelni

/**
 * An instantiation of a {@linkplain Widget} at a particular location in the tree.
 * <p>
 * Widgets describe how to configure a subtree but the same widget can be used to configure multiple subtrees
 * simultaneously.  An Element represents the use of a widget to configure a specific location in the tree. Over time,
 * the widget associated with a given element can change, for example, if the parent widget rebuilds and creates a new
 * widget for this location.
 * <p>
 * Elements form a tree. Most user-defined Elements have a unique child, called "delegate".
 * <p>
 * Életciklus: <ul>
 * <li>Egy szülő bejegyzi gyerekének ezt a Elementek. </li>
 * <li>{@code @BeforeStart}-tal annotált függvények meghívódnak</li>
 * <li>{@link #build()} meghívódik</li>
 * <li>{@code @AfterStart}-tal annotált függvények meghívódnak</li>
 * <li>ha módosulnak az {@code update()} által kiolvasott
 * {@linkplain MutableObservable Observable-k}, akkor újra és újra meghívódik {@linkplain #build()}
 * </li>
 * <li>Ha kikerül ez a node a fából, akkor {@code @OnStop} meghívódik utána</li>
 * </ul>
 * Ha utána újra bekerül a fába, akkor újrakezdődik ez a folyamat.
 * <p>
 * Egy Elementhez tartozik egy delegate widget. Ha delegate widget egy Element, akkor neki is van delegateje, ezáltal
 * tetszőleges hosszú láncot képezve.
 * <p>
 * Egy Elementnek tetszőleges számú gyereke lehet. Úgy lehet hozzáadni gyerek Elementet, hogy az {@code build()}
 * implementációjában meghívjuk a {@link Element#instantiate(Slot, Widget, RefreshID, List)}-et. Ha az egyik updatekor egy
 * adott elemenetet hozzáadtunk gyerekként, míg a következő updateben már nem, akkor az a gyerek törlődik.
 */
class Element {

    // TODO ha nincs slf4j impl, akkor ez NOP logger lesz
    static final Logger logger = LoggerFactory.getLogger(Element.class);
    static final boolean TRACE_REFRESH = false;

    @SuppressWarnings("PointlessBitwiseExpression")
    private static final int REFRESH_REASON_MODEL = 1 << 0;
    private static final int REFRESH_REASON_DIRECT_IV_USED_BY_SELF = 1 << 1;
    private static final int REFRESH_REASON_DIRECT_IV_USED_BY_CHILDREN = 1 << 2;
    private static final int REFRESH_REASON_INDIRECT_IV_USED_BY_SELF = 1 << 3;
    private static final int REFRESH_REASON_INDIRECT_IV_USED_BY_CHILDREN = 1 << 4;
    private static final int REFRESH_REASON_RESTART = 1 << 5;
    private static final int REFRESH_REASON_OBSERVABLES = 1 << 6;
    private static final int REFRESH_REASON_START = 1 << 7;

    private static final int REFRESH_REASON_MASK_SELF = REFRESH_REASON_MODEL |
            REFRESH_REASON_DIRECT_IV_USED_BY_SELF | REFRESH_REASON_INDIRECT_IV_USED_BY_SELF |
            REFRESH_REASON_START | REFRESH_REASON_RESTART | REFRESH_REASON_OBSERVABLES;
    private static final int REFRESH_REASON_MASK_ALL_CHILDREN =
            REFRESH_REASON_DIRECT_IV_USED_BY_CHILDREN | REFRESH_REASON_INDIRECT_IV_USED_BY_CHILDREN;

    final @NonNull ObservableHelper observableHelper = new ObservableHelper(this);
    @NonNull ElementState elementState = ElementState.INITIAL;
    @Nullable Element parent;
    final @NonNull List<Element> children = new ArrayList<>();

    boolean addedToParentInCurrentRefreshStateOfParent;

    // TODO ezt lehet törölni, mivel már UpValueWrapper.next nem létező fogalom EndingWidget átállás óta
    /**
     * Ez akkor és csak akkor nem null, ha parent nem null. Az elemei közt nem szerepel null.
     */
    List<? extends SubstitutedWidget> directAncestorUpValues;

    /**
     * Ez akkor és csak akkor nem null, ha elementState == ElementState.REFRESHING_SELF
     */
    RefreshID refreshID;

    /**
     * Csak akkor értelmezhető, ha elementState == {@code REFRESHING_CHILDREN_*}.<p> Azért kell, hogy a refresh során
     * egy korábbi gyerekünk leszármazottja tudja kérni későbbi gyerekünk leszármazottjának refreshét. <p>
     * MultiChildLayoutnál jött elő, nem értem hogy mire kell ez. Valszeg ahhoz van köze, hogy mivel a későbbi még nem
     * refreshelt, ezért nem tudta a saját observereit, ezért közöttük vannak még feleslegesek.
     */
    int refreshingChildIndex;
    /**
     * {@link #refreshingChildIndex}-hez. Azért nem jó a sima {@link #children}, mert az változhat a
     * {@code refreshChildren} közepén is (ha egy descendant eltulajdonítja az egyik gyerekünket).
     */
    Element[] refreshingChildren;

    // delegate visszarakáshoz
    Widget delegateWidget;

    // nem Observable, mert nincs értelme feliratkozni rá, helyette a descendant által provided értékeket kell figyelni
    WidgetInstantiation delegate;

    Map<Class<?>, Object> directIVs;

    /**
     * Azon öröklött értékek szerepelnek benne, amik: <ul>
     * <li>ez az elem bármely leszármazottja által használva van, de nem ez az elem egyik
     * leszármazottja által van biztosítva
     * <li>ez az elem által van használva
     * <li>ennek az elemnek a szülője által van biztosítva.
     * <p>
     * Akkor és csak akkor null, ha ez az Element egy RootElement.
     */
    final Map<Class<?>, InheritedValueHolder> inheritedValues =
            this instanceof WidgetTree.RootElement ? null : new HashMap<>();

    /**
     * Ez üres, ha parent == null.
     * <p>
     * A valuek azok, amelyek nem {@link #directAncestorUpValues}-beliek.
     */
    final Map<Class<? extends SubstitutedWidget>, SubstitutedWidget> parentInterestedUpValues = new HashMap<>();
    final InvalidationPoint upValuesIP = new InvalidationPoint();

    WidgetResolver vp;

    Slot delegateSlot; // lazy létrehozás, mert a fa alján nem kell

    private SimpleScope activeScope;
    SimpleScope refreshScope;

    WidgetState<?> currentState;

    /**
     * ennek a bezárásakor még a régi IV-k látszódnak
     */
    private SimpleScope untilWidgetStateNextRebuild;

    /**
     * ez lehet state, de lehet nem state role-ú is
     */
    private Widget nextWidget;

    void setWidget(Widget w) {
        if (!Widget.class.isInstance(w)) // TeaVM-es kód bugjakor előjött egy ilyen, hogy nem Widget volt a megadott
            throw new RuntimeException("not a widget: " + w);
        nextWidget = w;
    }

    /**
     * ezt nem szabad meghívni, ha inactivatable állapotban vagyunk
     */
    void refresh() {
        if (elementState != ElementState.REFRESH_REQUESTED &&
                elementState != ElementState.START_REQUESTED &&
                elementState != ElementState.RESTART_REQUESTED &&
                !delegateParentMismatch())
            // önmagában delegateParentMismatch csak lookupkor elegendő indok a refreshre
            throw new IllegalStateException("can't refresh in " + elementState.toString() + ": " + this);

        assert ObserverHolder.hasNoObserver();

        if (TRACE_REFRESH)
            TraceRefresh.TL.get().begin("BEGIN " + this.toString() + " (" + elementState + ")");

        ElementState elementState1 = elementState;
        boolean isStartOrRestart = elementState == ElementState.START_REQUESTED ||
                elementState == ElementState.RESTART_REQUESTED;
        boolean isStart = elementState == ElementState.START_REQUESTED;

        try {
            if (isStartOrRestart) {
                activeScope = new SimpleScope(Scope.global());
            }

            // azért reasont tárolunk el és nem csak 2 boolt, hogy debuggerben könnyebben lehessen visszakövetni
            int toBeRefreshed = 0;

            if (!(this instanceof WidgetTree.RootElement)) // ha egy IV hiányzik, a delegateet be kéne állítani valami hibaüzenetre
                toBeRefreshed |= refreshIVs();

            if (updateUserVisibleModel())
                toBeRefreshed |= REFRESH_REASON_MODEL;

            switch (elementState) {
                case RESTART_REQUESTED -> {
                    // TODO if (observableHelper.resume())
                    // childek elkavarodhatnak, ezért inkább mindenképpen selfet is refreshelünk
                    observableHelper.resume();
                    toBeRefreshed |= REFRESH_REASON_RESTART;
                }
                case REFRESH_REQUESTED -> {
                    // itt az ifben volt eredetileg !toBeRefreshed.self, de az nem jó,
                    // mert haveBeenInvalidated törli az OH-ból az invalidated státuszt,
                    // ezért muszáj meghívni (meg amúgy sem lassú)
                    if (observableHelper.haveBeenInvalidated())
                        toBeRefreshed |= REFRESH_REASON_OBSERVABLES;
                }
                case START_REQUESTED -> toBeRefreshed |= REFRESH_REASON_START;
                case IDLE -> { // TODO IDLE_STOPPABLE-kor lehetséges delegateParentMismatch?
                    if (!delegateParentMismatch()) // lehet hogy ezt bele kéne kódolni ElementStatebe
                        throw new RuntimeException(elementState.toString());
                }
                default -> throw new RuntimeException(elementState.toString());
            }

            assert elementState == elementState1;

            if ((toBeRefreshed & REFRESH_REASON_MASK_SELF) != 0) {
                if (TRACE_REFRESH)
                    TraceRefresh.TL.get().print("refreshSelf");

                elementState = ElementState.REFRESHING_SELF_BEFORE_CHILDREN;
                observableHelper.removeObserversIfNeeded();

                if (refreshScope != null && !refreshScope.isClosed())
                    refreshScope.close();
                refreshScope = new SimpleScope(activeScope);

                refreshSelf(isStartOrRestart, isStart);

                elementState = ElementState.REFRESHING_CHILDREN_AFTER_SELF;
            } else {
                if (delegate != null && delegate.element != null && delegate.element.parent != this) {
                    // elvették tőlünk a delegateet, és nincs refreshSelf se kérve.
                    // ezért vissza kell rakni.
                    if (delegateSlot == null)
                        delegateSlot = new Slot(null);
                    delegate = instantiate(delegateSlot, delegateWidget, delegate.refresh, delegate.upValues);
                }

                elementState = ElementState.REFRESHING_CHILDREN_AFTER_NO_SELF;
            }

            if (TRACE_REFRESH)
                TraceRefresh.TL.get().print(elementState.name());

            refreshChildren((toBeRefreshed & REFRESH_REASON_MASK_ALL_CHILDREN) != 0);
            // TODO lehet hogy refreshChildren közben veszik el a delegateünket

            if (elementState == ElementState.REFRESHING_CHILDREN_AFTER_NO_SELF_BUT_SELF_REQUESTED_IN_DESCENDANTS) {
                if (TRACE_REFRESH)
                    TraceRefresh.TL.get().print("refresh self (2)");
                elementState = ElementState.REFRESHING_SELF_AFTER_CHILDREN;
                observableHelper.haveBeenInvalidated();
                observableHelper.removeObserversIfNeeded();
                refreshSelf(isStartOrRestart, isStart);

                elementState = ElementState.REFRESHING_CHILDREN_SECOND;

                if (TRACE_REFRESH)
                    TraceRefresh.TL.get().print("refresh children (2)");
                refreshChildren((toBeRefreshed & REFRESH_REASON_MASK_ALL_CHILDREN) != 0);
            }

            invalidateParentIfUpValuesChanged();

            elementState = ElementState.IDLE;
            observableHelper.debug_assertStateNormal();
            verifyParentOfDelegateIsThis();
        } catch (Throwable e) {
            // TODO ilyenkor OH invalidated állapotban maradhatott
            elementState = ElementState.IDLE;
            logger.error("Failed to refresh " + this, e);
        } finally {
            if (TRACE_REFRESH)
                TraceRefresh.TL.get().end("END " + this);
        }
    }

    private void verifyParentOfDelegateIsThis() {
        if (delegate != null && delegate.element != null && delegate.element.parent != this)
            throw new RuntimeException("parent of delegate not this: \n" +
                    "This: " + this + "\n" +
                    "Delegate: " + delegate.element + "\n" +
                    "Parent of delegate: " + delegate.element.parent + "\n" +
                    "Refresh stack: \n" + refreshStackToString(
                    delegate.element.parent == null ?
                            Map.of(delegate.element, "delegate") :
                            Map.of(delegate.element, "delegate",
                                    delegate.element.parent, "parent of delegate")
            ));
    }

    /**
     * @return true, ha kell refreshSelf a modelváltozás miatt, különben false
     */
    private boolean updateUserVisibleModel() {
        Objects.requireNonNull(nextWidget, "nextWidget");

        if (currentState != null && currentState.effectiveModel() == nextWidget)
            // fast-path
            return false;

        nextWidget.initListenerProxyData();

        WidgetState.ChangeModelResult changeModelResult;
        if (currentState == null)
            changeModelResult = WidgetState.ChangeModelResult.NEEDS_NEW_STATE;
        else
            changeModelResult = currentState.tryChangeModel(nextWidget);

        return switch (changeModelResult) {
            case NEEDS_NEW_STATE -> {
                // új widget state-et kell létrehozni

                WidgetState<?> prevState = currentState;
                if (prevState != null)
                    prevState.dispose();

                // ezt azért, hogy ha accessor() exceptiont dob, akkor ne maradjon elavult érték currentStateben
                currentState = null;

                currentState = new WidgetState<>(this, nextWidget);

                // TODO mit csináljunk, ha konstruktor exceptiont dob?

                yield true;
            }
            case MODEL_IS_SAME_AS_BEFORE -> false;
            case MODEL_CHANGED -> true;
        };
    }

    // TODO ha ez nem sikerül (pl. nem létezik egyik IV), akkor delegatenek ki kéne azért rakni valamit

    /**
     * @return refresh reason mask
     */
    private int refreshIVs() {
        assert !(this instanceof WidgetTree.RootElement);

        // bitmask REFRESH_REASON_... konstansokból
        int changed = 0;

        if (directIVs != null) {
            for (Entry<Class<?>, Object> entry : directIVs.entrySet()) {
                assert !(entry.getValue() instanceof InheritedValueHolder);

                Class<?> type = entry.getKey();
                InheritedValueHolder ivh = inheritedValues.get(type);
                if (ivh == null) {
                    ivh = new InheritedValueHolder();
                    ivh.value = entry.getValue();
                    ivh.usage.add(IVUsage.PARENT_PROVIDED);
                    inheritedValues.put(type, ivh);
                } else {
                    if (!Objects.equals(ivh.value, entry.getValue())) {
                        ivh.value = entry.getValue();
                        if (ivh.usage.contains(IVUsage.USED_BY_SELF))
                            changed |= REFRESH_REASON_DIRECT_IV_USED_BY_SELF;
                        if (ivh.usage.contains(IVUsage.USED_BY_CHILDREN))
                            changed |= REFRESH_REASON_DIRECT_IV_USED_BY_CHILDREN;
                    }
                    ivh.usage.add(IVUsage.PARENT_PROVIDED);
                }
            }
            // directIVs = null;
        }

        for (Iterator<Entry<Class<?>, InheritedValueHolder>> iterator =
             inheritedValues.entrySet().iterator(); iterator.hasNext(); ) {

            assert parent != null;

            Entry<Class<?>, InheritedValueHolder> entry = iterator.next();
            Class<?> type = entry.getKey();
            InheritedValueHolder iv = entry.getValue();

            if (iv.usage.contains(IVUsage.PARENT_PROVIDED)) {
                if (directIVs.containsKey(type))
                    // már a helyes érték van benne, és changedIV* is állítva van
                    continue;
                else {
                    iv.usage.remove(IVUsage.PARENT_PROVIDED);
                    if (iv.usage.isEmpty()) {
                        // nincs használva és nem is biztosítjuk mi az értéket, ezért töröljük
                        iterator.remove();
                        continue;
                    }
                }
            }

            Object newValue = retrieveNonDirectIVValue(type);
            // equals sorrendjének konzisztensnek kéne lennie a fentivel, illetve el kéne dönteni hogy ha equals,
            // akkor a régit vagy az újat hagyjuk benne. valszeg a régit.
            // ld. még putIVsToFields
            if (!Objects.equals(newValue, iv.value)) {
                iv.value = newValue;
                if (iv.usage.contains(IVUsage.USED_BY_SELF))
                    changed |= REFRESH_REASON_INDIRECT_IV_USED_BY_SELF;
                if (iv.usage.contains(IVUsage.USED_BY_CHILDREN))
                    changed |= REFRESH_REASON_INDIRECT_IV_USED_BY_CHILDREN;
            }

            // TODO USED_BY_*-eket ki kéne szedni a setből, ha elavulnak
        }

        // ez nem ide kéne
        vp = findInheritedValueForInjection(WidgetResolver.class, true, "vp");

        return changed;
    }

    private void refreshSelf(boolean isStartOrRestart, boolean isStart) {
        refreshID = new RefreshID();
        for (Element e : children) {
            switch (e.elementState) {
                case IDLE -> e.elementState = ElementState.IDLE_STOPPABLE;
                case REFRESH_REQUESTED -> e.elementState = ElementState.REFRESH_REQUESTED_STOPPABLE;
                case IDLE_STOPPABLE, REFRESH_REQUESTED_STOPPABLE -> {
                    // nop
                }
                default -> {
                    logger.error("Unknown state of child at refreshSelf: " + e.elementState + ", " + e);
                    // talán kiírhatnánk itt ancestorokat is
                }
            }
            e.parentInterestedUpValues.clear();
        }
        ObserverHolder h = ObserverHolder.current();
        ObserverCollection prevObsC = h.obsC;
        int prevObsI = h.obsI;

        h.obsC = observableHelper;
        h.obsI = 1;
        try {
            doRefreshSelf();
        } finally {
            h.obsC = prevObsC;
            h.obsI = prevObsI;

            refreshID = null;
            for (Element e : children)
                e.addedToParentInCurrentRefreshStateOfParent = false;
        }
        // TODO tisztítsuk majd meg byKeyt
        WidgetTree.RootElement context = root();
        for (Element e : children)
            if (e.elementState == ElementState.IDLE_STOPPABLE || e.elementState == ElementState.REFRESH_REQUESTED_STOPPABLE)
                context.submitForDispose(e);
    }

    private WidgetTree.RootElement root() {
        Element e = this;
        while (e.parent != null)
            e = e.parent;
        return (WidgetTree.RootElement) e;
    }

    // TODO fieldName-et nem kéne bewrappelnie "field '...'" stringbe,
    //      mert így hülyén néz ki több helyen, pl. @Content metódus paraméter esetén,
    //      meg template condition variable esetén is
    // fieldName csak inherited() miatt nullable
    @Nullable
    final <T> T findInheritedValueForInjection(Class<T> type, boolean optional, @Nullable String fieldOrParameterName) {
        Object value;
        if (PeerCreationRequest.class.isAssignableFrom(type) && type != PeerCreationRequest.class) {
            value = findInheritedValue(PeerCreationRequestCollection.class, IVUsage.USED_BY_SELF);
            if (value != IVNotProvided.IV_NOT_PROVIDED) {
                PeerCreationRequestCollection requestCollection = (PeerCreationRequestCollection) value;
                if (requestCollection.request.getClass() == type)
                    value = requestCollection.request;
                else
                    value = IVNotProvided.IV_NOT_PROVIDED;
            }
        } else
            value = findInheritedValue(type, IVUsage.USED_BY_SELF);
        if (value == IVNotProvided.IV_NOT_PROVIDED)
            if (optional)
                value = null; // TODO ha primitív típus, akkor nyilván nem null kéne
            else {
                StringBuilder msg = new StringBuilder("inherited value for " +
                        type.getName() + (fieldOrParameterName == null ? " (used by " + fieldOrParameterName + ")" : "") +
                        " not supplied for " + this + "; Ancestors: ");
                int depth = 0;
                for (Element e = this; e != null; e = e.parent)
                    depth++;
                for (Element e = this; e != null; e = e.parent)
                    msg.append("\n").append(--depth).append(". ").append(e);
                throw new RuntimeException(msg.toString());
            }
        return type.cast(value); // TODO primitív típusokkal mi legyen?
    }

    /**
     * @return IVNotProvided.IV_NOT_PROVIDED, ha nincs találat
     */
    // felülírva RootElementben
    Object findInheritedValue(Class<?> type, IVUsage ivUsage) {
        switch (ivUsage) {
            case PARENT_PROVIDED -> throw new IllegalArgumentException();
            case USED_BY_SELF -> {
                assert elementState == ElementState.START_REQUESTED ||
                        elementState == ElementState.RESTART_REQUESTED ||
                        elementState == ElementState.REFRESH_REQUESTED ||
                        elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN ||
                        elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN;
                // REFRESHING_SELF csak azért, mert child() is használja IV mergekor
            }
            case USED_BY_CHILDREN -> {
                assert elementState == ElementState.REFRESHING_CHILDREN_AFTER_SELF
                        || elementState == ElementState.REFRESHING_CHILDREN_AFTER_NO_SELF
                        || elementState == ElementState.REFRESHING_CHILDREN_AFTER_NO_SELF_BUT_SELF_REQUESTED_IN_DESCENDANTS
                        || elementState == ElementState.REFRESHING_CHILDREN_SECOND
                        || elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN // ensureFresh esetén
                        || elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN;
            }
            default -> throw new RuntimeException();
        }

        if (inheritedValues.containsKey(type)) {
            InheritedValueHolder ivh = inheritedValues.get(type);
            assert !(ivh.value instanceof InheritedValueHolder);
            ivh.usage.add(ivUsage);
            return ivh.value;
        }

        Object val = retrieveNonDirectIVValue(type);

        assert !(val instanceof InheritedValueHolder);
        InheritedValueHolder ivh = new InheritedValueHolder();
        ivh.usage.add(ivUsage);
        ivh.value = val;
        InheritedValueHolder ivhInMap = inheritedValues.putIfAbsent(type, ivh);
        assert ivhInMap == null;
        return ivh.value;
    }

    private Object retrieveNonDirectIVValue(Class<?> type) {
        InheritedValueHolder dpIVH = inheritedValues.get(DynamicProvider.class);
        Object val = null;
        if (dpIVH != null && dpIVH.usage.contains(IVUsage.PARENT_PROVIDED)) {
            DynamicProvider dp = Objects.requireNonNull((DynamicProvider) dpIVH.value);
            val = dp.provideOrNull(type);
        }

        if (val == null)
            val = parent.findInheritedValue(type, IVUsage.USED_BY_CHILDREN);
        return val;
    }

    private void invalidateParentIfUpValuesChanged() {
        if (parent != null) {
            // TODO ezzel valami nem stimmel. ha ugyanazzal a keyjel instantiate-elek egy elemet, akkor
            //      a másodiknak a ensureFresh-jekor ez a hiba jön elő:
            //      ui11.ObservableHelper: Observed value was invalidated, but node is in REFRESHING_SELF_BEFORE_CHILDREN state: DOMGridPeer@9dba2e70
            //      reprodukálható, ha kiszedjük DOMGridPeerből a overlay számolást és belépünk bowling lobbiba
            for (Entry<Class<? extends SubstitutedWidget>, SubstitutedWidget> entry : parentInterestedUpValues.entrySet()) {
                Class<? extends SubstitutedWidget> type = entry.getKey();
                Object val = entry.getValue();
                Object newVal = lookupImpl(type, true, true);
                if (!Objects.equals(val, newVal)) {
                    System.out.println("invalidate parent because " + type.getSimpleName() + " changed from " + val + " to " + newVal);
                    System.out.println("Parent: " + parent);
                    System.out.println("This: " + this);
                    parent.upValuesIP.invalidate();
                    break;
                }
            }
        }
    }

    /**
     * Meghívja a {@link #build()} függvényt, és eltárolja annak az az által visszaadott értéket ennek az Elementnek a
     * delegateként.
     */
    void doRefreshSelf() {
        try {
            Widget widget = currentState.stateWidget;
            Widget content = build();
            if (content == null && !(widget instanceof SubstitutedWidget))
                // el kéne dönteni hogy lehet-e null. ha igen, akkor withKey-ben is kezelni kéne.
                throw new NullPointerException("Element.build() returned null on " + this);
            // TODO ha az előző delegate bewrappelődik egy másik widgetbe, akkor
            //      fölöslegesen refresheljük az előző delegateet, mert már
            //      csak a wrapperben kéne.
            //      lehet hogy a children elejére kéne rakni valahogy (vagy legalább az előző delegate elé).
            if (delegateSlot == null)
                delegateSlot = new Slot(null);

            List<? extends SubstitutedWidget> upValues = switch (widget) {
                case ParentDataWidget.CombinerParentDataWidget combinerParentDataWidget ->
                        List.of(combinerParentDataWidget.parentData);
                case SubstitutedWidget substitutedWidget -> List.of(substitutedWidget);
                default -> List.of();
            };

            if (content == null)
                delegate = new WidgetInstantiation(this, refreshID, null, upValues);
            else
                delegate = instantiate(delegateSlot, delegateWidget = content, refreshID, upValues);
        } catch (Throwable t) {
            delegate = delegateCreationFailed(t);
        }

        if (Element.TRACE_REFRESH)
            TraceRefresh.TL.get().print(this + " new delegate: " + delegate);
    }

    @Nullable
    private WidgetInstantiation delegateCreationFailed(Throwable t) {
        WidgetInstantiation delegateHandle;
        logger.error("Failed to make delegate for " + this, t);

        // TODO Errorok esetén se kéne feltétlen továbbdobni.
        //      pl. UnsatisfiedLinkError előfordult már
        //      (TeaVM metaprogramming API-t használó kódot próbáltam futtatni JVM-en).
        //      talán csak OutOfMemoryErrort és StackOverflowErrort kéne továbbdobni.
        //      viszont akkor is úgy, hogy a delegate ne maradjon elavult.


        if (t instanceof Error e && !(e instanceof AssertionError))
            throw e;

        try {
            Iterator<ErrorWidgetFactory> sl = ServiceLoader.load(ErrorWidgetFactory.class).iterator();
            if (sl.hasNext()) {
                ErrorWidgetFactory errorWidgetFactory = sl.next();
                Widget widget = errorWidgetFactory.makeDelegateCreationError(t);
                if (delegateSlot == null)
                    delegateSlot = new Slot(null);
                delegateHandle = instantiate(delegateSlot, delegateWidget = widget, refreshID, List.of());
                // TODO StackOverflowError lesz, ha nem tudja a hibaüzenetet sem megjeleníteni
            } else {
                logger.error("No error widget factory");
                delegateHandle = null;
            }
            return delegateHandle;
        } catch (RuntimeException | AssertionError e2) {
            e2.addSuppressed(t);
            logger.error("Failed to make error widget after delegate creation error on " + this, e2);
            return null;
            // jobb híján null lesz a delegateHandleben. így a parentben is exception lesz, de nem tudunk jobbat.
        }
    }

    /**
     * Meghatározza hogy mi legyen ennek a Elementnek a delegate-je.
     * <p>
     * Ez a függvény meg fog hívódni újra, ha egy, a végrehajtása során kiolvasott {@link ObservableBase} módosul.
     *
     * @return nem szabad nullt visszaadnia
     */
    // TODO ez most restartkor mindenképpen meghívódik. ezen lehet
    //      hogy változtatni kéne. de ha változtatunk, akkor
    //      LottieWebAnimationPeer-t is át kell írni, meg sok más E-t is.
    private Widget build() {
        if (currentState == null)
            throw new IllegalStateException();

        closeUntilNextRebuildScope();

        currentState.retrieveInheritedValues();
        currentState.callInitIfNotCalled();
        currentState.callOnResumeIfNotCalled();

        Widget content = currentState.stateWidget.build();
        if (content == null)
            if (currentState.stateWidget instanceof SubstitutedWidget)
                return null;
            else
                throw new NullPointerException(getClass().getSimpleName() +
                        ".build() returned null on " + currentState);

        // ha csak a decorate invalidálódik, akkor nem kéne build-et meghívni, mert
        // váratlanul előjöhetne a build() implementáció esetleges nem-idempotenssége miatti hiba.
        // utóbbit dekorációtól független módon kéne inkább kiszűrni, pl. definiálni valami debug módot,
        // amikor mondjuk 2 másodpercenként invalidálódik az összes build
        content = currentState.decorateChild(content);

        return content;
    }

    private void refreshChildren(boolean all) {
        assert children.stream().allMatch(e -> e.parent == this);
        // azért kell másolni, mert más Element ensureChildje elveheti tőlünk a gyereket
        Element[] childrenCopy = children.toArray(Element[]::new);
        this.refreshingChildren = childrenCopy;
        try {
            for (int i = 0; i < childrenCopy.length; i++) {
                this.refreshingChildIndex = i;
                Element e = childrenCopy[i];
                if (e.parent != this)
                    continue;
                switch (e.elementState) {
                    case IDLE -> {
                        if (all) {
                            e.elementState = ElementState.REFRESH_REQUESTED;
                            e.refresh();
                        }
                    }
                    case IDLE_STOPPABLE, REFRESH_REQUESTED_STOPPABLE -> {
                        // skip
                    }
                    case START_REQUESTED,
                         REFRESH_REQUESTED, RESTART_REQUESTED -> {
                        e.refresh();
                    }
                    default -> throw new RuntimeException("unexpected state " +
                            e.elementState + " for " + e + " (parent: " + this + ")");
                }
            }
        } finally {
            refreshingChildren = null;
            // mivel refreshingChildren null lesz, ezért mindegy hogy ide mit írunk, mert úgyis NPE lesz,
            // ha egy gyerek össze akarná hasonlítani refreshingChildren-beli indexével
            refreshingChildIndex = -2;
        }
    }

    /**
     * Ennek az Elementnek a gyerekévé teszi a megadott Widgetből képezhető egy Elementet, ami a következő frissítésig
     * aktív fog maradni. Csak a frissítés közben hívható meg.
     *
     * @throws IllegalStateException ha nem REFRESHING_SELF állapotban van jelenleg az Element
     * @throws NullPointerException  ha a megadott widget {@code null}
     */
    // TODO ideiglenes instantiate? pl. MultiChildLayouthoz
    // TODO duplicate key detektálása, pl. DOMGridPeernél az egymásra rakható elemek kapcsán előjött
    //      upValues invalidálás bugot óráig tartott debugolni
    WidgetInstantiation instantiate(Slot slot, Widget widget, RefreshID refreshState,
                                    List<? extends SubstitutedWidget> upValues) {
        Objects.requireNonNull(slot, "key");
        Objects.requireNonNull(widget, "widget");
        Objects.requireNonNull(upValues, "upValues");

        Map<Class<?>, Object> ivs = new HashMap<>();

        while (true) {
            Objects.requireNonNull(slot);
            switch (widget) {
                case null -> {
                    throw new NullPointerException("CSB null " + this + ", " + upValues);
                }
                case Provider<?> p -> {
                    Object val = p.value();

                    // részben azért nem val instanceof Mergeable-t nézünk, hogy null esetén is működjön,
                    // részben pedig hogy findIVProvidesUntil nem a példány típusából, hanem a megadott típusból
                    // dönti el, hogy directIVsből vagy a directAncestorEDs-ből szedje az értékeket.
                    final boolean isMergeableType = Mergeable.class.isAssignableFrom(p.type()) || p.type() == DynamicProvider.class;

                    // p.ignoreMergeableType helyett eredetileg azt néztük, hogy
                    // currentState.stateWidget instanceof InheritedValueMerger. de ez nem működik helyesen,
                    // ha két egymásba ágyazott mergeölhető Provider van.
                    if (isMergeableType && !p.ignoreMergeableType) {
                        widget = new InheritedValueMerger<>(p);
                    } else {
                        ivs.put(p.type(), val);
                        widget = p.content();
                    }
                }
                case KeyWrapper kw -> {
                    slot = kw.slot;
                    widget = kw.content;
                }
                default -> {
                    return handleRegularWidget(widget, slot, refreshState, upValues, ivs);
                }
            }
        }
    }

    private @NonNull WidgetInstantiation handleRegularWidget(Widget widget, Slot slot,
                                                             RefreshID refreshState,
                                                             List<? extends SubstitutedWidget> upValues,
                                                             Map<Class<?>, Object> ivs) {
        Element peer = slot.element;

        peer.setWidget(widget);
        registerChild(peer);

        peer.directAncestorUpValues = upValues;
        peer.directIVs = ivs;
        return new WidgetInstantiation(this, refreshState, peer, upValues);
    }

    // TODO ez így inkonzisztens, mert ha a parent REFRESHING_CHILDREN_* állapotban van,
    //      akkor refreshSelfet kér, különben pedig nem
    void requestRefresh() {
        // TODO ha a végén exceptiont dobunk, akkor az előzőekben átállított elementState-eket vissza kéne állítani
        for (Element e = this, prev = null; ; ) {
            switch (e.elementState) {
                case INITIAL, STOPPED, REFRESHING_SELF_BEFORE_CHILDREN, REFRESHING_SELF_AFTER_CHILDREN -> {
                    throw cantRequestRefresh(e);
                }
                case REFRESHING_CHILDREN_AFTER_SELF, REFRESHING_CHILDREN_SECOND -> {
                    if (prev == null)
                        // refreshSelf-et már nem tudunk kérni, mert már megtörtént
                        throw cantRequestRefresh(e);
                    else {
                        // nem refreshSelf kell, csak refreshChildren
                        if (e.willRefreshChild(prev))
                            // el fog jutni hozzánk a refreshChildren ciklus
                            return;
                        else
                            throw cantRequestRefresh(e);
                    }
                }
                case REFRESHING_CHILDREN_AFTER_NO_SELF -> {
                    if (prev == null) {
                        // refreshSelfet kérünk
                        e.elementState = ElementState.REFRESHING_CHILDREN_AFTER_NO_SELF_BUT_SELF_REQUESTED_IN_DESCENDANTS;
                        return;
                    } else {
                        // nem refreshSelf kell, csak refreshChildren
                        if (e.willRefreshChild(prev))
                            // el fog jutni hozzánk a refreshChildren ciklus
                            return;
                        else {
                            // elvileg lehetséges lenne, ha refreshSelf-et igényelnénk.
                            // de ez egyrészt felesleges refreshSelf-et produkál,
                            // másrészt semmi értelmes felhasználása nincs.
                            throw cantRequestRefresh(e);
                        }
                    }
                }
                case REFRESHING_CHILDREN_AFTER_NO_SELF_BUT_SELF_REQUESTED_IN_DESCENDANTS -> {
                    // Ha e == this, akkor azért nop mert már volt requestelve.
                    // Ha e != this, akkor meg azért, mert refreshSelf után úgyis lesz még
                    // egy REFRESHING_CHILDREN_SECOND. Ez utóbbi esetben viszont nem vagyok biztos:
                    // nem tűnik értelmes dolognak refresh childrent kérni ilyen állapotban,
                    // de egyelőre így hagyom (2025-11-29), mert eddig is így volt.
                    return;
                }
                case START_REQUESTED, RESTART_REQUESTED, REFRESH_REQUESTED, REFRESH_REQUESTED_STOPPABLE -> {
                    // nop
                    return;
                }
                case IDLE -> {
                    e.elementState = ElementState.REFRESH_REQUESTED;
                }
                case IDLE_STOPPABLE -> {
                    // ez akkor történhet, ha pl. subscribeolt observable módosul egy olyan elemnél ami már kikerült a fából
                    e.elementState = ElementState.REFRESH_REQUESTED_STOPPABLE;
                }
                default -> throw new RuntimeException("can't request refresh while in " + e.elementState);
            }

            if (e.parent == null) {
                if (e instanceof WidgetTree.RootElement rootElement) {
                    rootElement.requestRootRefresh();
                    return;
                } else
                    // nem lehetséges
                    throw new RuntimeException("have no parent, but not " + WidgetTree.RootElement.class.getSimpleName() + ": " + e);
            } else {
                if (e.parent.elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN ||
                        parent.elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN)
                    return;

                prev = e;
                e = e.parent;
            }
        }
    }

    private boolean willRefreshChild(Element child) {
        if (refreshingChildren == null)
            return false;

        // ez így lassú, ha sok child van.
        // lehet ElementState-be belekódolni (IDLE állapotot szétválasztjuk IDLE és IDLE_WILL_BE_REFRESHED_AS_CHILD
        // állapottá), de az meg konstans időt ad hozzá egy refreshhez ami nem biztos hogy kifizetődő mert
        // ez a kavarás a refreshChildren közbeni updatetel valszeg nem olyan gyakori, másrészt
        // nem is tipikus a sok children.

        for (int i = refreshingChildIndex + 1; i < refreshingChildren.length; i++) {
            if (refreshingChildren[i] == child)
                return true;
        }
        return false;
    }

    private @NonNull IllegalStateException cantRequestRefresh(Element e) {
        return new IllegalStateException("Cannot request refresh of " + this +
                " because " + e + " is in " + e.elementState + ". \n" +
                // nem feltétlen szerepel a refresh stackben a this
                "Ancestors of requested refresh element: " + debug_ancestors() + "\n" +
                "Refresh stack: \n" +
                e.refreshStackToString(Map.of(this, "ORIG")));
    }

    /**
     * ezután e1 refreshelve lesz
     */
    void registerChild(@NonNull Element e1) {
        assert elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN || elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN;
        if (e1.elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN || e1.elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN)
            throw new IllegalStateException();

        if (e1.parent != this) {
            // TODO rekurzió detektálása

            if (e1.parent != null) {
                if ((e1.parent.elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN ||
                        e1.parent.elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN) &&
                        e1.addedToParentInCurrentRefreshStateOfParent /* lehet hogy van rájuk működő EH */)
                    // TODO IDLE (és START_REQUESTED, stb.) esetében se kéne engedni
                    throw new RuntimeException("can't remove element from a parent in REFRESHING_SELF state: " + e1 +
                            ", parent:" + e1.parent + ", wanna be parent: " + this);

                e1.parent.children.remove(e1);

                /*
                nem baj végülis ha elveszi a delegateet, de figyelni kell rá, hogy lookupkor visszarakjuk.
                lásd TestStealDelegate.
                if (e1.parent.delegate.element == e1)
                    logger.warn("Remove delegate from " + e1.parent + "\n" +
                            "Delegate: " + e1 + "\n" +
                            "New parent: " + this + "\n" +
                            "Refresh stack: \n" +
                            refreshStackToString(Map.of(e1.parent, "old parent", e1, "child", this, "new parent")));
                 */
            }
            e1.parent = this;
            e1.parentInterestedUpValues.clear();
            e1.addedToParentInCurrentRefreshStateOfParent = true;
            children.add(e1);

            switch (e1.elementState) {
                case INITIAL ->
                    // ez nem pontos, mert nem "requested", csak vár rá
                        e1.elementState = ElementState.START_REQUESTED;
                case STOPPED ->
                    // ld. fenti komment
                        e1.elementState = ElementState.RESTART_REQUESTED;
                case IDLE, IDLE_STOPPABLE, REFRESH_REQUESTED_STOPPABLE -> {
                    // mert IV-k illetve directAncestorED-k (ezáltal a Tagok) megváltozhattak
                    e1.elementState = ElementState.REFRESH_REQUESTED;
                    // egyúttal töröljük _STOPPABLE flaget, ha van
                }
                case REFRESH_REQUESTED, START_REQUESTED, RESTART_REQUESTED -> {
                }
                case REFRESHING_SELF_BEFORE_CHILDREN,
                     REFRESHING_SELF_AFTER_CHILDREN,
                     REFRESHING_CHILDREN_AFTER_NO_SELF,
                     REFRESHING_CHILDREN_AFTER_NO_SELF_BUT_SELF_REQUESTED_IN_DESCENDANTS,
                     REFRESHING_CHILDREN_AFTER_SELF,
                     REFRESHING_CHILDREN_SECOND ->
                    // TODO ilyenkor lehet hogy jobb lenne e1.parent meg a többit revertelni,
                    //      mert így inkonzisztens állapotban maradunk
                        throw new RuntimeException();
            }
        } else {
            assert e1.elementState != ElementState.INITIAL && e1.elementState != ElementState.STOPPED;

            // töröljük _STOPPABLE flaget
            switch (e1.elementState) {
                case IDLE_STOPPABLE -> e1.elementState = ElementState.IDLE;
                case REFRESH_REQUESTED_STOPPABLE -> e1.elementState = ElementState.REFRESH_REQUESTED;
            }

            // itt szándékosan nem töröljük parentInterestedUpValuest,
            // mert ha az első instantiate után a másodikban egy UpValueWrapperben van ez a widget,
            // akkor még subscribeolva kell maradnunk az első instantiate után kiolvasott UpValuera.
            // TODO ha két instantiate között megváltozik a widget inputja úgy, hogy a saját maga által előállított
            //      upvaluek is megváltoznak, akkor azt nem kéne hagynunk

            // mert IV-k megváltozhattak. vagy modelt átírhatták.
            e1.requestRefresh();
        }
    }

    void inactivate() {
        assert parent != null : "can't inactivate because already detached: " + this;
        if (!parent.children.remove(this))
            throw new RuntimeException();
        parent = null;
        directAncestorUpValues = null;
        parentInterestedUpValues.clear();
        elementState = ElementState.STOPPED;
        observableHelper.pause();

        activeScope.close();

        while (!children.isEmpty())
            children.getLast().inactivate();

        if (currentState != null) // TODO lehet olyan, hogy currentState == null?
            currentState.onResumeCalled = false;
        closeUntilNextRebuildScope();
        if (currentState != null)
            currentState.closeUntilPauseScope();
    }

    // beleveszi directAncestorEDs tartalmát is.
    // ez nem használható, ha ez az Element egy RootElement
    <U extends SubstitutedWidget> @NonNull U lookupImpl(Class<U> type, boolean noEnsureFresh, boolean optional) {
        Element e = this;

        while (e != null) {
            // ha e == this, akkor ezért nem kell néznünk:
            // - ha invalidateParentIfUpValuesChangedből vagyunk hívva: parentInterestedUpValuesban valuek azok,
            //   amelyek nem directAncestorUpValuesban vannak
            // - ha doLookupból vagyunk hívva: már megnézte directAncestorUpValues tartalmát, ezért felesleges újra
            //   megnézni
            if (e != this) {
                U t = findInUpValueList(type, e.directAncestorUpValues);
                if (t != null) return t;
            }

            if (noEnsureFresh) {
                assert e == this || e.elementState == ElementState.IDLE; // IDLE_STOPPABLE sem lehet

                e.verifyParentOfDelegateIsThis();
            } else {
                // Ez a refresh hívás nem tudom, mire kellett eredetileg (WidgetInstantiation.lookup úgyis
                // ensureFreshel, és annak a delegate láncon is végig kéne mennie, ha van refreshelendő dolog).
                // Viszont most (2025-12-08) azért is kell, mert bejött a delegate visszarakásos probléma,
                // ezért ilyenkor muszáj refresht hívni.
                switch (e.elementState) {
                    // itt jó volt hogy volt REFRESH_REQUESTED_FOR_CHILDREN is.
                    // lehet hogy majd vissza kéne rakni
                    case IDLE -> {
                        if (e.delegateParentMismatch())
                            ObserverHolder.withoutObserver(e::refresh);
                    }
                    case START_REQUESTED, RESTART_REQUESTED, REFRESH_REQUESTED -> {
                        ObserverHolder.withoutObserver(e::refresh);
                    }
                    default -> throw new IllegalStateException("can't ensureFresh while in "
                            + e.elementState + " state: " + e + "\nRefresh stack: \n" + e.refreshStackToString(Map.of()));
                }

                e.verifyParentOfDelegateIsThis();
            }

            if (e.delegate == null)
                e = null;
            else {
                if (e.delegate.element == null) {
                    U t = findInUpValueList(type, e.delegate.upValues);
                    if (t != null) return t;
                    break;
                } else
                    e = e.delegate.element;
            }
        }

        if (optional)
            return null;

        // TODO exception típus
        throw new RuntimeException(type.getName() + " not found in delegate chain of " + this + "\n" +
                "Delegate chain after lookup: \n" + debug_delegateChain());
    }

    private String debug_delegateChain() {
        StringBuilder sb = new StringBuilder();

        Element e = this;
        while (true) {
            for (SubstitutedWidget u : e.directAncestorUpValues)
                sb.append("- up value: ").append(u).append("\n");
            if (e.directIVs != null)
                e.directIVs.forEach((ivType, ivValue) -> {
                    // ivType.getSimpleName nem mindig működik TeaVM esetén
                    sb.append("- inherited value: ").
                            append(ivValue == null ? "<null value>" : ivValue.getClass().getName()).
                            append("\n");
                });
            if (e.debug_getCurrentWidget() != null)
                sb.append("- widget: ").append(e.debug_getCurrentWidget()).append("\n");
            sb.append("- element (").append(e.elementState).append("): ").append(e).append("\n");
            if (e.delegate == null)
                break;
            else if (e.delegate.element == null) {
                for (SubstitutedWidget u : e.delegate.upValues)
                    sb.append("- up value: ").append(u).append("\n");
                break;
            } else {
                e = e.delegate.element;
            }
        }
        return sb.toString();
    }

    private boolean delegateParentMismatch() {
        // ezt is belerakjuk ElementState-be? IDLE-nek egy változata elvileg.
        return delegate != null && delegate.element != null && delegate.element.parent != this;
    }

    static <U extends SubstitutedWidget> @Nullable U findInUpValueList(Class<U> type, List<? extends SubstitutedWidget> upValues) {
        for (SubstitutedWidget ed : upValues) {
            if (type.isInstance(ed))
                return type.cast(ed);
        }
        return null;
    }

    void ensureActive() {
        if (parent == null)
            throw new IllegalStateException("not active: " + this);
    }

    /**
     * ez az első {@code build()} előtt állítódik nemnullra, és {@code @OnStop} előtt záródik be
     *
     * @throws IllegalStateException ha nincs a fában van ez az elem
     */
    // TODO ha be van zárva, akkor lehet hogy inkább nullt kéne visszaadni
    // TODO lehet hogy inkább egy olyat kéne visszaadni, ami nem változik és egy Element példány
    //      esetén ugyanaz marad mindig?
    //      de akkor mit kéne csinálni egy inaktív állapotban visszaadott Scopenak, ha meghívjuk rajta az onClose-t?
    // TODO lehetne ez scope helyett activeScope (mert van lent refreshScope is)
    final Scope scope() {
        if (activeScope == null || activeScope.isClosed())
            throw new IllegalStateException(elementState.toString());
        return activeScope;
    }

    Scope untilWidgetStateNextRebuild(Widget requestor) throws IllegalStateException {
        if (currentState.stateWidget != requestor)
            // elvileg nem lehetséges ilyen
            throw new RuntimeException("WE2 uNR");

        if (untilWidgetStateNextRebuild == null) {
            this.untilWidgetStateNextRebuild = new SimpleScope(Scope.global());
        }
        return untilWidgetStateNextRebuild;
    }

    Scope untilWidgetStatePause(Widget requestor) throws IllegalStateException {
        if (currentState.stateWidget != requestor)
            // elvileg nem lehetséges ilyen
            throw new RuntimeException("WE2 uU");

        return currentState.untilPause();
    }

    private void closeUntilNextRebuildScope() {
        if (untilWidgetStateNextRebuild != null) {
            SimpleScope s = untilWidgetStateNextRebuild;
            untilWidgetStateNextRebuild = null;
            s.close();
        }
    }

    boolean isRefreshingSelfOrDescendants() {
        return elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN ||
                elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN ||
                elementState == ElementState.REFRESHING_CHILDREN_AFTER_SELF ||
                elementState == ElementState.REFRESHING_CHILDREN_AFTER_NO_SELF ||
                elementState == ElementState.REFRESHING_CHILDREN_AFTER_NO_SELF_BUT_SELF_REQUESTED_IN_DESCENDANTS ||
                elementState == ElementState.REFRESHING_CHILDREN_SECOND;
    }

    WidgetAccessor<?> accessor(Widget requestedBy) {
        if (requestedBy != currentState.stateWidget)
            throw new IllegalStateException("WE a");
        return currentState.accessor;
    }

    boolean isActive() {
        return activeScope != null && !activeScope.isClosed();
    }

    String refreshStackToString(Map<Element, String> marks) {
        Element root = this;
        while (root.parent != null)
            root = root.parent;

        List<Object /* Element|Integer */> q = new LinkedList<>();
        q.add(root);
        q.add(1);

        StringBuilder sb = new StringBuilder(); // TeaVMben nincs StringJoiner

        int depth = 0;
        while (q.size() > 1) {
            switch (q.removeFirst()) {
                case Element e -> {
                    int pos = sb.length();
                    String arrowHyphens = "-".repeat(Math.max(5,
                            marks.values().stream().mapToInt(String::length).max().orElse(0) + 1));
                    if (marks.containsKey(e))
                        sb.append(marks.get(e)).
                                append("-".repeat(Math.max(0, arrowHyphens.length() - marks.get(e).length()))).
                                append("> ");
                    else
                        sb.append(e == this ? arrowHyphens + "> " : " ".repeat(arrowHyphens.length()) + "  ");

                    sb.append(depth).append(". ");
                    int prefixLength = sb.length() - pos;
                    if (e.getClass() == Element.class)
                        sb.append(e.elementState).
                                append(e.parent != null ? ", #" + e.parent.children.indexOf(e) : "");
                    else
                        sb.append(e.getClass().getSimpleName()).
                                append(e.parent != null ? " #" + e.parent.children.indexOf(e) : "").
                                append(" (").append(e.elementState).append(")");
                    sb.append(": ").
                            append(e.toString().replace("\n", "\n" + " ".repeat(prefixLength))).
                            append("\n");
                    for (Element child : e.children) {
                        // valójában azt akarjuk nézni hogy elementState REFRESHING_SELF vagy REFRESHING_CHILDREN-e,
                        // de inkább így nézzük, mert így a hibás állapotúakat (pl. STOPPED, INITIAL) is kiírjuk
                        if (child.elementState != ElementState.IDLE &&
                                child.elementState != ElementState.IDLE_STOPPABLE &&
                                child.elementState != ElementState.REFRESH_REQUESTED &&
                                child.elementState != ElementState.REFRESH_REQUESTED_STOPPABLE &&
                                child.elementState != ElementState.START_REQUESTED &&
                                child.elementState != ElementState.RESTART_REQUESTED)
                            q.add(child);
                    }
                }
                case Integer i -> {
                    depth = i;
                    q.add(depth + 1);
                }
                default -> throw new RuntimeException("should not reach here (OHRS)");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    String debug_ancestors() {
        StringBuilder sb = new StringBuilder();
        for (Element e = this; e != null; e = e.parent) {
            sb.insert(0, e.getClass().getSimpleName() + "@" + Integer.toHexString(e.hashCode()) +
                    (e.parent == null ? "" : " #" + e.parent.children.indexOf(e)) + ", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    Widget debug_getCurrentWidget() {
        return currentState == null ? null : currentState.stateWidget;
    }

    @Override
    public String toString() {
        return super.toString() + (currentState == null ? " (no current widget state) model widget = " + nextWidget :
                currentState.stateWidget == null ? " (no current widget) model widget = " + nextWidget :
                        ": current state widget = " + currentState.stateWidget);
    }

    // https://www.figma.com/board/QddkKq9pxYkYFlGwu0FUtk/ElementState?t=yVIO5zyAmVum82xP-6

    /**
     * aktív állapotnak nevezzük INITIAL-on és STOPPED-en kívül az összes többit.
     * <p>
     * Régi leírás állapotokról, majd aktualizálni kell: Egy Element akkor és csak akkor aktív ha olyan element fában
     * van, ami éppen megjelenődik egy megjelenítő eszközön. Azaz ilyenkor ismertek az olyan felülről biztosított
     * értékek, mint a {@link WidgetResolver}, Ticker, FocusRoot, Surface. Például a fontméretét sem ismeri ha nem aktív
     * éppen.
     */
    enum ElementState {

        INITIAL,

        /**
         * Similar to {@link #REFRESH_REQUESTED}, but means first refresh after attached to the tree.
         */
        START_REQUESTED,

        /**
         * Ide kerülhetünk, ha megváltozott egy observable (beleértve @Model mezőt is) vagy egy IV vagy egy leszármazott
         * is kérte a frissítést.
         */
        REFRESH_REQUESTED,

        /**
         * Ugyanaz mint {@link #REFRESH_REQUESTED}, de ha nem fog bekerülni valamelyik elem childjeibe még ebben a
         * frame-ben, akkor le lesz állítva.
         */
        REFRESH_REQUESTED_STOPPABLE,

        /**
         * Ilyenkor lehet {@link Element#instantiate(Slot, Widget, RefreshID, List) Element.instantiate}-et meghívni (és
         * {@linkplain #REFRESHING_SELF_AFTER_CHILDREN}-ben).
         */
        REFRESHING_SELF_BEFORE_CHILDREN,

        /**
         * A leszármazottait frissítjük, miután volt {@link #REFRESHING_SELF_BEFORE_CHILDREN}. Ilyenkor már nem lehet
         * újabb self refresht kérni.
         */
        REFRESHING_CHILDREN_AFTER_SELF,

        /**
         * A leszármazottait frissítjük, de előtte nem volt {@link #REFRESHING_SELF_BEFORE_CHILDREN}. Ilyenkor még lehet
         * self refresht kérni.
         */
        REFRESHING_CHILDREN_AFTER_NO_SELF,

        /**
         * A leszármazottait frissítjük, de előtte nem volt {@link #REFRESHING_SELF_BEFORE_CHILDREN}, viszont most a
         * leszármazottak frissítése közben self refresht. Ilyenkor lehet self további refresht kérni, de nem történik
         * semmi.
         */
        REFRESHING_CHILDREN_AFTER_NO_SELF_BUT_SELF_REQUESTED_IN_DESCENDANTS,

        /**
         * Ilyenkor lehet {@link Element#instantiate(Slot, Widget, RefreshID, List) Element.instantiate}-et meghívni (és
         * {@linkplain #REFRESHING_SELF_BEFORE_CHILDREN}-ben).
         */
        REFRESHING_SELF_AFTER_CHILDREN,

        /**
         * Egyszer már volt {@link #REFRESHING_CHILDREN_AFTER_NO_SELF}, de kértek self refresht, ezért lett még egy
         * {@link #REFRESHING_SELF_AFTER_CHILDREN self refresh}, majd ez. Ilyenkor már nem lehet self refresht kérni.
         */
        REFRESHING_CHILDREN_SECOND,

        /**
         * Nincs igényelve/"időzítve" frissítés.
         */
        IDLE,

        /**
         * Ugyanaz mint {@linkplain #IDLE}, de ha nem fog bekerülni ebben a frame-ben valamelyik Element childjeibe,
         * akkor le lesz állítva.
         */
        IDLE_STOPPABLE,

        STOPPED,

        /**
         * Ez ugyanaz, mint {@link #START_REQUESTED}, de újraengedélyeződnek az ehhez az Elementhez tartozó observerek.
         */
        RESTART_REQUESTED,
    }

    static class InheritedValueHolder {

        Object value;
        final Set<IVUsage> usage = EnumSet.noneOf(IVUsage.class);

        // TODO USED_BY_* nincs törölve sose

        enum IVUsage {
            PARENT_PROVIDED, USED_BY_SELF, USED_BY_CHILDREN
        }

        @Override
        public String toString() {
            return "IVH " + usage + ": " + value;
        }
    }

    enum IVNotProvided {
        IV_NOT_PROVIDED
    }

    static class TraceRefresh {

        private static final Logger logger = LoggerFactory.getLogger(TraceRefresh.class);
        static final ThreadLocal<TraceRefresh> TL = ThreadLocal.withInitial(TraceRefresh::new);

        int depth;

        void print(String s) {
            logger.info("  ".repeat(depth) + s);
        }

        void begin(String s) {
            print(s);
            depth++;
        }

        void end(String s) {
            depth--;
            print(s);
        }
    }

    static final class RefreshID {
    }
}
