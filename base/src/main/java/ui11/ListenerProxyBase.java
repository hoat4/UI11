package ui11;

import java.util.*;
import java.util.function.Consumer;

// Ha egy widgetnek a nem-listener input fieldjei közül nem változott meg egy se, de egy listener mező igen,
// akkor akkor lehet rebuild nélkül átvinni a listenert az régibe, ha nincs még a ListenerProxyBase más state role-ú
// widgetben használva.

// néhány példa eset:
// A: 1	A=1
// A: 2	A=1, 1.replacement=2, 2.replacement=1
// B: 1	B=2
// C: 2	C=1
// A: 3	A=3
//
// A: 1	A=1
// A: 2	A=1, 1.replacement=2, 2.replacement=1
// B: 1	B=2
// A: 3	A=1, 1.replacement=3, 2.replacement=1, 3.replacement=1
// ez utóbbi nem jó, tehát csak akkor szabad engedni ha 1 és 2 is szabad
//
// A: 1	A=1
// A: 2	A=1, 1.replacement=2, 2.replacement=1
// A: 3	A=1, 1.replacement=3, 3.replacement=1
// B: 3	B=1, 1.replacement=3, 3.replacement=1

abstract class ListenerProxyBase<L> {

    /**
     * ez azért kell, hogy amikor végigmegyünk az input field értékeken, akkor tudjuk, hogy kell-e foglalkozni az illető
     * mezővel
     */
    private final Widget modelWidget;
    private final L initialValue;

    private int inputField = -1;

    public ListenerProxyBase(Widget modelWidget, L initialValue) {
        this.modelWidget = modelWidget;
        this.initialValue = initialValue;
    }

    boolean init(Widget modelWidget, int inputFieldID) {
        if (this.modelWidget != modelWidget)
            return false;
        if (this.inputField != -1)
            throw new IllegalStateException("LPB3 init");
        this.inputField = inputFieldID;
        return true;
    }

    protected L currentValue() {
        if (modelWidget.lpModelData == null || inputField == -1)
            throw new IllegalStateException("LPB3 " + inputField);

        if (modelWidget.lpModelData.replacement == null)
            return initialValue;
        else {
            @SuppressWarnings("unchecked")
            ListenerProxyBase<L> proxy = (ListenerProxyBase<L>)
                    modelWidget.accessor().readNonPrimitiveInputField(modelWidget.lpModelData.replacement, inputField);
            return proxy.initialValue;
        }
    }

    public boolean isOwnedBy(Widget model) {
        return this.modelWidget == model;
    }

    /**
     * ez az initial value-t hasonlítja össze, nem az effective value-t
     */
    public boolean hasSameValue(ListenerProxyBase<?> other) {
        return initialValue == other.initialValue;
    }

    static class RunnableListenerProxy extends ListenerProxyBase<Runnable> implements Runnable {

        public RunnableListenerProxy(Widget modelWidget, Runnable initialValue) {
            super(modelWidget, initialValue);
        }

        @Override
        public void run() {
            currentValue().run();
        }
    }

    static class ConsumerListenerProxy<T> extends ListenerProxyBase<Consumer<T>> implements Consumer<T> {

        public ConsumerListenerProxy(Widget modelWidget, Consumer<T> initialValue) {
            super(modelWidget, initialValue);
        }

        @Override
        public void accept(T t) {
            currentValue().accept(t);
        }
    }

    /**
     * Listener proxy data for model-role widget instances.
     */
    static class LPModelData {

        private final Widget thisModel;

        private final Set<WidgetState<?>> usages = Collections.newSetFromMap(new IdentityHashMap<>()); // TODO weak ref

        /**
         * model szerepű widget lehet csak, state nem
         */
        public Widget replacement;

        public LPModelData(Widget thisModel) {
            this.thisModel = thisModel;
        }

        void addUsage(WidgetState<?> widgetState) {
            verify();
            if (!usages.add(widgetState))
                throw new RuntimeException("already added to " + this + ": " + widgetState);
            verify();
        }

        void removeUsage(WidgetState<?> widgetState) {
            verify();
            if (!usages.remove(widgetState))
                throw new RuntimeException("not in usage list of " + this + ": " + widgetState);
            if (usages.isEmpty() && replacement != null && replacement.lpModelData.usages.isEmpty()) {
                replacement.lpModelData.replacement = null;
                replacement = null;
            }
            verify();
        }

        private void verify() {
            if (usages.isEmpty() && replacement != null && replacement.lpModelData.usages.isEmpty() ||
                    replacement != null && replacement.lpModelData.replacement.lpModelData != this)
                throw new RuntimeException();
        }

        boolean trySwapWith(Widget otherModel, WidgetState<?> user) {
            verify();

            if (!usages.contains(user) || !otherModel.lpModelData.usages.contains(user))
                throw new IllegalStateException();

            if (!usages.equals(Set.of(user)) || !otherModel.lpModelData.usages.equals(Set.of(user)))
                // vannak más widgetstateek is, akiket meg összezavarna a csere
                return false;

            if (replacement != null || otherModel.lpModelData.replacement != null)
                // bizonyos esetekben talán lehetne, de nem tudom átlátni ezt
                return false;

            replacement = otherModel;
            otherModel.lpModelData.replacement = thisModel;
            verify();

            return true;
        }
    }
    // TODO concurrent
}
