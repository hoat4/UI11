package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    long beganRefreshID, finishedRefreshID = -1;

    public WidgetTree(Widget rootWidget, Executor executor) {
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

            if (refreshStack != null)
                throw new RuntimeException("refresh stack already exists");

            ObserverHolder observerHolder = ObserverHolder.current();
            observerHolder.ensureNoCurrentObserver();

            ResolutionRequest<SubstitutedWidget> rootReq = new ResolutionRequest<>(
                    null,
                    this,
                    new PeerCreationRequest<>(SubstitutedWidget.class) {
                    },
                    rootWidget);
            root = findOrCreateWidgetState(rootReq.primaryWrapper(), null, root);
            rootReq.setWidgetInstantiation(root);
            refreshStack = new RefreshStack(root);

            while (!refreshStack.isEmpty()) {
                WidgetState<?> w = refreshStack.peekWidget();

                if (w.tree != this)
                    throw new RuntimeException("widget from other tree");
                if (w.flags < 0)
                    throw new RuntimeException("disposed or invalid widget state appeared");
                // TODO FLAG_IN_INACTIVATION_QUEUE-et ellenőrizzük (vagy ha nem itt, akkor findNextToRefreshben)

                // TODO ha lesz a widget feldolgozása közben egy exception,
                //      akkor nem kéne visszaállítani a flageket?
                boolean wasActive = w.hasFlag(WidgetState.FLAG_ACTIVE);
                boolean needsRebuild = w.removeFlagIfPresent(WidgetState.FLAG_NEEDS_REBUILD);
                boolean needsDescendantRefresh = w.removeFlagIfPresent(WidgetState.FLAG_DESCENDANT_NEEDS_REFRESH);
                boolean hasStolenChildren = w.removeFlagIfPresent(WidgetState.FLAG_HAS_STOLEN_CHILDREN);

                assert !w.hasFlag(WidgetState.FLAG_NEEDS_INIT) || needsRebuild;

                w.setParent(refreshStack.peekParent());

                w.refreshedAt = beganRefreshID;

                if (!wasActive) {
                    w.addFlag(WidgetState.FLAG_ACTIVE);

                    // TODO ezt nem inkább a closeUntilNextRebuildScope után kéne?
                    needsRebuild |= w.resumeObservables();
                }

                needsRebuild |= w.retrieveIVValues();

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

                if (!needsRebuild) {
                    if (!needsDescendantRefresh && w.descendantsInterestedIVs != null &&
                            !refreshStack.ivsMatch(w.descendantsInterestedIVs))
                        needsDescendantRefresh = true;

                    findNextToRefresh(!needsDescendantRefresh);
                    continue;
                }

                if (w.stateWidget instanceof ResolutionRequestWidget rrw) {
                    WidgetInstantiation[] prevChildren = (WidgetInstantiation[]) w.children;
                    WidgetInstantiation[] newChildren = rrw.buildMulti(w, prevChildren);
                    Objects.requireNonNull(newChildren);
                    w.children = newChildren;

                    if (wasActive)
                        removeDisappearedChildren(prevChildren, newChildren, w);
                } else {
                    WidgetInstantiation prevChild = (WidgetInstantiation) w.children;

                    w.removeObservers();
                    observerHolder.setObserver(w);
                    Widget content;
                    try {
                        content = w.stateWidget.build();
                        content = w.decorateChild(content);
                    } finally {
                        observerHolder.clearObserver(w);
                    }

                    if (content == null && !(w.stateWidget instanceof SubstitutedWidget))
                        throw new NullPointerException(w.stateWidget.getClass().getSimpleName() +
                                ".build() returned null on " + w);

                    WidgetInstantiation newChild;
                    if (content != null)
                        newChild = findOrCreateWidgetState(content, w, prevChild);
                    else
                        newChild = null;
                    w.children = newChild;

                    if (wasActive && newChild != prevChild && prevChild != null)
                        prevChild.widgetState().removeParent(w);
                }

                w.removeFlag(WidgetState.FLAG_NEEDS_REBUILD);

                findNextToRefresh(false);
            }

            while (!inactivationQueue.isEmpty()) {
                WidgetState<?> w = inactivationQueue.removeFirst();
                w.removeFlag(WidgetState.FLAG_IN_INACTIVATION_QUEUE);
                w.inactivate();
            }
        } catch (Throwable e) {
            // executorok gyakran elnyelik, azért naplózzuk
            logger.error("Refresh failed", e);
            throw e;
        } finally {
            finishedRefreshID = beganRefreshID;
            refreshStack = null;
        }
    }

    /**
     * Az új childeket hozzáadjuk a parentjükhöz, az eltűnteket töröljük a parentjükből,
     * valamint az összes esetén berakjuk a refresh queueba, amelyeknek kell refresh.
     */
    private static void removeDisappearedChildren(WidgetInstantiation[] prevChildren,
                                                  WidgetInstantiation[] newChildren,
                                                  WidgetState<?> parent) {
        for (WidgetInstantiation prevChild : prevChildren)
            prevChild.widgetState().removeFlagIfPresent(WidgetState.FLAG_USAGE_CHECK);
        for (WidgetInstantiation newChild : newChildren)
            newChild.widgetState().addFlagIfNotPresent(WidgetState.FLAG_USAGE_CHECK);
        for (WidgetInstantiation prevChild : prevChildren) {
            if (prevChild.widgetState().hasFlag(WidgetState.FLAG_USAGE_CHECK))
                prevChild.widgetState().removeFlag(WidgetState.FLAG_USAGE_CHECK);
            else
                prevChild.widgetState().removeParent(parent);
        }
    }

    private void findNextToRefresh(boolean skipDescendantsOfCurrent) {
        WidgetState<?> w = refreshStack.peekWidget();
        assert w.descendantsInterestedIVs != null || !skipDescendantsOfCurrent;

        if (!skipDescendantsOfCurrent && w.hasChildren()) {
            // enter children
            w.descendantsInterestedIVs = new HashMap<>();
            assert w.hasChildren();
            WidgetInstantiation child = w.child(0);
            assert child != null;
            refreshStack.push(w, 0, child);
        } else {
            // find next sibling of current or next sibling of ancestor
            while (true) {
                RefreshStack.Item current = refreshStack.pop();
                assert (current.parent() == null) == refreshStack.isEmpty();
                if (current.parent() == null)
                    break;

                // ennek semmi köze a kereséshez, csak pop utáni teendő
                current.child().widgetState().descendantsInterestedIVs.forEach((ivType, val) -> {
                    if (!current.child().directIVs().containsKey(ivType)) {
                        current.parent().addDescendantInterestedIV(ivType, val);
                    }
                });

                WidgetInstantiation nextSibling = current.parent().child(current.childIndex() + 1);
                if (nextSibling != null) {
                    refreshStack.push(current.parent(), current.childIndex() + 1, nextSibling);
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
    WidgetInstantiation findOrCreateWidgetState(@NonNull Widget widget,
                                                @Nullable WidgetState<?> parent,
                                                @Nullable WidgetInstantiation previous) {
        Objects.requireNonNull(widget, "widget");
        if (parent != null && !parent.hasFlag(WidgetState.FLAG_ACTIVE))
            throw new IllegalArgumentException("parent not active: " + parent);

        Slot slot = null;
        Map<Class<?>, Object> ivs = new HashMap<>();
        WidgetState<?> w = previous == null ? null : previous.widgetState();

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
                case ResolutionRequest.Reuse r -> {
                    return r.make(this, refreshStack, ivs);
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
                    // új widget state-et kell létrehozni

                    WidgetState<?> prevState = w;
                    if (prevState != null)
                        prevState.dispose();

                    w = new WidgetState<>(widget, this);

                    if (slot != null)
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

        return new WidgetInstantiation(w, ivs, true);
    }

    Object getAndSubscribeIVForCurrentWidget(WidgetState<?> widgetState, Class<?> type, @NonNull Object ifNotProvided) {
        if (!widgetState.hasFlag(WidgetState.FLAG_ACTIVE))
            throw new IllegalStateException("not active");

        WidgetInstantiation w = refreshStack.peekWidgetInstantiation();
        assert w.widgetState() == widgetState;

        Object value = refreshStack.getIV(type, ifNotProvided);

        if (!refreshStack.peekWidgetInstantiation().directIVs().containsKey(type) &&
                widgetState.parent != null) {
            widgetState.parent.addDescendantInterestedIV(type,
                    value == ifNotProvided ? null : value);
        }

        return value;
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

    void scheduleRefresh() {
        if (refreshScheduled)
            return;
        executor.execute(this::refresh);
        refreshScheduled = true;
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
}
