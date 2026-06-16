package ui11;

import ui11.Element.InheritedValueHolder.IVUsage;
import ui11.observable.ObserverHolder;
import ui11.resolution.GlobalViewProviders;
import ui11.resolution.WidgetResolver;

import java.util.*;
import java.util.concurrent.Executor;

/**
 * The container for all content in a widget tree. This class is not usually used by applications, instead it is used by
 * platform-specific windowing system and rendering implementation modules.
 */
public final class WidgetTree {

    private final RootElement rootElement;

    private WidgetTree(RootElement rootElement) {
        this.rootElement = rootElement;
    }

    public static WidgetTree create(Widget root, Executor executor) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(executor);

        RootElement rootElement = new RootElement(root, executor);
        rootElement.start();
        return new WidgetTree(rootElement);
    }

    // TODO milyen API kéne ide?
    //      pl. valami dispose-szerűség, illetve rootot lookupolni?

    static class RootElement extends Element {

        private final Executor executor;

        private List<Element> toBeDisposed;
        private final Map<Class<?>, Object> ivDefaultValues = new HashMap<>();

        private RootElement(Widget root, Executor executor) {
            // ha rootnak widgetproxy-t akarunk megadni (Provider, KeyWrapper) vagy EndingWidgetet, akkor
            // be kell wrappelni egy widgetbe

            this.executor = executor;
            // setWidget csak regular widgetet tud fogadni, ezért wrappelni kell a rootot
            setWidget(new RootWidgetWrapper(root));
        }


        void start() {
            if (elementState != ElementState.INITIAL && elementState != ElementState.STOPPED)
                throw new IllegalStateException();
            elementState = ElementState.START_REQUESTED;
            ObserverHolder.withoutObserver(this::refreshAndDisposeStoppableElements);
        }

        void requestRootRefresh() {
            executor.execute(this::refreshAndDisposeStoppableElements);
        }

        private void refreshAndDisposeStoppableElements() {
            try {
                toBeDisposed = new ArrayList<>();

                // TODO itt kéne ObserverHolder.withoutObserver?
                refresh();

                for (Element e : toBeDisposed) {
                    if (e.elementState == ElementState.IDLE_STOPPABLE || e.elementState == ElementState.REFRESH_REQUESTED_STOPPABLE) {
                        e.inactivate();
                    }
                }
            } catch (Throwable e) {
                // TODO ilyenkor mi legyen toBeDisposeddel?
                logger.error("Failed refresh on " + this, e);
            } finally {
                toBeDisposed = null;
            }
        }

        @Override
        Object findInheritedValue(Class<?> type, IVUsage usage) {
            if (type == WidgetResolver.class)
                return GlobalViewProviders.instance();

            if (SupplyDefaultInstanceHelper.shouldUseDefaultInstance(type))
                return ivDefaultValues.computeIfAbsent(type, SupplyDefaultInstanceHelper::createDefaultInstance);
            else
                return IVNotProvided.IV_NOT_PROVIDED;
        }

        void submitForDispose(Element e) {
            toBeDisposed.add(e);
        }
    }

    private static class RootWidgetWrapper extends Widget {

        private final Widget content;

        public RootWidgetWrapper(Widget content) {
            this.content = content;
        }

        @Override
        protected Widget build() {
            return content;
        }
    }


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
