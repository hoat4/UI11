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
    private static final boolean TRACE_CLEANUP_UNUSED_IV = false; // CleanupUnusedIVCollectorTest

    static final Object IV_NOT_PROVIDED = new Object();

    // TODO ha nincs slf4j impl, akkor ez NOP logger lesz
    private static final Logger logger = LoggerFactory.getLogger(WidgetTree.class);

    private final Widget rootWidget;
    private final Executor executor;

    private Edge root;
    private boolean refreshScheduled;

    private RefreshStack refreshStack;

    /**
     * if a WidgetState gots a new parent in the safe refresh cycle, then it will be removed from this queue
     */
    private final SequencedSet<WidgetState<?>> disposeQueue = new LinkedHashSet<>();

    private SequencedMap<WidgetState<?>, List<LaterValidationCheck>> laterValidationChecks;
    private List<WidgetState<?>> laterRelativeIDChecks;
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
        for (ResolverProvider provider : ServiceLoader.load(ResolverProvider.class)) {
            ResolverRegistry r = new ResolverRegistry();
            provider.configure(r); // TODO exceptionök
            resolverRegistry.add(r, provider.priority());
        }
        resolverRegistry.sortByPriority();
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
            laterRelativeIDChecks = new ArrayList<>();

            ObserverHolder observerHolder = ObserverHolder.current();
            observerHolder.ensureNoCurrentObserver();

            root = findOrCreateWidgetState(rootWidget, null, root, Set.of());
            refreshStack = new RefreshStack(root);

            while (!refreshStack.isEmpty()) {
                WidgetState<?> w = refreshStack.peekWidget();

                if (w.tree != this)
                    throw new RuntimeException("Widget from other tree: " + w);
                if (w.flags < 0)
                    throw new RuntimeException("Disposed or invalid widget state appeared: " + w);

                w.laterValidationCheckActual = w.laterValidationCheckLast;

                // TODO ha lesz a widget feldolgozása közben egy exception,
                //      akkor nem kéne visszaállítani a flageket?
                boolean needsInit = w.removeFlagIfPresent(WidgetState.FLAG_NEEDS_INIT);
                boolean needsRebuild = w.removeFlagIfPresent(WidgetState.FLAG_NEEDS_REBUILD);
                boolean needsDescendantRefresh = w.removeFlagIfPresent(WidgetState.FLAG_DESCENDANT_NEEDS_REFRESH);
                boolean hasStolenChildren = w.removeFlagIfPresent(WidgetState.FLAG_HAS_STOLEN_CHILDREN);

                boolean wasActive = !needsInit;
                assert !w.hasFlag(WidgetState.FLAG_NEEDS_INIT) || needsRebuild;

                w.registerParent(refreshStack.peekWidgetInstantiation());
                assert !w.hasFlag(WidgetState.FLAG_QUEUED_FOR_DISPOSE);
                w.refreshedAt = beganRefreshID;

                int ivsUpdated = w.retrieveIVValues();
                needsRebuild |= (ivsUpdated & IVCollector.USED_BY_SELF) != 0;

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

                if (needsInit)
                    w.stateWidget.initState(); // TODO what to do if an exception happens in initState?

                needsDescendantRefresh |= hasStolenChildren;

                refreshStack.setDebugValuesOfCurrentWidget(needsRebuild);

                if (!needsRebuild) {
                    needsDescendantRefresh |= (ivsUpdated & IVCollector.USED_BY_DESCENDANT) != 0;

                    if (TRACE_REFRESH)
                        traceRefresh(w + ": no rebuild, " +
                                (needsDescendantRefresh ? "but enter children" : "skip children"));

                    findNextToRefresh(needsDescendantRefresh);
                    continue;
                }

                if (TRACE_REFRESH)
                    traceRefresh(w + ": rebuild");

                if (w.stateWidget instanceof PeerRequestor rrw) {
                    Edge[] prevChildren = (Edge[]) w.children;
                    w.beforeBuild();
                    Edge[] newChildren = rrw.buildMulti(w, prevChildren);
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

                findNextToRefresh(true);

                // nem szabad pl. FLAG_NEEDS_REBUILD-nek maradnia.
                // következő widgeteknél ki fog derülni, de nehezen értelmezhető hibaüzenetekkel.
                // kapjuk el minél előbb, hogy könnyebben lehessen debugolni.
                // akkor volt ilyen, amikor pl. InheritedPropBase beállított Observable-t új értékre.
                assert !w.hasFlag(WidgetState.NEEDS_REFRESH) : w;
            }

            for (WidgetState<?> w : laterRelativeIDChecks) {
                w.removeObsoleteIDs();
            }

            while (!disposeQueue.isEmpty()) {
                WidgetState<?> w = disposeQueue.removeFirst();
                w.removeFlag(WidgetState.FLAG_QUEUED_FOR_DISPOSE);
                w.dispose();
            }

            laterValidationChecks.forEach((w, checks) -> {
                for (LaterValidationCheck check : checks) {
                    if (w.laterValidationCheckActual < check.required) {
                        // TODO valami értelmesebb kéne a logüzenetbe a w.toString elejére
                        //      ahelyett hogy "ui11.Node@3dc45957"
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
            laterRelativeIDChecks = null;
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

    private void findNextToRefresh(boolean enterDescendants) {
        WidgetState<?> w = refreshStack.peekWidget();

        if (enterDescendants)
            w.ivCollectorsByType.values().forEach(c -> c.usage &= ~IVCollector.USED_BY_DESCENDANT);

        if (enterDescendants && w.hasChildren()) {
            // enter children
            Edge child = w.child(0);
            assert child != null;
            refreshStack.push(w, 0, child);
        } else {
            // find next sibling of current or next sibling of ancestor
            while (true) {
                RefreshStack.Item current = refreshStack.pop();

                // cleanup unused IV collectors
                current.widgetInstantiation.child().ivCollectorsByType.values().removeIf(c -> {
                    if (TRACE_CLEANUP_UNUSED_IV && c.usage == 0)
                        System.out.println("Remove " + c + " from " + current.widgetInstantiation.child());
                    return c.usage == 0;
                });

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
     * A létrehozott/megtalált majd esetleg módosított Node-et
     * hozzáadja a parent children listájába, illetve a refreshQueueba is.
     * Ha parent nem null, akkor feltételezi, hogy {@link WidgetState#isActive() aktív} állapotban van.
     *
     * @param parent ez csak akkor null, ha a root widgetről van szó
     */
    Edge findOrCreateWidgetState(@NonNull Widget widget,
                                 @Nullable WidgetState<?> parent,
                                 @Nullable Edge previous,
                                 @Nullable Set<? extends ResolutionRequest<?>> reqs) {
        Objects.requireNonNull(widget, "widget");
        if (parent != null && !parent.isActive())
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

    void addToDisposeQueue(@NonNull WidgetState<?> w) {
        if (w.tree != this)
            throw new IllegalArgumentException("WT aTIQ");

        w.addFlag(WidgetState.FLAG_QUEUED_FOR_DISPOSE);
        disposeQueue.addLast(w);
    }

    void removeFromDisposeQueue(@NonNull WidgetState<?> w) {
        if (w.tree != this)
            throw new IllegalArgumentException("WT rTIQ");

        w.removeFlag(WidgetState.FLAG_QUEUED_FOR_DISPOSE);
        disposeQueue.remove(w);
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

    void submitForLaterRelativeIDCleanup(WidgetState<?> w) {
        if (laterRelativeIDChecks == null)
            throw new IllegalStateException();
        if (w.hasFlag(WidgetState.FLAG_QUEUED_FOR_RELATIVE_ID_CLEANUP))
            return;
        w.addFlag(WidgetState.FLAG_QUEUED_FOR_RELATIVE_ID_CLEANUP);
        laterRelativeIDChecks.add(w);
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
