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

    private WidgetInstantiation root;
    private boolean refreshScheduled;

    private RefreshStack refreshStack;

    /**
     * ha egy Slothoz új WidgetStateet rendelünk, akkor szedjük ki ebből a queueból
     */
    private final SequencedSet<WidgetState<?>> inactivationQueue = new LinkedHashSet<>();

    private SequencedMap<WidgetState<?>, List<LaterValidationCheck>> laterValidationChecks;
    private long laterValidationCheckGenerator;

    long beganRefreshID, finishedRefreshID = -1;

    private WidgetTree(Widget rootWidget, Executor executor) {
        this.rootWidget = rootWidget;
        this.executor = executor;
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
                // azért stderr, mert "Missed to refresh" szöveg logger.error, ami stderrre megy általában,
                // és a sorrend összekavarodna
                System.err.println("[TRACE_REFRESH] Begin refresh " + beganRefreshID);

            if (refreshStack != null)
                throw new RuntimeException("refresh stack already exists");

            laterValidationChecks = new LinkedHashMap<>();

            ObserverHolder observerHolder = ObserverHolder.current();
            observerHolder.ensureNoCurrentObserver();

            root = findOrCreateWidgetState(rootWidget, null, root, Set.of(), false);
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
                    needsRebuild = true; // ivsFromSecondaryLocation lehetséges megváltozása miatt

                if (needsRebuild) {
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
                        System.err.println("[TRACE_REFRESH] " + w + ": no rebuild, " +
                                (needsDescendantRefresh ? "but enter children" : "skip children"));

                    findNextToRefresh(!needsDescendantRefresh);
                    continue;
                }

                if (TRACE_REFRESH)
                    System.err.println("[TRACE_REFRESH] " + w + ": rebuild");

                if (w.stateWidget instanceof PeerRequestor rrw) {
                    WidgetInstantiation[] prevChildren = (WidgetInstantiation[]) w.children;
                    WidgetInstantiation[] newChildren = rrw.buildMulti(w, prevChildren);
                    Objects.requireNonNull(newChildren);
                    w.children = newChildren;

                    if (wasActive)
                        removeFromParentListFromRemovedChildren(prevChildren, newChildren, w);
                } else {
                    WidgetInstantiation prevChild = (WidgetInstantiation) w.children;

                    w.removeObservers(null);

                    observerHolder.setObserver(w);
                    Widget content;
                    try {
                        content = w.stateWidget.build();
                        content = w.decorateChild(content);
                    } finally {
                        observerHolder.clearObserver(w);
                    }

                    if (content == null && !(w.stateWidget instanceof ChainEnd))
                        throw new NullPointerException(w.stateWidget.getClass().getSimpleName() +
                                ".build() returned null on " + w);

                    WidgetInstantiation newChild;
                    if (content != null)
                        newChild = findOrCreateWidgetState(content, w, prevChild, null, false);
                    else
                        newChild = null;
                    w.children = newChild;

                    if (wasActive && prevChild != null && (newChild == null || newChild.child() != prevChild.child()))
                        prevChild.child().removeParent(w);
                }

                findNextToRefresh(false);
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
            if (TRACE_REFRESH)
                System.err.println("[TRACE_REFRESH] Finished refresh " + beganRefreshID);
            finishedRefreshID = beganRefreshID;
            refreshStack = null;
            laterValidationChecks = null;
        }
    }

    private boolean ivsMatch(WidgetState<?> w, Map<Class<?>, Object> descendantsInterestedIVs) {
        for (Map.Entry<Class<?>, Object> entry : descendantsInterestedIVs.entrySet()) {
            if (!Objects.equals(getIVForCurrentWidget(w, entry.getKey(), false), entry.getValue()))
                return false;
        }
        return true;
    }

    /**
     * Az új childeket hozzáadjuk a parentjükhöz, az eltűnteket töröljük a parentjükből,
     * valamint az összes esetén berakjuk a refresh queueba, amelyeknek kell refresh.
     */
    private static void removeFromParentListFromRemovedChildren(WidgetInstantiation @NonNull [] prevChildren,
                                                                WidgetInstantiation @Nullable [] newChildren,
                                                                WidgetState<?> parent) {
        for (WidgetInstantiation prevChild : prevChildren)
            prevChild.child().removeFlagIfPresent(WidgetState.FLAG_USAGE_CHECK);
        if (newChildren != null)
            for (WidgetInstantiation newChild : newChildren)
                newChild.child().addFlagIfNotPresent(WidgetState.FLAG_USAGE_CHECK);
        for (WidgetInstantiation prevChild : prevChildren) {
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
            WidgetInstantiation child = w.child(0);
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

                WidgetInstantiation nextSibling = current.parent.child(current.childIndex + 1);
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
     * @param parent          ez csak akkor null, ha a root widgetről van szó
     * @param clearParentData ez csak akkor értelmezhető, ha {@code reqs} nem null
     */
    WidgetInstantiation findOrCreateWidgetState(@NonNull Widget widget,
                                                @Nullable WidgetState<?> parent,
                                                @Nullable WidgetInstantiation previous,
                                                @Nullable Set<? extends ResolutionRequest<?>> reqs,
                                                boolean clearParentData) {
        Objects.requireNonNull(widget, "widget");
        if (parent != null && !parent.hasFlag(WidgetState.FLAG_ACTIVE))
            throw new IllegalArgumentException("parent not active: " + parent);

        Slot slot = null;

        Map<Class<?>, Object> ivs = new HashMap<>();

        if (reqs != null) {
            ivs.put(ResolutionRequestCollection.class, new ResolutionRequestCollection(reqs));

            if (clearParentData)
                ivs.put(ParentDataWidget.ParentDataCollection.class, ParentDataWidget.ParentDataCollection.CLEAR);
        }

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
                    final boolean isMergeableType = Provider.Mergeable.class.isAssignableFrom(p.type()) ||
                            p.type() == DynamicProvider.class;

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
                    w = slot.content;
                }
                default -> {
                    break processProxyWidgets;
                }
            }
        }
        Objects.requireNonNull(widget, "nextWidget");

        if (widget instanceof Slot2.SlotWidget slotWidget)
            w = slotWidget.slot.widgetState;

        if (w == null || w.effectiveModel() != widget) {
            widget.initListenerProxyData();

            WidgetState.ChangeModelResult changeModelResult;
            if (w == null)
                changeModelResult = WidgetState.ChangeModelResult.NEEDS_NEW_STATE;
            else
                changeModelResult = w.tryChangeModel(widget);

            switch (changeModelResult) {
                case NEEDS_NEW_STATE -> {
                    // új widget state-et kell létrehozni

                    WidgetState<?> prevState = w;
                    if (prevState != null)
                        prevState.dispose();

                    w = new WidgetState<>(widget, this);

                    if (widget instanceof Slot2.SlotWidget slotWidget) {
                        @SuppressWarnings("unchecked")
                        WidgetState<Slot2.SlotWidget> castedW = (WidgetState<Slot2.SlotWidget>) w;
                        assert prevState == null;
                        slotWidget.slot.widgetState = castedW;
                    } else if (slot != null)
                        // TODO mit csináljunk, ha a slotot többen is használják egyszerre?
                        slot.content = w;

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

        WidgetInstantiation wi = new WidgetInstantiation(parent, w, ivs);
        if (reqs != null)
            for (ResolutionRequest<?> req : reqs)
                req.reqWI = wi;
        return wi;
    }

    /**
     * @return {@link #IV_NOT_PROVIDED}, ha nincs
     */
    Object getIVForCurrentWidget(WidgetState<?> widgetState, Class<?> type, boolean subscribe) {
        if (!widgetState.hasFlag(WidgetState.FLAG_ACTIVE))
            throw new IllegalStateException("not active");

        class Item {
            final WidgetState<?> w;
            final Object value;
            int parentIndex;

            Item(WidgetState<?> w, Object value) {
                Objects.requireNonNull(w);
                this.w = w;
                this.value = value;
            }
        }

        Map<Object, List<Item>> differentValues = new IdentityHashMap<>();

        Deque<Item> stack = new LinkedList<>();
        stack.push(new Item(widgetState, IV_NOT_PROVIDED));

        // TODO ez így exponenciálisan lassul, ha sok elágazás van.
        //      úgy kéne hogy ha egy node-ot már bejártunk és nincs új információ, akkor ne menjünk oda újra.
        while (!stack.isEmpty()) {
            Item current = stack.peek();

            // current.w.parents lehet hogy üres (inaktivációs queueban vár),
            // ekkor skippeljük

            if (current.parentIndex == current.w.parents.size()) {
                stack.pop();
                continue;
            }

            WidgetInstantiation edge = current.w.parents.get(current.parentIndex);
            current.parentIndex++;
            Object value = edge.directIVs().getOrDefault(type, IV_NOT_PROVIDED);

            if (subscribe && value != IV_NOT_PROVIDED &&
                    stack.stream().allMatch(item -> item.value == IV_NOT_PROVIDED)) {
                for (Item item : stack) {
                    if (item.w != widgetState)
                        item.w.descendantsInterestedIVs.put(type, value);
                }
            }

            if (edge.parent() == null) {
                assert edge == root;
                Object visibleValue = IV_NOT_PROVIDED;
                for (Item item : stack.reversed()) {
                    if (item.value != IV_NOT_PROVIDED) {
                        visibleValue = item.value;
                        break;
                    }
                }
                if (visibleValue == IV_NOT_PROVIDED)
                    visibleValue = value;
                if (!differentValues.containsKey(visibleValue))
                    differentValues.put(visibleValue, List.copyOf(stack));
            } else {
                stack.push(new Item(edge.parent(), value));
            }
        }

        assert !differentValues.isEmpty();
        if (differentValues.size() > 1) {
            if (type == ResolutionRequestCollection.class) {
                Set<ResolutionRequestCollection> colls =
                        (Set<ResolutionRequestCollection>) (Set<?>) differentValues.keySet();
                return ResolutionRequestCollection.combine(colls);
            }

            StringBuilder sb = new StringBuilder("Different values for inherited value " + type.getName() + ":");
            for (Map.Entry<Object, List<Item>> e : differentValues.entrySet()) {
                sb.append("\n- ").append(e.getKey());
                sb.append("\n  Found at: ");
                int i = e.getValue().size();
                for (Item item : e.getValue().reversed())
                    sb.append("\n   ").append(--i).append(".: ").append(item.w);
            }
            throw new RuntimeException(sb.toString());
        }

        return differentValues.keySet().iterator().next();
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

    String refreshStackToDebugString() {
        return refreshStack.toDebugString();
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
