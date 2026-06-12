package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.observable.Scope;
import ui11.observable.SimpleScope;

class WidgetState<W extends Widget> {

    private final Element element;
    W modelWidget;
    W alsoLockedModelWidget;

    /**
     * ennek az értéke csak state role-ú widget lehet
     */
    W stateWidget;

    final @NonNull WidgetAccessor<W> accessor;

    // TODO ezt majd használatba kéne venni (@Inject mezők megváltozásának ellenőrzéséhez)
    Object[] injectedFieldContents;

    boolean initCalled;
    boolean onResumeCalled;

    private SimpleScope untilPause;

    boolean disposed;

    @SuppressWarnings("unchecked")
    WidgetState(Element element, W modelWidget) {
        this.element = element;
        modelWidget = otherIfSwapped(modelWidget);

        this.modelWidget = modelWidget;

        // ez lehet hogy InvalidWidgetDefinitionException fog dobni
        this.accessor = (WidgetAccessor<W>) modelWidget.accessor();
        assert accessor.clazz() == modelWidget.getClass();

        this.stateWidget = (W) modelWidget.makeCloneToBeStateRole(element);
        assert accessor.clazz() == stateWidget.getClass();

        accessor.checkStateEmptyAndPrepareState(stateWidget, this, modelWidget);

        if (modelWidget.lpModelData != null)
            // ez a végére van hagyva azért, hogyha fent exception történik, akkor ne jegyezzünk be usage-et
            modelWidget.lpModelData.addUsage(this);
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
        prev.disposeFromStateRole(element);

        this.modelWidget = model;

        try {
            @SuppressWarnings("unchecked")
            W newStateWidget = (W) model.makeCloneToBeStateRole(element);
            this.stateWidget = newStateWidget;

            accessor.transferState(prev, this.stateWidget);
        } catch (RuntimeException | Error e) {
            this.stateWidget = null; // félkész state widgetet ne használjunk, mert további bonyodalmakhoz vezet
            throw e;
        }
    }

    public Scope untilPause() {
        if (untilPause == null)
            untilPause = new SimpleScope(Scope.global());
        return untilPause;
    }

    void dispose() {
        stateWidget.disposeFromStateRole(element);
        stateWidget = null; // nem lényeges, csak hogy könnyebben kiderüljön ha valami nem stimmel

        if (modelWidget.lpModelData != null)
            modelWidget.lpModelData.removeUsage(this);
        if (alsoLockedModelWidget != null) {
            alsoLockedModelWidget.lpModelData.removeUsage(this);
            alsoLockedModelWidget = null;
        }

        disposed = true;

        closeUntilPauseScope();
    }

    void closeUntilPauseScope() {
        if (untilPause != null) {
            SimpleScope s = untilPause;
            untilPause = null;
            s.close();
        }
    }

    void retrieveInheritedValues() {
        accessor.retrieveInheritedValues(stateWidget);
    }

    void callInitIfNotCalled() {
        if (!initCalled) {
            stateWidget.initState();
            initCalled = true;
        }
    }

    void callOnResumeIfNotCalled() {
        if (!onResumeCalled) {
            stateWidget.onResume();
            onResumeCalled = true;
        }
    }

    Widget decorateChild(Widget content) {
        return accessor.decorate(stateWidget, content);
    }

    /**
     * Ha még tartozik Element ehhez a WidgetStatehez, akkor visszaadja ezt. Egyébként {@code null}.
     */
    public Element elementIfActive() {
        return disposed ? null : element;
    }

    static abstract class InheritedPropBase<T> {

        final MutableObservable<T> value = MutableObservable.ofNullable();

        final WidgetState<?> widgetState;
        final Class<T> type;
        final boolean optional;
        final String fieldDebugName;

        protected InheritedPropBase(WidgetState<?> widgetState, Class<T> type, boolean optional, String fieldDebugName) {
            this.widgetState = widgetState;
            this.type = type;
            this.optional = optional;
            this.fieldDebugName = fieldDebugName;
        }

        @Nullable
        T retrieveValue() {
            Element e = widgetState.elementIfActive();
            if (e == null)
                throw new IllegalStateException("can't retrieve value, widget state is inactive: " + this + ", " + widgetState);
            T val = e.findInheritedValueForInjection(type, optional, null);
            if (val == null && !optional)
                throw new RuntimeException("internal error, IV has no value (2) but non optional: " +
                        this + ", " + e);
            return val;
        }

        T get() {
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

            Element e = widgetState.elementIfActive();
            if (e == null)
                throw new IllegalStateException("can't retrieve value, widget state is inactive: " + this + ", " + widgetState);
            e.ensureActive();
            T t = value.get();
            if (t == null && !optional)
                throw new RuntimeException("internal error, IV has no value (1) but non optional: " + this + ", " + e);
            return t;
        }

        void update() {
            final T val = retrieveValue();
            value.set(val);
        }
    }

    static final class InheritedProp<T> extends InheritedPropBase<T> implements Observable<T> {

        // ha ennek a konstruktornak a signaturejét megváltoztatjuk, akkor
        // változtassuk meg WidgetAccessorTeaVMPluginben a hivatkozást és a getVariable-t is
        // (nem fog hibát jelezni ha nem tesszük, csak rejtélyes JS hibák fognak megjelenni)
        public InheritedProp(WidgetState<?> widgetState, Class<T> type, boolean optional, String fieldDebugName) {
            super(widgetState, type, optional, fieldDebugName);
        }

        @Override
        public T get() {
            return super.get();
        }

        @Override
        public String toString() {
            return "InheritedProp{" +
                    "value=" + value +
                    ", type=" + type +
                    ", optional=" + optional +
                    ", of " + widgetState + "}";
        }
    }
}
