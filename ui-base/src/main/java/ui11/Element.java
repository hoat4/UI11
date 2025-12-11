package ui11;

import ui11.observable.*;
import ui11.observable.Observable;
import ui11.Element.InheritedValueHolder.IVUsage;
import ui11.provide.DynamicProvider;
import ui11.provide.UpValue;
import ui11.resolution.ErrorWidgetFactory;
import ui11.resolution.WidgetResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

import static java.util.stream.Collectors.joining;

// kéne olyan mechanizmus, amivel childrent/cachedPeereket megőrizhetjük stop/start között

// TODO itt a javadocban Lifecycle annotációk elavultak

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
 * implementációjában meghívjuk a {@link BuildContext#instantiate(Object, Widget)}-ot. Ha az egyik updatekor egy adott
 * elemenetet hozzáadtunk gyerekként, míg a következő updateben már nem, akkor az a gyerek törlődik.
 */
sealed abstract class Element permits RootElement, RSWStateHolder {

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

    @Nonnull final ObservableHelper observableHelper = new ObservableHelper(this);
    @Nonnull
    ElementState elementState = ElementState.INITIAL;
    @Nullable
    Element parent;
    @Nonnull final List<Element> children = new ArrayList<>();

    boolean addedToParentInCurrentRefreshStateOfParent;

    /**
     * Ez akkor és csak akkor nem null, ha parent nem null. Az elemei közt nem szerepel null.
     */
    List<? extends UpValue> directAncestorUpValues;

    /**
     * Ez akkor és csak akkor nem null, ha elementState == ElementState.REFRESHING_SELF
     */
    BuildContext refreshState;

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
            this instanceof RootElement ? null : new HashMap<>();

    // ha ennek a nevét módosítjuk, módosítsuk ListenerProxyGenerator.Element_originalModel_FIELD_NAME-t is
    Object model;

    final Map<Class<? extends UpValue>, Object> parentInterestedUpValues = new HashMap<>(); // ez üres, ha parent == null
    final InvalidationPoint upValuesIP = new InvalidationPoint();

    WidgetResolver vp;

    /**
     * az ebben lévő elemek nem feltétlen childok
     */
    // TODO ennek tartalmát törölni kéne valamikor, mert így mem leak.
    //      nem világos hogy mikor, mert withKeyt/Slotot descendant widgetek is használhatják.
    @Nonnull final Map<Object, Element> cachedPeers = new HashMap<>();

    private SimpleScope activeScope;
    SimpleScope refreshScope;

    private final List<InheritedProp<?>> inheritedProps = new ArrayList<>();

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

            if (!(this instanceof RootElement)) // ha egy IV hiányzik, a delegateet be kéne állítani valami hibaüzenetre
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
                    delegate = registerChild(delegate.element, delegate.key, delegate.upValues, delegate.ivs);
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
                    "Refresh stack: \n" + refreshStackToString(Map.of(delegate.element, "delegate",
                    delegate.element.parent, "parent of delegate")));
    }

    abstract Class<?> modelType();

    /**
     * @return true, ha kell refreshSelf a modelváltozás miatt, különben false
     */
    abstract boolean updateUserVisibleModel();

    // TODO ha ez nem sikerül (pl. nem létezik egyik IV), akkor delegatenek ki kéne azért rakni valamit

    /**
     * @return refresh reason mask
     */
    private int refreshIVs() {
        assert !(this instanceof RootElement);

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
        refreshState = new BuildContext(this, isStartOrRestart, isStart);
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

            refreshState = null;
            for (Element e : children)
                e.addedToParentInCurrentRefreshStateOfParent = false;
        }
        // TODO tisztítsuk majd meg byKeyt
        RootElement context = root();
        for (Element e : children)
            if (e.elementState == ElementState.IDLE_STOPPABLE || e.elementState == ElementState.REFRESH_REQUESTED_STOPPABLE)
                context.submitForDispose(e);
    }

    private RootElement root() {
        Element e = this;
        while (e.parent != null)
            e = e.parent;
        return (RootElement) e;
    }

    // TODO fieldName-et nem kéne bewrappelnie "field '...'" stringbe,
    //      mert így hülyén néz ki több helyen, pl. @Content metódus paraméter esetén,
    //      meg template condition variable esetén is
    // fieldName csak inherited() miatt nullable
    @Nullable
    final <T> T findInheritedValueForInjection(Class<T> type, boolean optional, @Nullable String fieldOrParameterName) {
        Object value = findInheritedValue(type, IVUsage.USED_BY_SELF);
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
            for (Entry<Class<? extends UpValue>, Object> entry : parentInterestedUpValues.entrySet()) {
                Class<? extends UpValue> type = entry.getKey();
                Object val = entry.getValue();
                Object newVal = lookupImpl(type, true, true);
                if (!Objects.equals(val, newVal)) {
                    //System.out.println("invalidate parent because " + type.getSimpleName() + " changed from " + val + " to " + newVal);
                    //System.out.println("Parent: " + parent);
                    //System.out.println("This: " + this);
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
            for (InheritedProp<?> p : inheritedProps)
                p.update();

            Widget content = build();
            if (content == null)
                // el kéne dönteni hogy lehet-e null. ha igen, akkor withKey-ben is kezelni kéne.
                throw new NullPointerException("Element.build() returned null on " + this);
            // TODO ha az előző delegate bewrappelődik egy másik widgetbe, akkor
            //      fölöslegesen refresheljük az előző delegateet, mert már
            //      csak a wrapperben kéne.
            //      lehet hogy a children elejére kéne rakni valahogy (vagy legalább az előző delegate elé).
            delegate = refreshState.instantiate(DelegateSlot.DELEGATE_SLOT, content);
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

        if (t instanceof Error e && !(e instanceof AssertionError))
            throw e;

        try {
            Iterator<ErrorWidgetFactory> sl = ServiceLoader.load(ErrorWidgetFactory.class).iterator();
            if (sl.hasNext()) {
                ErrorWidgetFactory errorWidgetFactory = sl.next();
                Widget widget = errorWidgetFactory.makeDelegateCreationError(t);
                delegateHandle = refreshState.instantiate(DelegateSlot.DELEGATE_SLOT, widget);
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

    private enum DelegateSlot {
        DELEGATE_SLOT
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
    abstract Widget build();

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
     * saját magunkra self refresht kérünk, ancestorokra csak simát
     */
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
                if (e instanceof RootElement rootElement) {
                    rootElement.requestRootRefresh();
                    return;
                } else
                    // nem lehetséges
                    throw new RuntimeException("have no parent, but not " + RootElement.class.getSimpleName() + ": " + e);
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

    @Nonnull
    private IllegalStateException cantRequestRefresh(Element e) {
        return new IllegalStateException("Cannot request refresh of " + this +
                " because " + e + " is in " + e.elementState + ". \n" +
                // nem feltétlen szerepel a refresh stackben a this
                "Ancestors of requested refresh element: " + debug_ancestors() + "\n" +
                "Refresh stack: \n" +
                e.refreshStackToString(Map.of(this, "ORIG")));
    }

    @Nonnull
    WidgetInstantiation registerChild(Element e1, @Nonnull KeyWrapper kw,
                                      List<? extends UpValue> upValues, Map<Class<?>, Object> ivs) {
        assert elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN || elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN;
        if (e1.elementState == ElementState.REFRESHING_SELF_BEFORE_CHILDREN || e1.elementState == ElementState.REFRESHING_SELF_AFTER_CHILDREN)
            throw new IllegalStateException();

        // TODO ha cachedPeersből szedtük ki, akkor felesleges újra putolni
        kw.container.cachedPeers.put(kw.key, e1);

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

            // mert IV-k megváltozhattak. vagy modelt átírhatta trySetModel.
            e1.requestRefresh();
        }

        e1.directAncestorUpValues = upValues;
        e1.directIVs = ivs;
        return new WidgetInstantiation(this, refreshState, e1, upValues, kw, ivs);
    }

    // TODO ha előző @DefaultPeer-rel találta meg,
    //      akkor engedje a trySetModelt akkor is ha nincs modelType

    /**
     * ezt akkor szabad csak meghívni, ha utána meg lesz hívva is a leendő parent registerChild-ja
     */
    final boolean trySetModel(Object model) {
        Objects.requireNonNull(model);

        // TODO ez a komment még érvényes?
        // az isAssignableFrom azért használható, mert ha descendantbe más DefaultPeerCreator megadva,
        // akkor ElementDefReflector hibát jelezne

        Class<?> requiredType = modelType();
        if (requiredType != null && requiredType.isAssignableFrom(model.getClass())) {
            this.model = model;

            // ilyenkor még nem szabad refresht kérni, mert
            // még nem a "végleges" parentünknél vagyunk, csak hamarosan fogunk odakerülni.
            // viszont registerChild amúgy is kér jelenleg mindig refresht, ezért nem baj,
            // ha itt nem kérünk.

            // if (elementState != ElementState.INITIAL && elementState != ElementState.STOPPED)
            //     requestRefresh();
            return true;
        } else
            return false;

        // TODO modelt nullra kéne állítani, amikor már nincs
    }

    final void setModel(Widget widget) {
        if (!trySetModel(widget))
            throw new RuntimeException("trySetModel failed on " + this + " to " + widget);
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
    }

    // beleveszi directAncestorEDs tartalmát is.
    // ez nem használható, ha ez az Element egy RootElement
    @Nonnull
    <U extends UpValue> U lookupImpl(Class<U> type, boolean noEnsureFresh, boolean optional) {
        Element e = this;

        while (e != null) {
            U t = findInUpValueList(type, e.directAncestorUpValues);
            if (t != null) return t;

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
                    t = findInUpValueList(type, e.delegate.upValues);
                    if (t != null) return t;
                    break;
                } else
                    e = e.delegate.element;
            }
        }

        if (optional)
            return null;

        StringBuilder sb = new StringBuilder(type.getName() + " not found in delegate chain of " + this);
        sb.append("\nDelegate chain: \n");

        e = this;
        while (true) {
            for (UpValue u : e.directAncestorUpValues)
                sb.append("- up value: ").append(u).append("\n");
            if (e.model != null)
                sb.append("- widget: ").append(e.model).append("\n");
            sb.append("- element (").append(e.elementState).append("): ").append(e).append("\n");
            if (e.delegate == null)
                break;
            else if (e.delegate.element == null) {
                for (UpValue u : e.directAncestorUpValues)
                    sb.append("- up value: ").append(u).append("\n");
                break;
            } else {
                e = e.delegate.element;
            }
        }

        // TODO exception típus
        throw new RuntimeException(sb.toString());
    }

    private boolean delegateParentMismatch() {
        // ezt is belerakjuk ElementState-be? IDLE-nek egy változata elvileg.
        return delegate != null && delegate.element != null && delegate.element.parent != this;
    }

    static <U extends UpValue> @Nullable U findInUpValueList(Class<U> type, List<? extends UpValue> upValues) {
        for (UpValue ed : upValues) {
            if (type.isInstance(ed))
                return type.cast(ed);
        }
        return null;
    }

    // azért nem egy annotáció, mert ott nem lehet rendesen invalidálni a függőséget a mező olvasásakor
    // (csak approximálni próbáltunk, hogy akkor a @Inherited mezőt tartalmazó widgetet invalidáljuk,
    //  de ez kapásból elbukik, ha pl. egy inner class Element eléri az illető mezőt).
    <T> Observable<T> inherited(Class<T> type, boolean optional) {
        // duplicate-eket szűrni kéne? lehet hogy kiderülhet általa, ha valaki folyton újra meghívja ezt

        if (elementState != ElementState.INITIAL &&
                elementState != ElementState.START_REQUESTED)
            throw new IllegalStateException("too late to add inherited prop " + type.getName() + ", " + this);

        InheritedProp<T> p = new InheritedProp<>(type, optional);
        inheritedProps.add(p);
        return p;
    }

    private class InheritedProp<T> implements Observable<T> {

        private final MutableObservable<T> value = MutableObservable.ofNullable();
        private final Class<T> type;
        private final boolean optional;

        public InheritedProp(Class<T> type, boolean optional) {
            this.type = type;
            this.optional = optional;
        }

        @Override
        public T get() {
            /*
            System.out.println("subscribe to " + type.getSimpleName() + " prop of " +
                    Integer.toHexString(Element.this.hashCode()) + " by " +
                    (ObserverHolder.current().obsC instanceof ObservableHelper h ?
                            Integer.toHexString(h.node.hashCode()) : ObserverHolder.current().obsC));
            if (ObserverHolder.current().obsC instanceof ObservableHelper h) {
                System.out.println("THIS:  " + debug_ancestors());
                System.out.println("OTHER: " + h.node.debug_ancestors());
                System.out.println();
            }
            */

            ensureActive();
            T t = value.get();
            if (t == null && !optional)
                throw new RuntimeException("internal error, IV has no value (1) but non optional: " + this + ", " + Element.this);
            return t;
        }

        void update() {
            T val = findInheritedValueForInjection(type, optional, null);
            if (val == null && !optional)
                throw new RuntimeException("internal error, IV has no value (2) but non optional: " + this + ", " + Element.this);
            value.set(val);
        }

        @Override
        public String toString() {
            return "InheritedProp{" +
                    "value=" + value +
                    ", type=" + type +
                    ", optional=" + optional +
                    ", of " + Element.this + "}";
        }
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
                    sb.append(e.getClass().getSimpleName()).
                            append(e.parent != null ? " #" + e.parent.children.indexOf(e) : "").
                            append(" (").append(e.elementState).append(")").
                            append(": ").append(e.toString().replace("\n", "\n" + " ".repeat(prefixLength))).
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
         * Ekkor a {@linkplain Element#refresh()} viselkedése ugyanaz, mint {@link #REFRESH_REQUESTED} esetén, de
         * {@link BuildContext#isStartOrRestart} {@code true} lesz, ezáltal {@link Widget#onResume()} is meghívódik.
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
         * Ilyenkor lehet {@link BuildContext#instantiate(Object, Widget) Element4.childet} meghívni (és
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
         * Ilyenkor lehet {@link BuildContext#instantiate(Object, Widget) Element4.childet} meghívni (és
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
}
