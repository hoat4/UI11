package ui11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Element.InheritedValueHolder.IVUsage;
import ui11.observable.ObserverHolder;
import ui11.resolution.GlobalViewProviders;
import ui11.resolution.WidgetResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * The container for all content in a widget tree.
 */
// TODO név
public final class RootElement extends Element {

    private static final Logger logger = LoggerFactory.getLogger(RootElement.class);

    private final Component root;
    private final Executor executor;

    private List<Element> toBeDisposed;
    private final Map<Class<?>, Object> ivDefaultValues = new HashMap<>();

    public RootElement(Component root, Executor executor) {
        this.root = root;
        this.executor = executor;
    }

    public void start() {
        if (elementState != ElementState.INITIAL && elementState != ElementState.STOPPED)
            throw new IllegalStateException();
        elementState = ElementState.START_REQUESTED;
        ObserverHolder.withoutObserver(this::refreshAndDisposeStoppableElements);
    }

    public void stop() {
        throw new RuntimeException("TODO");
    }

    @Override
    Widget build() {
        return root;
    }

    void submitTask(Runnable task) {
        executor.execute(task);
    }

    void requestRootRefresh() {
        submitTask(this::refreshAndDisposeStoppableElements);
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

    @Override
    Class<?> modelType() {
        return null;
    }

    @Override
    boolean updateUserVisibleModel() {
        return false;
    }
}
