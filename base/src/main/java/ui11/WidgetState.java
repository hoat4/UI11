package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.RefreshStack.IVValueWrapper;
import ui11.ResolutionRequest.ResolutionRequestCollection;
import ui11.observable.*;
import ui11.observable.Observable;

import java.util.*;
import java.util.function.Supplier;

/**
 * An instantiation of a {@linkplain Widget} at a particular location in the tree.
 * <p>
 * Widgets describe how to configure a subtree but the same widget can be used to configure multiple subtrees
 * simultaneously.  An WidgetState represents the use of a widget to configure a specific location in the tree. Over time,
 * the widget associated with a given element can change, for example, if the parent widget rebuilds and creates a new
 * widget for this location.
 * <p>
 * WidgetStates form a tree. Many of them have a single child, which is provided by the {@link Widget#build()} method.
 */
final class WidgetState<W extends Widget> implements ObserverCollection {

    static final int FLAG_NEEDS_INIT = 1;
    static final int FLAG_NEEDS_REBUILD = 2;
    static final int FLAG_DESCENDANT_NEEDS_REFRESH = 4;
    static final int FLAG_ACTIVE = 8;
    static final int FLAG_IN_INACTIVATION_QUEUE = 16;
    /**
     * Megtiltja a refresh során a descendantok skippelését, hogy meghívódjon minden gyerekén a
     * {@link #registerParentAndPushIVs(WidgetInstantiation, RefreshStack)}.
     */
    static final int FLAG_HAS_STOLEN_CHILDREN = 32;

    /**
     * refresh során ideiglenesen van beállítva, de lehet hogy úgy marad
     */
    static final int FLAG_USAGE_CHECK = 32;

    // flags mezőnek érvénytelen értékek
    static final int DISPOSED = -1;
    static final int NOT_YET_CREATED = -2;

    /**
     * maszk debug célra, hogy lehessen látni, hogy lefutott-e már egy widget refresh-e
     */
    static final int NEEDS_REFRESH = FLAG_NEEDS_INIT | FLAG_NEEDS_REBUILD | FLAG_HAS_STOLEN_CHILDREN;

    final WidgetTree tree;

    @SuppressWarnings("UnusedAssignment")
    int flags = NOT_YET_CREATED;

    W modelWidget;
    W alsoLockedModelWidget;

    /**
     * ennek az értéke csak state role-ú widget lehet
     */
    W stateWidget;

    final @NonNull WidgetAccessor<W> accessor;

    // TODO ezt majd használatba kéne venni (@Inject mezők megváltozásának ellenőrzéséhez)
    Object[] injectedFieldContents;

    private SimpleScope untilPause;
    private SimpleScope untilNextRebuild;

    /**
     * Ha egyszerre vagyunk egy {@link ResolutionRequestWidget}-ben és a finishernek egy descendantjában, akkor előbbi
     * ennek a listának az első eleme, az utóbbi pedig a második eleme.
     */
    final List<WidgetInstantiation> parents = new ArrayList<>();

    /**
     * Ezt csak ezen WidgetState refreshjekor módosítjuk
     * <p>
     * Ha tömb, akkor nem lehet 0 elemű.
     * <p>
     * Lehetne intrusive linked list is, nem tudom hogy melyik a jobb.
     */
    /* null | WidgetInstantiation | WidgetInstantiation[] */ Object children;

    /**
     * @see WidgetTree#beganRefreshID
     */
    long refreshedAt;
    private Object observed; // ObservableBase[] vagy Set
    private long[] stateAtPause;

    IVCollector<?>[] ivCollectors;

    /**
     * Azon IV-k vannak benne, amit ezen widget ancestorai providolnak és ezen widget descendantjai fogyasztanak.
     * Arra van használva, hogy átugorjuk a descendantokat (más feltételekkel együtt, pl.
     * {@link #FLAG_DESCENDANT_NEEDS_REFRESH}).
     * Amit ez a widget provideol, azok azért nincsenek benne, mert rebuild után úgyis lesz refresh a childokon.
     * <p>
     * Akkor lesz nem-nullra beállítva az értéke, amikor bekerül a refresh stackba az első child.
     * Ha nincs child, akkor null lesz.
     * Ha átugorjuk a descendatokat, akkor nem változtatjuk az értékét.
     * Pop esetén kiolvassuk a tartalmát és a szülőhoz ugyanezen mezőjéhez hozzáfűzzük.
     * Továbbá amikor descendant-of-finisherből olvassunk ki egy IV-t, akkor az forrás és a fogyasztó közötti
     * mindegyik widgethez hozzáadjuk.
     */
    Map<Class<?>, Object> descendantsInterestedIVs;

    private ResolutionRequestCollection computedReqs;

    @SuppressWarnings("unchecked")
    WidgetState(W modelWidget, WidgetTree tree) {
        this.tree = tree;

        modelWidget = otherIfSwapped(modelWidget);

        this.modelWidget = modelWidget;

        // ez lehet hogy InvalidWidgetDefinitionException fog dobni
        this.accessor = (WidgetAccessor<W>) modelWidget.accessor();
        assert accessor.clazz() == modelWidget.getClass();

        this.stateWidget = (W) modelWidget.makeCloneToBeStateRole(this);
        assert accessor.clazz() == stateWidget.getClass();

        accessor.checkStateEmptyAndPrepareState(stateWidget, this, modelWidget);

        if (modelWidget.lpModelData != null)
            // ez a végére van hagyva azért, hogyha fent exception történik, akkor ne jegyezzünk be usage-et
            modelWidget.lpModelData.addUsage(this);

        flags = FLAG_NEEDS_INIT | FLAG_NEEDS_REBUILD;
    }

    private @NonNull W otherIfSwapped(W modelWidget) {
        if (modelWidget.lpModelData != null && modelWidget.lpModelData.replacement != null) {
            // ugyanazt tartalmazza, csak a listenerproxy-k mutatnak máshova
            @SuppressWarnings("unchecked")
            W other = (W) modelWidget.lpModelData.replacement;
            return other;
        } else
            return modelWidget;
    }

    W effectiveModel() {
        return otherIfSwapped(modelWidget);
    }

    ChangeModelResult tryChangeModel(Widget newModelRaw) {
        if (newModelRaw.getClass() != accessor.clazz())
            return ChangeModelResult.NEEDS_NEW_STATE;

        @SuppressWarnings("unchecked") W newModel = (W) newModelRaw;

        return switch (accessor.areInputFieldsChanged(otherIfSwapped(this.modelWidget), newModel)) {
            case NOT_NEEDS_UPDATE -> ChangeModelResult.MODEL_IS_SAME_AS_BEFORE;
            case NEEDS_LISTENER_PROXY_BACKPROPAGATION -> {
                W oldModel = this.modelWidget;
                oldModel.lpModelData.removeUsage(this);
                if (alsoLockedModelWidget != null) {
                    alsoLockedModelWidget.lpModelData.removeUsage(this);
                    alsoLockedModelWidget = null;
                }

                // ez az oldModel.removeUsage után történjen meg, mert lehet hogy
                // newModel.replacement = oldModel volt, ami a removeUsage által szűnt meg
                newModel = otherIfSwapped(newModel);

                oldModel.lpModelData.addUsage(this);

                if (oldModel == newModel) {
                    // "revertelni" kellett a régebbi modelre
                    yield ChangeModelResult.MODEL_IS_SAME_AS_BEFORE;
                }

                newModel.lpModelData.addUsage(this);
                if (oldModel.lpModelData.trySwapWith(newModel, this)) {
                    alsoLockedModelWidget = newModel;
                    yield ChangeModelResult.MODEL_IS_SAME_AS_BEFORE;
                } else {
                    changeModel(newModel);
                    oldModel.lpModelData.removeUsage(this);
                    yield ChangeModelResult.MODEL_CHANGED;
                }
            }
            case NEEDS_UPDATE -> {
                newModel = otherIfSwapped(newModel);

                if (newModel.lpModelData != null)
                    newModel.lpModelData.addUsage(this);
                W oldModel = this.modelWidget;
                changeModel(newModel);
                if (oldModel.lpModelData != null)
                    oldModel.lpModelData.removeUsage(this);
                if (alsoLockedModelWidget != null) {
                    alsoLockedModelWidget.lpModelData.removeUsage(this);
                    alsoLockedModelWidget = null;
                }
                yield ChangeModelResult.MODEL_CHANGED;
            }
        };
    }

    enum ChangeModelResult {
        MODEL_IS_SAME_AS_BEFORE, MODEL_CHANGED, NEEDS_NEW_STATE
    }

    private void changeModel(W model) {
        W prev = this.stateWidget;
        prev.disposeFromStateRole(this);

        this.modelWidget = model;

        try {
            @SuppressWarnings("unchecked")
            W newStateWidget = (W) model.makeCloneToBeStateRole(this);
            this.stateWidget = newStateWidget;

            accessor.transferState(prev, this.stateWidget);
        } catch (RuntimeException | Error e) {
            this.stateWidget = null; // félkész state widgetet ne használjunk, mert további bonyodalmakhoz vezet
            throw e;
        }
    }

    Widget decorateChild(Widget content) {
        return accessor.decorate(stateWidget, content);
    }

    public Scope untilPause() {
        if (untilPause == null)
            untilPause = new SimpleScope(Scope.global());
        return untilPause;
    }

    public Scope untilNextRebuild() {
        if (untilNextRebuild == null)
            untilNextRebuild = new SimpleScope(Scope.global());
        return untilNextRebuild;
    }

    void dispose() {
        inactivate();

        stateWidget.disposeFromStateRole(this);
        stateWidget = null; // nem lényeges, csak hogy könnyebben kiderüljön ha valami nem stimmel

        if (modelWidget.lpModelData != null)
            modelWidget.lpModelData.removeUsage(this);
        if (alsoLockedModelWidget != null) {
            alsoLockedModelWidget.lpModelData.removeUsage(this);
            alsoLockedModelWidget = null;
        }

        flags = DISPOSED;
    }

    // reaktiválásra nincs itt külön függvény, hanem a refresh folyamat részeként történik meg
    void inactivate() {
        System.out.println("inactivate " + stateWidget);
        if (!hasFlag(FLAG_ACTIVE))
            throw new IllegalStateException("already inactive");
        assert parents.isEmpty();
        removeFlag(FLAG_ACTIVE);

        // reaktiváláskor úgyis az egész részfán végigmegyünk
        removeFlagIfPresent(FLAG_DESCENDANT_NEEDS_REFRESH);

        computedReqs = null;

        pauseObservables();
        closeUntilPauseScope();
        closeUntilNextRebuildScope();

        switch (children) {
            case null -> {
                // nop
            }
            case WidgetInstantiation child -> child.child().removeParent(this);
            default -> {
                for (WidgetInstantiation child : (WidgetInstantiation[]) children)
                    child.child().removeParent(this);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void pauseObservables() {
        if (observed instanceof Set<?>) {
            Set<ObservableBase> s = (Set<ObservableBase>) observed;
            stateAtPause = new long[s.size()];
            int i = 0;
            for (ObservableBase obs : s) {
                stateAtPause[i] = obs.invalidationCount;
                obs.removeObserver(this, -1);
            }
        } else if (observed != null) {
            ObservableBase[] arr = (ObservableBase[]) observed;
            stateAtPause = new long[arr.length];
            for (int i = 0; i < arr.length; i++) {
                ObservableBase obs = arr[i];
                if (obs == null)
                    continue;
                stateAtPause[i] = obs.invalidationCount;
                obs.removeObserver(this, -1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    boolean resumeObservables() {
        if (observed == null)
            return false;

        boolean needsRebuild = false;
        if (observed instanceof Set<?>) {
            Set<ObservableBase> s = (Set<ObservableBase>) observed;
            int i = 0;
            for (ObservableBase obs : s) {
                needsRebuild |= obs.invalidationCount != stateAtPause[i];
                obs.addObserver(this);
                i++;
            }
        } else {
            ObservableBase[] arr = (ObservableBase[]) observed;
            stateAtPause = new long[arr.length];
            for (int i = 0; i < arr.length; i++) {
                ObservableBase obs = arr[i];
                if (obs == null)
                    continue;
                needsRebuild |= obs.invalidationCount != stateAtPause[i];
                obs.addObserver(this);
            }
        }

        stateAtPause = null;
        return needsRebuild;
    }

    /**
     * Ezt csak akkor lehet meghívni, ha a parent {@linkplain #NEEDS_REFRESH refreshelve lett már}
     */
    void registerParentAndPushIVs(@NonNull WidgetInstantiation wi,
                                                                    RefreshStack refreshStack) {
        assert wi.child() == this;

        WidgetState<?> newParent = wi.parent();
        if (newParent != null && newParent.hasFlag(NEEDS_REFRESH))
            // mert akkor nem lehet iv consumereket bejegyezni
            throw new IllegalArgumentException("Can't add parent which is not fresh");

        int parentIndex = 0;
        for (; parentIndex < parents.size(); parentIndex++) {
            if (parents.get(parentIndex).parent() == newParent) {
                parents.set(parentIndex, wi);
                break;
            }
            if (isDescendantOfFinisherOf(parents.get(parentIndex).parent(), newParent))
                continue;

            // i-től parents.size()-ig cserélni kell a parenteket kezdve cserélni
            List<WidgetInstantiation> parentsToBeRemoved = parents.subList(parentIndex, parents.size());
            for (WidgetInstantiation p : parentsToBeRemoved)
                // ha az n-edik invalidate sikeres lesz, akkor az n+1-edik is sikeres lesz,
                // ezért nem kell visszacsinálni, ha valamelyik exceptiont dob
                if (p.parent() != null)
                    p.parent().invalidate(FLAG_HAS_STOLEN_CHILDREN, () ->
                            "steal " + this + " from " + parentsToBeRemoved + " by " + newParent);
            // a clear az invalidateek után legyen, hogy csak akkor történjen, ha nem dob exceptiont
            parentsToBeRemoved.clear();
            assert parentIndex == parents.size();
        }

        if (parents.size() == parentIndex) {
            assert parents.stream().noneMatch(wi2 -> wi2.parent() == this);
            parents.add(wi);

            if (hasFlag(FLAG_IN_INACTIVATION_QUEUE))
                tree.removeFromInactivationQueue(this);
        }

        assert parents.get(parentIndex).parent() == wi.parent();

        Map<Class<?>, IVValueWrapper> newIVs = new HashMap<>();
        wi.directIVs().forEach((type, val) -> {
            assert type != ResolutionRequestCollection.class && type != ResolutionRequest.class;
            newIVs.put(type, new IVValueWrapper(val, wi, false));
        });
        if (parentIndex < parents.size() - 1) {
            assert wi.parent() != null;
            Map<Class<?>, IVWithOrigin> fromBottom = ivsUntilFinisherOf(wi.parent(), this);
            fromBottom.forEach((type, val) -> {
                assert type != ResolutionRequestCollection.class && type != ResolutionRequest.class;
                newIVs.putIfAbsent(type, new IVValueWrapper(
                        val.value, val.origin, true));
            });
        }
        refreshStack.pushIVs(this, newIVs);

        List<ResolutionRequest<?>> reqs = new ArrayList<>();
        for (int i = 0; i < parents.size(); i++) {
            WidgetInstantiation parent = parents.get(i);
            ResolutionRequest<?> req = parent.directReq();
            if (req == null)
                assert i == parents.size() - 1;
            else
                reqs.add(req);
            // ezekre nem kell feliratkozni, mert a directIV/directReq megváltozásából úgyis következik a refresh
        }

        if (parents.size() - 1 == parentIndex) {
            ResolutionRequestCollection inheritedReqs = refreshStack.inheritedReqs();
            if (inheritedReqs == null) {
                assert parents.size() == 1 && parents.getFirst() == wi; // hogy origin egyértelmű legyen
                assert reqs.size() == 1;
            } else {
                // ha legalsó parentnek van directReq-ja, akkor az overrideolja az öröklötteket
                if (wi.directReq() == null)
                    reqs.addAll(inheritedReqs.requests.values());
            }
        } else {
            throw new RuntimeException("TODO");
        }

        ResolutionRequestCollection reqColl = ResolutionRequestCollection.of(reqs);
        refreshStack.setComputedReqs(this, reqColl);
    }

    /**
     * ha már nem a parentünk a megadott widget, akkor nem csinál semmit
     */
    void removeParent(WidgetState<?> parent) {
        for (int i = 0; i < parents.size(); i++) {
            if (parents.get(i).parent() == parent) {
                parents.subList(i, parents.size()).clear();
                if (i == 0 && hasFlag(FLAG_ACTIVE) && !hasFlag(FLAG_IN_INACTIVATION_QUEUE))
                    tree.addToInactivationQueue(this);
                break;
            }
        }
    }

    /**
     * azért nullablek, mert a null jelenti a "gyökér parentjét" is.
     *
     * @return true akkor, ha a felmenője b-nek (ha a kettő ugyanaz, akkor az nem számít felmenőnek)
     */
    static boolean isDescendantOfFinisherOf(@Nullable WidgetState<?> a, @Nullable WidgetState<?> b) {
        if (a == null || b == null)
            // ha a == null, akkor azért nem lehetséges, mert az ál-gyökér sosem egy RequestResolutionWidget.
            // ha b == null, akkor azért nem lehetséges, mert akkor legfeljebb az a lehet a b-nek descendantja.
            return false;
        if (!(a.stateWidget instanceof ResolutionRequestWidget rrw))
            return false;
        a = rrw.finisher();
        if (a == null)
            return false;
        for (b = b.parents.getLast().parent(); b != null; b = b.parents.getLast().parent())
            if (a == b)
                return true;
        return false;
    }

    static boolean isDescendantOfOrSame(WidgetState<?> a, WidgetState<?> b) {
        for (; b != null; b = b.parents.getLast().parent())
            if (a == b)
                return true;
        return false;
    }

    /**
     *
     * @return nem {@code null}, ha {@code base} egy RRW, van finisherje és annak {@code b} egy descendantja
     */
    static Map<Class<?>, IVWithOrigin> ivsUntilFinisherOf(
            @NonNull WidgetState<?> base, @NonNull WidgetState<?> b) {
        if (!(base.stateWidget instanceof ResolutionRequestWidget rrw))
            throw new RuntimeException("not RRW");
        WidgetState<?> finisher = rrw.finisher();
        if (finisher == null)
            throw new RuntimeException("finisher not populated");
        Map<Class<?>, IVWithOrigin> ivs = new HashMap<>();
        do {
            assert b != null : finisher + " not found in ancestors";
            WidgetInstantiation wi = b.parents.getLast();
            wi.directIVs().forEach((type, val) -> ivs.putIfAbsent(type, new IVWithOrigin(val, wi)));
            b = wi.parent();
        } while (finisher != b);
        // TODO lehet hogy ezt két lépésben kéne, és
        //      ha nem descendant, akkor nem is építeni fel a mapet
        return ivs;
    }

    record IVWithOrigin(@Nullable Object value, @NonNull WidgetInstantiation origin) {
    }

    void closeUntilPauseScope() {
        if (untilPause != null) {
            SimpleScope s = untilPause;
            untilPause = null;
            s.close();
        }
    }

    void closeUntilNextRebuildScope() {
        if (untilNextRebuild != null) {
            SimpleScope s = untilNextRebuild;
            untilNextRebuild = null;
            s.close();
        }
    }

    void copyIVValuesToFields() {
        accessor.copyIVValuesToFields(stateWidget);
    }

    boolean hasChildren() {
        return children != null;
    }

    /**
     * @return {@code null}, ha i nagyobb mint a childek száma
     */
    @Nullable WidgetInstantiation child(int i) {
        return switch (children) {
            case WidgetInstantiation w -> i == 0 ? w : null;
            case WidgetInstantiation[] array -> i < array.length ? array[i] : null;
            case null -> null;
            default -> throw new RuntimeException("unknown content in children");
        };
    }

    void addFlagIfNotPresent(int flag) {
        if (flags < 0)
            throw new IllegalStateException();
        flags |= flag;
    }

    void addFlag(int flag) {
        if (flags < 0 || (flags & flag) != 0)
            throw new IllegalStateException();
        flags |= flag;
    }

    void removeFlag(int flag) {
        if (flags < 0 || (flags & flag) != flag)
            throw new IllegalStateException();
        flags &= ~flag;
    }

    boolean removeFlagIfPresent(int flag) {
        // hasFlag csinál DISPOSED ellenőrzést is
        boolean result = hasFlag(flag);
        flags &= ~flag;
        return result;
    }

    /**
     * ha kombinációt adunk meg neki, akkor azt ellenőrzi, hogy bármelyik
     * flag a megadottak közül van-e benne
     */
    boolean hasFlag(int flag) {
        if (flags < 0)
            throw new IllegalStateException("Widget is in " + switch (flags) {
                case DISPOSED -> "disposed";
                case NOT_YET_CREATED -> "not yet created";
                default -> Integer.toString(flags);
            } + " state: " + this);
        return (flags & flag) != 0;
    }

    @Override
    public void invalidate(Supplier<String> debugMessageSupplier) {
        invalidate(FLAG_NEEDS_REBUILD, debugMessageSupplier);
        removeObservers();
    }

    void invalidate(int invalidationFlag, Supplier<String> debugMessageSupplier) {
        if (!hasFlag(FLAG_ACTIVE))
            throw new IllegalStateException("not active, can't invalidate");
        if (hasFlag(invalidationFlag))
            return;
        if (refreshedAt > tree.finishedRefreshID) {
            // régi kódban logger.error volt. mi legyen?
            // valamint ott refresh stack is ki volt írva
            // TODO legalább widgeteket írjuk ki (most WidgetState.toString inheritelt Objectből)
            throw new RuntimeException("Can't invalidate widget" +
                    (debugMessageSupplier == null ? "" : " after " + debugMessageSupplier.get()) +
                    ", because it has been already " +
                    "rebuilt in the current refresh cycle: " + this);
        }

        addFlag(invalidationFlag);

        for (WidgetState<?> ancestor = parents.getLast().parent();
             ancestor != null; ancestor = ancestor.parents.getLast().parent()) {
            if (ancestor.hasFlag(FLAG_DESCENDANT_NEEDS_REFRESH)) {
                assert tree.isRefreshScheduled();
                return;
            } else
                ancestor.addFlag(FLAG_DESCENDANT_NEEDS_REFRESH);
        }

        tree.scheduleRefresh();
    }

    void removeObservers() {
        if (observed == null)
            return;
        if (observed.getClass() == ObservableBase[].class) {
            ObservableBase[] a = (ObservableBase[]) observed;
            for (int i = 0; i < a.length; i++) {
                ObservableBase o = a[i];
                if (o != null) {
                    o.removeObserver(this, 1);
                    a[i] = null;
                }
            }
        } else {
            @SuppressWarnings("unchecked")
            Set<ObservableBase> s = (Set<ObservableBase>) observed;
            s.forEach(o -> o.removeObserver(this, 1));
            s.clear();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void subscribedTo(ObservableBase newO) {
        ObservableBase[] arr;
        if (observed == null)
            observed = arr = new ObservableBase[5];
        else if (observed.getClass() == ObservableBase[].class)
            // instanceof Set<?> set volt korábban, de az lassabb
            arr = (ObservableBase[]) observed;
        else {
            ((Set<ObservableBase>) observed).add(newO);
            return;
        }

        for (int i = 0; i < 5; i++) {
            ObservableBase o = arr[i];
            if (o == newO)
                return;
            if (o == null) {
                for (int j = i + 1; j < 5; j++) {
                    o = arr[j];
                    if (o == newO)
                        return;
                }
                arr[i] = newO;
                return;
            }
        }

        Set<Object> s = Collections.newSetFromMap(new IdentityHashMap<>());
        s.add(arr[0]);
        s.add(arr[1]);
        s.add(arr[2]);
        s.add(arr[3]);
        s.add(arr[4]);
        s.add(newO);
        observed = s;
    }

    /**
     * @see #descendantsInterestedIVs
     */
    void addDescendantInterestedIV(Class<?> ivType, Object val) {
        assert descendantsInterestedIVs != null;
        assert !descendantsInterestedIVs.containsKey(ivType) ||
                descendantsInterestedIVs.get(ivType) == val : ivType + ", " + val + ", " + descendantsInterestedIVs;
        descendantsInterestedIVs.putIfAbsent(ivType, val);
    }

    boolean retrieveIVValues() {
        boolean changed = false;
        for (IVCollector<?> collector : ivCollectors)
            changed |= collector.retrieveValue();
        return changed;
    }

    // ha előrehaladottabb állapotban lesz a resolution rendszer, akkor lehetne szűrni,
    // hogy csak azokat a részfákat refresheljük, amiket érdekelnek az adott típusú PeerCreationRequestek
    boolean compareAndSetComputedReqs(@NonNull ResolutionRequestCollection newReqs) {
        if (this.computedReqs == null) {
            this.computedReqs = newReqs;
            return false;
        }
        if (this.computedReqs.equals(newReqs))
            return false;
        this.computedReqs = newReqs;
        return true;
    }

    static abstract class InheritedPropBase<T> {

        final MutableObservable<T> value = MutableObservable.ofNullable();

        final boolean optional;
        final String fieldDebugName;
        final IVCollector<T> ivCollector;

        protected InheritedPropBase(@NonNull IVCollector<T> ivCollector,
                                    boolean optional,
                                    @NonNull String fieldDebugName) {
            Objects.requireNonNull(ivCollector);
            Objects.requireNonNull(fieldDebugName);
            this.ivCollector = ivCollector;
            this.optional = optional;
            this.fieldDebugName = fieldDebugName;
        }

        @Nullable
        T retrieveValue() {
            T val = ivCollector.currentValue(optional, fieldDebugName);
            if (val == null && !optional)
                throw new RuntimeException("internal error, IV has no value (2) but non optional: " +
                        this + ", " + ivCollector.widgetState);
            return val;
        }

        T get() {
            T t = value.get();
            if (t == null && !optional)
                throw new RuntimeException("internal error, IV has no value (1) but non optional: " +
                        this + ", " + ivCollector.widgetState);
            return t;
        }

        void update() {
            final T val = retrieveValue();
            value.set(val);
        }
    }

    static final class InheritedPropObservable<T> extends InheritedPropBase<T> implements Observable<T> {

        public InheritedPropObservable(IVCollector<T> ivCollector, boolean optional,
                                       String fieldDebugName) {
            super(ivCollector, optional, fieldDebugName);
        }

        @Override
        public T get() {
            return super.get();
        }

        @Override
        public String toString() {
            return "InheritedPropObservable{" +
                    "value=" + value +
                    ", optional=" + optional +
                    ", ivCollector=" + ivCollector +
                    ", of " + ivCollector.widgetState + "}";
        }
    }

    // ha ennek a konstruktornak a signaturejét megváltoztatjuk, akkor
    // változtassuk meg WidgetAccessorTeaVMPluginben a hivatkozást és a getVariable-t is
    // (nem fog hibát jelezni ha nem tesszük, csak rejtélyes JS hibák fognak megjelenni)
    static class IVCollector<T> {

        private static final Object IV_NOT_PROVIDED = new Object();

        final WidgetState<?> widgetState;
        final Class<T> type;

        private Object value;

        IVCollector(WidgetState<?> widgetState, Class<T> type) {
            this.widgetState = widgetState;
            this.type = type;
        }

        boolean retrieveValue() {
            Object newValue;

            if (PeerCreationRequest.class.isAssignableFrom(type) && PeerCreationRequest.class != type) {
                newValue = widgetState.tree.getAndSubscribeIVForCurrentWidget(
                        widgetState, ResolutionRequestCollection.class, IV_NOT_PROVIDED);
                if (newValue != IV_NOT_PROVIDED) {
                    ResolutionRequestCollection coll =
                            (ResolutionRequestCollection) newValue;
                    newValue = findResolutionRequest(coll);
                }
            } else
                newValue = widgetState.tree.getAndSubscribeIVForCurrentWidget(
                        widgetState, type, IV_NOT_PROVIDED);

            if (!Objects.equals(value, newValue)) {
                this.value = newValue;
                return true;
            } else
                return false;
        }

        private @NonNull Object findResolutionRequest(ResolutionRequestCollection coll) {
            Object newValue;
            List<ResolutionRequest<?>> reqs = coll.requests.values().stream().
                    filter(r -> type.isInstance(r.requestData)).
                    toList();
            newValue = switch (reqs.size()) {
                case 0 -> IV_NOT_PROVIDED;
                case 1 -> type.cast(reqs.getFirst().requestData);
                default -> {
                    // TODO
                    throw new RuntimeException("Multiple requests corresponds to " + type.getName());
                }
            };
            return newValue;
        }

        /**
         * csak retrieveValue vagy resume után szabad meghívni, ha a widget előtte
         * FLAG_NEEDS_IV_RETRIEVE vagy !FLAG_ACTIVE állapotban volt
         */
        T currentValue(boolean optional, String fieldOrParameterName) {
            if (!widgetState.hasFlag(FLAG_ACTIVE))
                throw new IllegalStateException("can't retrieve value, widget state is inactive: " + this + ", " + widgetState);
            if (value == null)
                throw new RuntimeException("should not reach here (IVC.cV)");

            if (value == IV_NOT_PROVIDED)
                if (optional)
                    return null;
                else {
                    // TODO hibaüzenet esetén field nevet hogy kéne kiírni? (InjectionFieldInfo::debugName)
                    throw new RuntimeException("inherited value for " +
                            type.getName() + " (used by " + fieldOrParameterName + ")" +
                            " not supplied for " + widgetState /* TODO + "; Ancestors: " */);
                }
            else
                return type.cast(value);
        }
    }
}
