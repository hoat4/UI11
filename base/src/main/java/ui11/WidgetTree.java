package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.observable.ObservableBase;
import ui11.observable.ObserverHolder;
import ui11.provide.DynamicProvider;
import ui11.provide.Provider;

import java.util.*;
import java.util.concurrent.Executor;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

/**
 * The container for all content in a widget tree. This class is not usually used by applications, instead it is used by
 * platform-specific windowing system and rendering implementation modules.
 */
public final class WidgetTree {

    private static final boolean TRACE_REFRESH = false;

    static final Object IV_NOT_PROVIDED = new Object();

    // TODO ha nincs slf4j impl, akkor ez NOP logger lesz
    private static final Logger logger = LoggerFactory.getLogger(WidgetTree.class);

    private final Widget rootWidget;
    private final Executor executor;

    private Edge root;
    private boolean refreshScheduled;

    private RefreshStack refreshStack;

    /**
     * ha egy Slothoz új WidgetStateet rendelünk, akkor szedjük ki ebből a queueból
     */
    private final SequencedSet<WidgetState<?>> inactivationQueue = new LinkedHashSet<>();

    private SequencedMap<WidgetState<?>, List<LaterValidationCheck>> laterValidationChecks;
    private long laterValidationCheckGenerator;

    long beganRefreshID, finishedRefreshID = -1;

    final ResolverRegistry resolverRegistry;

    private WidgetTree(Widget rootWidget, Executor executor) {
        this.rootWidget = rootWidget;
        this.executor = executor;

        ResolverRegistry resolverRegistry = new ResolverRegistry();
        // kell cacheelni, mert folyton ServiceLoadert hívogatni lassú.
        // TeaVM-ben nincs ServiceLoader.stream(), ezért muszáj iterátorral.
        // (TeaVM-ben egyébként valójában nem lenne lassú folyton ServiceLoaderen iterálni)
        // mert thread context class loadert néz, ezért egyelőre WidgetTreenként cacheljük.
        // TODO mi legyen vele? lehessen megadni create-nek ClassLoadert? vagy hajtsuk végre az
        //      első refresht az executorban és akkor az már úgyis a megfelelő threadben történik?
        for (ResolverProvider d : ServiceLoader.load(ResolverProvider.class))
            d.configure(resolverRegistry); // TODO exceptionök
        this.resolverRegistry = resolverRegistry;
    }

    public static WidgetTree create(Widget root, Executor executor) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(executor);

        WidgetTree widgetTree = new WidgetTree(root, executor);
        widgetTree.refresh();
        return widgetTree;
    }

    void refresh() {
        try {
            beganRefreshID++;
            refreshScheduled = false;

            if (TRACE_REFRESH)
                traceRefresh("Begin refresh " + beganRefreshID);

            if (refreshStack != null)
                throw new RuntimeException("refresh stack already exists");

            laterValidationChecks = new LinkedHashMap<>();

            ObserverHolder observerHolder = ObserverHolder.current();
            observerHolder.ensureNoCurrentObserver();

            root = findOrCreateWidgetState(rootWidget, null, root, Set.of());
            refreshStack = new RefreshStack(root);

            while (!refreshStack.isEmpty()) {
                WidgetState<?> w = refreshStack.peekWidget();

                if (w.tree != this)
                    throw new RuntimeException("widget from other tree");
                if (w.flags < 0)
                    throw new RuntimeException("disposed or invalid widget state appeared");
                // TODO FLAG_IN_INACTIVATION_QUEUE-et ellenőrizzük (vagy ha nem itt, akkor findNextToRefreshben)

                w.laterValidationCheckActual = w.laterValidationCheckLast;

                // TODO ha lesz a widget feldolgozása közben egy exception,
                //      akkor nem kéne visszaállítani a flageket?
                boolean wasActive = w.hasFlag(WidgetState.FLAG_ACTIVE);
                boolean needsRebuild = w.removeFlagIfPresent(WidgetState.FLAG_NEEDS_REBUILD);
                boolean needsDescendantRefresh = w.removeFlagIfPresent(WidgetState.FLAG_DESCENDANT_NEEDS_REFRESH);
                boolean hasStolenChildren = w.removeFlagIfPresent(WidgetState.FLAG_HAS_STOLEN_CHILDREN);

                assert !w.hasFlag(WidgetState.FLAG_NEEDS_INIT) || needsRebuild;

                w.registerParent(refreshStack.peekWidgetInstantiation());

                w.refreshedAt = beganRefreshID;

                if (!wasActive) {
                    w.addFlag(WidgetState.FLAG_ACTIVE);

                    // TODO ezt nem inkább a closeUntilNextRebuildScope után kéne?
                    needsRebuild |= w.resumeObservables();
                }

                needsRebuild |= w.retrieveIVValues();

                if (w.stateWidget instanceof PeerRequestor)
                    // TODO ez már nem elavult?
                    needsRebuild = true; // ivsFromSecondaryLocation lehetséges megváltozása miatt

                if (w.stateWidget instanceof InheritedValueMerger)
                    // IVM nem @Inject mezőben kéri el az IV-t, hanem getIVForCurrentWidget-et,
                    // ezért retrieveIVValues nem tudja hogy megváltozott-e az utolsó buildhez képest
                    needsRebuild = true;

                if (needsRebuild) {
                    // ezt azért hoztuk ide fel, hogy ha pl. copyIVValuesToFields átállít egy Observable-t,
                    // akkor ne legyen FLAG_NEEDS_REBUILD beállítva, mert már ki lett olvasva és falsera állítva,
                    // és true-ra ragadna ha ilyen későn állítgatnánk
                    w.removeObservers(null);

                    w.closeUntilNextRebuildScope();
                    w.copyIVValuesToFields();
                }
                if (w.hasFlag(WidgetState.FLAG_NEEDS_INIT)) {
                    w.stateWidget.initState();
                    w.removeFlag(WidgetState.FLAG_NEEDS_INIT);
                }
                if (!wasActive) {
                    w.stateWidget.onResume();
                    needsDescendantRefresh = true;
                }
                needsDescendantRefresh |= hasStolenChildren;

                refreshStack.setDebugValuesOfCurrentWidget(needsRebuild);

                if (!needsRebuild) {
                    if (!needsDescendantRefresh && w.descendantsInterestedIVs != null &&
                            !ivsMatch(w, w.descendantsInterestedIVs))
                        needsDescendantRefresh = true;

                    if (TRACE_REFRESH)
                        traceRefresh(w + ": no rebuild, " +
                                (needsDescendantRefresh ? "but enter children" : "skip children" +
                                        "; descendant interested IVs: " + w.descendantsInterestedIVs));

                    findNextToRefresh(!needsDescendantRefresh);
                    continue;
                }

                if (TRACE_REFRESH)
                    traceRefresh(w + ": rebuild");

                if (w.stateWidget instanceof PeerRequestor rrw) {
                    Edge[] prevChildren = (Edge[]) w.children;
                    w.beforeBuild();
                    Edge[] newChildren = rrw.buildMulti(w, prevChildren);
                    w.removeObsoleteIDs();
                    Objects.requireNonNull(newChildren);
                    w.children = newChildren;

                    if (wasActive)
                        removeFromParentListFromRemovedChildren(prevChildren, newChildren, w);
                } else {
                    Edge prevChild = (Edge) w.children;

                    // removeObservers feljebb lett víve

                    Widget content;
                    try {
                        observerHolder.setObserver(w);
                        w.beforeBuild();
                        try {
                            content = w.stateWidget.build();
                        } finally {
                            observerHolder.clearObserver(w);
                            w.removeObsoleteIDs();
                        }

                        if (content == null && !(w.stateWidget instanceof ChainEnd))
                            throw new NullPointerException(w.stateWidget.getClass().getSimpleName() +
                                    ".build() returned null on " + w);

                        content = w.decorateChild(content);
                    } catch (Throwable e) {
                        if (refreshStack.containsErrorWidget()) {
                            logger.error("Failed to build content for " + w + ", but " +
                                    "an error widget is already on refresh stack so error is fatal", e);
                            if (e instanceof Error e2)
                                throw e2;
                            // sneaky throw (vagy más JVM nyelvek) miatt az e lehet checked exception is
                            throw asRuntimeException(e);
                        } else {
                            logger.error("Failed to build content for " + w, e);
                            content = new ErrorWidget(e);
                        }
                    }

                    Edge newChild;
                    if (content != null)
                        newChild = findOrCreateWidgetState(content, w, prevChild, null);
                    else
                        newChild = null;
                    w.children = newChild;

                    if (wasActive && prevChild != null && (newChild == null || newChild.child() != prevChild.child()))
                        prevChild.child().removeParent(w);
                }

                findNextToRefresh(false);

                // nem szabad pl. FLAG_NEEDS_REBUILD-nek maradnia.
                // következő widgeteknél ki fog derülni, de nehezen értelmezhető hibaüzenetekkel.
                // kapjuk el minél előbb, hogy könnyebben lehessen debugolni.
                // akkor volt ilyen, amikor pl. InheritedPropBase beállított Observable-t új értékre.
                assert !w.hasFlag(WidgetState.NEEDS_REFRESH) : w;
            }

            while (!inactivationQueue.isEmpty()) {
                WidgetState<?> w = inactivationQueue.removeFirst();
                w.removeFlag(WidgetState.FLAG_IN_INACTIVATION_QUEUE);
                w.inactivate();
            }

            laterValidationChecks.forEach((w, checks) -> {
                for (LaterValidationCheck check : checks) {
                    if (w.laterValidationCheckActual < check.required) {
                        // TODO valami értelmesebb kéne a logüzenetbe a w.toString elejére
                        //      ahelyett hogy "ui11.WidgetState@3dc45957"
                        logger.error("Missed to refresh " + w + ": " + check.msgIfNotAchieved);
                        w.removeFlag(WidgetState.FLAG_NEEDS_REBUILD);
                        w.restoreObservables(check.toBeRestored);
                    }
                }
            });
        } catch (Throwable e) {
            // executorok gyakran elnyelik, azért naplózzuk
            logger.error("Refresh failed", e);
            throw e;
        } finally {
            finishedRefreshID = beganRefreshID;
            refreshStack = null;
            laterValidationChecks = null;
            if (TRACE_REFRESH) {
                traceRefresh("Finished refresh " + beganRefreshID);
            }
        }
    }

    private RuntimeException asRuntimeException(Throwable e) {
        return e instanceof RuntimeException e2 ? e2 : new RuntimeException(e);
    }

    private void traceRefresh(String s) {
        // azért stderr, mert "Missed to refresh" szöveg logger.error, ami stderrre megy általában,
        // és a sorrend összekavarodna
        int indent = refreshStack == null ? 0 : refreshStack.depth();
        System.err.println("[TRACE_REFRESH] " + "  ".repeat(indent) + s);
    }

    private boolean ivsMatch(WidgetState<?> w, Map<Class<?>, Object> descendantsInterestedIVs) {
        for (Map.Entry<Class<?>, Object> entry : descendantsInterestedIVs.entrySet()) {
            if (!Objects.equals(getIVForCurrentWidget(w, entry.getKey(), false), entry.getValue()))
                return false;
        }
        return true;
    }

    /**
     * Az új childeket hozzáadjuk a parentjükhöz, az eltűnteket töröljük a parentjükből, valamint az összes esetén
     * berakjuk a refresh queueba, amelyeknek kell refresh.
     */
    private static void removeFromParentListFromRemovedChildren(Edge @NonNull [] prevChildren,
                                                                Edge @Nullable [] newChildren,
                                                                WidgetState<?> parent) {
        for (Edge prevChild : prevChildren)
            prevChild.child().removeFlagIfPresent(WidgetState.FLAG_USAGE_CHECK);
        if (newChildren != null)
            for (Edge newChild : newChildren)
                newChild.child().addFlagIfNotPresent(WidgetState.FLAG_USAGE_CHECK);
        for (Edge prevChild : prevChildren) {
            if (prevChild.child().hasFlag(WidgetState.FLAG_USAGE_CHECK))
                prevChild.child().removeFlag(WidgetState.FLAG_USAGE_CHECK);
            else
                prevChild.child().removeParent(parent);
        }
    }

    private void findNextToRefresh(boolean skipDescendantsOfCurrent) {
        WidgetState<?> w = refreshStack.peekWidget();

        if (!skipDescendantsOfCurrent && w.hasChildren()) {
            // enter children
            w.descendantsInterestedIVs = new HashMap<>();
            assert w.hasChildren();
            Edge child = w.child(0);
            assert child != null;
            refreshStack.push(w, 0, child);
        } else {
            if (!skipDescendantsOfCurrent) {
                // fenti if feltétele miatt ekkor w-nek nincs gyereke.
                // ha nincs gyereke, akkor viszont elavult információkat tartalmaz a descendantsInterestedIVs map,
                // ha még létezik, ezért töröljük.
                w.descendantsInterestedIVs = null;
            }

            // find next sibling of current or next sibling of ancestor
            while (true) {
                WidgetState<?> peek = refreshStack.peekWidget();
                if (peek.descendantsInterestedIVs == null)
                    assert peek == w;

                RefreshStack.Item current = refreshStack.pop();
                assert (current.parent == null) == refreshStack.isEmpty();
                if (current.parent == null)
                    break;

                Edge nextSibling = current.parent.child(current.childIndex + 1);
                if (nextSibling != null) {
                    refreshStack.push(current.parent, current.childIndex + 1, nextSibling);
                    break;
                }
            }
        }
    }

    /**
     * A létrehozott/megtalált majd esetleg módosított WidgetState-et
     * hozzáadja a parent children listájába, illetve a refreshQueueba is.
     * Ha parent nem null, akkor feltételezi, hogy {@link WidgetState#FLAG_ACTIVE aktív} állapotban van.
     *
     * @param parent ez csak akkor null, ha a root widgetről van szó
     */
    Edge findOrCreateWidgetState(@NonNull Widget widget,
                                 @Nullable WidgetState<?> parent,
                                 @Nullable Edge previous,
                                 @Nullable Set<? extends ResolutionRequest<?>> reqs) {
        Objects.requireNonNull(widget, "widget");
        if (parent != null && !parent.hasFlag(WidgetState.FLAG_ACTIVE))
            throw new IllegalArgumentException("parent not active: " + parent);

        Map<Class<?>, Object> ivs = new HashMap<>();

        if (reqs != null)
            ivs.put(ResolutionRequestCollection.class, new ResolutionRequestCollection(reqs));

        Widget firstWidget = widget;

        WidgetState<?> w = previous == null ? null : previous.child();

        processProxyWidgets:
        while (true) {
            switch (widget) {
                case null -> {
                    throw new NullPointerException("CSB null " + this);
                }
                case Provider<?> p -> {
                    Object val = p.value();

                    // részben azért nem val instanceof Mergeable-t nézünk, hogy null esetén is működjön,
                    // részben pedig hogy findIVProvidesUntil nem a példány típusából, hanem a megadott típusból
                    // dönti el, hogy directIVsből vagy a directAncestorEDs-ből szedje az értékeket.
                    boolean isMergeableType = Provider.Mergeable.class.isAssignableFrom(p.type()) ||
                            p.type() == DynamicProvider.class;
                    if (isMergeableType && firstWidget == widget &&
                            parent != null && parent.stateWidget instanceof InheritedValueMerger<?> ivm &&
                            ivm.returnedProviderShouldNotBeMerged())
                        // TODO this branch is probably non-tested as of now
                        isMergeableType = false;

                    if (isMergeableType) {
                        widget = new InheritedValueMerger<>(p);
                    } else {
                        ivs.put(p.type(), val);
                        widget = p.content();
                    }
                }
                case GlobalKey.GlobalKeyWidget globalKeyWidget -> {
                    w = globalKeyWidget.replacement(this);
                    widget = w.stateWidget;
                }
                default -> {
                    break processProxyWidgets;
                }
            }
        }
        Objects.requireNonNull(widget, "nextWidget");

        if (w == null || w.effectiveModel() != widget) {
            widget.initListenerProxyData();

            WidgetState.ChangeModelResult changeModelResult;
            if (w == null)
                changeModelResult = WidgetState.ChangeModelResult.NEEDS_NEW_STATE;
            else
                changeModelResult = w.tryChangeModel(widget);

            switch (changeModelResult) {
                case NEEDS_NEW_STATE -> {
                    w = new WidgetState<>(widget, this);

                    // TODO mit csináljunk, ha konstruktor exceptiont dob?
                }
                case MODEL_IS_SAME_AS_BEFORE -> {
                    // nop
                }
                case MODEL_CHANGED -> {
                    w.addFlagIfNotPresent(WidgetState.FLAG_NEEDS_REBUILD);
                }
            }
        }

        Edge wi = new Edge(parent, w, ivs, beganRefreshID);
        if (reqs != null)
            for (ResolutionRequest<?> req : reqs)
                req.reqWI = wi;
        return wi;
    }

    // TODO ezt az IV prioritásos dolgot át kéne gondolni alaposabban.
    //      Most az lett a működés, hogy a req coll-t adó widgetig menve a legmagasabb a
    //      seq numberű widget instantiation a prioritás.
    //      De ez tényleg helyes működés, vagy csak arra elég hogy IDTest lefusson?

    /**
     * @param subscribe descendantokba bejegyzi a típus utáni érdeklődést. Ettől önmagában
     *                  még nem fogunk a jövőben mindenképp rebuildelődni, mert ahhoz
     *                  {@linkplain WidgetState#retrieveIVValues()}-nak is true-t kell visszaadnia.
     * @return {@link #IV_NOT_PROVIDED}, ha nincs
     */
    Object getIVForCurrentWidget(WidgetState<?> widgetState, Class<?> type, boolean subscribe) {
        if (!widgetState.hasFlag(WidgetState.FLAG_ACTIVE))
            throw new IllegalStateException("not active");

        Deque<IVStackItem> stack = new LinkedList<>();
        stack.push(new IVStackItem(widgetState, 0));

        List<Edge> visitedEdges = new ArrayList<>();

        try {
            boolean foundAnyValue = false;
            IVCombiner combiner = IVCombiner.forType(type);

            while (!stack.isEmpty()) {
                IVStackItem current = stack.peek();

                // current.w.parents lehet hogy üres (inaktivációs queueban vár),
                // ekkor skippeljük

                if (current.parentIndex == current.parent.parents.size()) {
                    stack.pop();
                    continue;
                }

                Edge edge = current.parent.parents.get(current.parentIndex);
                current.parentIndex++;

                if (edge.ivRetrieval_visited)
                    continue;
                edge.ivRetrieval_visited = true;
                visitedEdges.add(edge);

                Object value = edge.directIVs().getOrDefault(type, IV_NOT_PROVIDED);
                if (value == IV_NOT_PROVIDED) {
                    DynamicProvider dynamicProvider = (DynamicProvider) edge.directIVs().get(DynamicProvider.class);
                    if (dynamicProvider != null) {
                        // TODO kéne valamit csinálni hogy DynamicProvider ne kavarhasson subscribe-olásokkal
                        Object dynamicValue = dynamicProvider.provideOrNull(type);
                        if (dynamicValue != null)
                            value = type.cast(dynamicValue);
                    }
                }

                if (value == IV_NOT_PROVIDED) {
                    assert (edge == root) == (edge.parent() == null);
                    if (edge != root)
                        stack.push(new IVStackItem(edge.parent(), edge.instantiatedAt()));
                    continue;
                }

                if (subscribe) {
                    for (IVStackItem item : stack) {
                        if (item.parent != widgetState)
                            item.parent.descendantsInterestedIVs.put(type, value);
                    }
                }

                foundAnyValue = true;
                if (edge != root) // TODO a rootban nem lehetnek IV-k?
                    // ennek az edge-nek az instantiatedAt-je is lényeges lehet, ezért berakjuk ideiglenesen a stackbe
                    stack.push(new IVStackItem(edge.parent(), edge.instantiatedAt()));
                combiner.add(value, stack);
                if (edge != root)
                    stack.pop();
            }

            return foundAnyValue ? combiner.finish() : IV_NOT_PROVIDED;
        } finally {
            for (Edge e : visitedEdges)
                e.ivRetrieval_visited = false;
        }
    }

    static class IVStackItem {
        final WidgetState<?> parent;
        final long edgeCreationTime;
        int parentIndex;

        IVStackItem(WidgetState<?> edgeParent, long edgeCreationTime) {
            Objects.requireNonNull(edgeParent);
            this.parent = edgeParent;
            this.edgeCreationTime = edgeCreationTime;
        }
    }

    void addToInactivationQueue(@NonNull WidgetState<?> w) {
        if (w.tree != this)
            throw new IllegalArgumentException("WT aTIQ");

        w.addFlag(WidgetState.FLAG_IN_INACTIVATION_QUEUE);
        inactivationQueue.addLast(w);
    }

    void removeFromInactivationQueue(@NonNull WidgetState<?> w) {
        if (w.tree != this)
            throw new IllegalArgumentException("WT rTIQ");

        w.removeFlag(WidgetState.FLAG_IN_INACTIVATION_QUEUE);
        inactivationQueue.remove(w);
    }

    void submitForLaterValidationCountCheck(WidgetState<?> w, String msg, List<ObservableBase> toBeRestored) {
        if (beganRefreshID == finishedRefreshID)
            throw new IllegalStateException();

        // TODO mi legyen ha inactivation queue feldolgozása közben vagyunk?

        long expected = ++laterValidationCheckGenerator;
        w.laterValidationCheckLast = expected;
        laterValidationChecks.computeIfAbsent(w, __ -> new ArrayList<>()).
                add(new LaterValidationCheck(expected, msg, toBeRestored));
    }

    boolean isRefreshScheduled() {
        return refreshScheduled;
    }

    void scheduleRefresh() {
        if (refreshScheduled)
            return;
        executor.execute(this::refresh);
        refreshScheduled = true;
    }

    String refreshStackToString(boolean showInternal) {
        return refreshStack.stackToString(showInternal);
    }

    // TODO milyen API kéne ide?
    //      pl. valami dispose-szerűség, illetve rootot lookupolni?

    // TODO ez 2023-09-08-án (r22244) került be, aztán Element4 óta, azaz
    //      kb. 2024-07-02 (r23141) óta nincs használva.
    //      de valszeg jó lenne valami perf stat API.
    static class UIPerformanceStats {
        public int prefSizeCalculations_withoutFixedCrossSize;
        public int prefSizeCalculations_withFixedCrossSize;
        public int elementCreations;
        public int peerCreations;
        public boolean layoutActive;
        public long layoutTime;
    }

    private static record LaterValidationCheck(long required, String msgIfNotAchieved,
                                               List<ObservableBase> toBeRestored) {
    }

    static final class ChainEnd extends Widget {

        @Override
        protected Widget build() {
            return null;
        }
    }
}
