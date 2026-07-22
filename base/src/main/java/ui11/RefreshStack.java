package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.*;

final class RefreshStack {

    private final Deque<Item> stack = new LinkedList<>();
    private final Map<Class<?>, Deque<IVValueWrapper>> ivs = new HashMap<>();

    RefreshStack(@NonNull WidgetInstantiation root) {
        Objects.requireNonNull(root);
        stack.push(new Item(null, -100 /* érvénytelen érték */, root, null));
    }

    boolean isEmpty() {
        boolean empty = stack.isEmpty();
        assert !empty || ivs.values().stream().allMatch(Deque::isEmpty);
        return empty;
    }

    boolean isRoot() {
        return stack.size() == 1;
    }

    @NonNull WidgetState<?> peekWidget() throws NoSuchElementException {
        return stack.element().widgetInstantiation.child();
    }

    @NonNull WidgetInstantiation peekWidgetInstantiation() {
        return stack.element().widgetInstantiation;
    }

    IVValueWrapper getIV(Class<?> type) {
        Deque<IVValueWrapper> ivStack = ivs.get(type);
        if (ivStack == null || ivStack.isEmpty())
            return null;
        else {
            IVValueWrapper result = ivStack.element();
            assert result.value == null || type.isInstance(result.value);
            return result;
        }
    }

    boolean ivsMatch(Map<Class<?>, @Nullable Object> b) {
        for (Map.Entry<Class<?>, Object> entry : b.entrySet()) {
            IVValueWrapper actual = getIV(entry.getKey());
            // TODO ez a null jó?
            if (!Objects.equals(actual == null ? null : actual.value, entry.getValue()))
                return false;
        }
        return true;
    }

    void push(@NonNull WidgetState<?> parent, int childIndex, @NonNull WidgetInstantiation child) {
        ResolutionRequestCollection inheritedReqs = stack.element().computedReqs;
        assert inheritedReqs != null;
        stack.push(new Item(parent, childIndex, child, inheritedReqs));
    }

    void pushIVs(@NonNull WidgetState<?> expectedW, @NonNull Map<Class<?>, IVValueWrapper> ivs) {
        Item item = stack.element();
        assert item.widgetInstantiation.child() == expectedW;
        assert item.pushedIVs == null;
        item.pushedIVs = ivs;
        ivs.forEach((type, val) -> {
            this.ivs.computeIfAbsent(type, __ -> new ArrayDeque<>()).push(val);
        });
    }

    void setComputedReqs(@NonNull WidgetState<?> expectedW, @NonNull ResolutionRequestCollection reqColl) {
        Item item = stack.element();
        assert item.widgetInstantiation.child() == expectedW;
        assert item.computedReqs == null;
        item.computedReqs = reqColl;
    }

    ResolutionRequestCollection inheritedReqs() {
        ResolutionRequestCollection inheritedReqs = stack.element().inheritedReqs;
        assert inheritedReqs != null || isRoot();
        return inheritedReqs;
    }

    @NonNull ResolutionRequestCollection computedReqs() {
        ResolutionRequestCollection computedReqs = stack.element().computedReqs;
        assert computedReqs != null;
        return computedReqs;
    }

    @NonNull Item pop() {
        Item item = stack.pop();
        item.pushedIVs.forEach((type, val) -> {
            IVValueWrapper popped = ivs.get(type).pop();
            assert popped == val;
        });
        return item;
    }

    public String toDebugString() {
        if (stack.isEmpty())
            return "EMPTY";
        StringBuilder sb = new StringBuilder();
        for (Item item : stack) {
            sb.append("\n- ").append(item.widgetInstantiation.child().stateWidget.getClass().getName());
            sb.append(" (needsRebuild=").append(item.needsRebuild).append(')');
            item.widgetInstantiation.directIVs().forEach((type, val) -> {
                sb.append("\n    ").append(ReflectionUtil.simpleName(type)).append(" = ").append(val);
            });
            if (item.widgetInstantiation.directReq() != null)
                sb.append("\n    New req: ").append(item.widgetInstantiation.directReq());
            for (ResolutionRequest<?> req : item.widgetInstantiation.directCompletedRequests())
                sb.append("\n    Completed req: ").append(req);
        }
        return sb.toString();
    }

    void setDebugValuesOfCurrentWidget(boolean needsRebuild) {
        Item top = stack.element();
        top.needsRebuild = needsRebuild;
    }

    static final class Item {

        // az itteni parent helyett child.widgetState().parent nem jó,
        // mert WidgetState.parent lehet hogy még nincs beállítva

        public final @Nullable WidgetState<?> parent;
        public final int childIndex;
        public final @NonNull WidgetInstantiation widgetInstantiation;
        /**
         * csak root esetén null
         */
        public final @Nullable ResolutionRequestCollection inheritedReqs;

        private Boolean needsRebuild;

        private Map<Class<?>, IVValueWrapper> pushedIVs;
        ResolutionRequestCollection computedReqs;

        /**
         * @param inheritedReqs csak root esetén null
         */
        Item(@Nullable WidgetState<?> parent,
             int childIndex,
             @NonNull WidgetInstantiation widgetInstantiation,
             @Nullable ResolutionRequestCollection inheritedReqs) {
            this.parent = parent;
            this.childIndex = childIndex;
            this.widgetInstantiation = widgetInstantiation;
            this.inheritedReqs = inheritedReqs;
        }

        @Override
        public String toString() {
            // debuggerhez hasznos
            return widgetInstantiation.child().modelWidget.toString();
        }
    }

    static class IVValueWrapper {

        final @Nullable Object value;

        /**
         * ha {@code null}, az azért van nem kell rá feliratkozni, mert mergeölt érték ami
         * ancestorból jött és ott már feliratkoztunk rá
         */
        final @Nullable WidgetInstantiation origin;
        final boolean isFromDescendant;

        IVValueWrapper(@Nullable Object value, @Nullable WidgetInstantiation origin, boolean isFromDescendant) {
            this.value = value;
            this.origin = origin;
            this.isFromDescendant = isFromDescendant;
        }
    }
}
