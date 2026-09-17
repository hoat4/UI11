package ui11;

import org.jspecify.annotations.NonNull;
import ui11.observable.MutableObservable;

// név kicsit fura, de Flutterben is így hívják: https://api.flutter.dev/flutter/widgets/GlobalKey-class.html
final class GlobalKey {

    final MutableObservable<Widget> content = MutableObservable.ofNullable();
    boolean used;

    // ennek csak ezért azért nem lehet rögtön értéket adni, mert WidgetTree-t nem ismerjük
    private WidgetState<GlobalKeyWidgetImpl> widgetState;

    private WidgetState<?> debug_w;
    private Object debug_k1, debug_k2;

    GlobalKey(WidgetState<?> debug_w, Object debug_k1, Object debug_k2) {
        this.debug_w = debug_w;
        this.debug_k1 = debug_k1;
        this.debug_k2 = debug_k2;
    }

    @NonNull Widget wrap(@NonNull Widget widget) {
        // TODO detektálni kéne, ha egy refresh cycle-n belül 2 eltérő widgetet is próbálnak belerakni?
        content.set(widget);
        return new GlobalKeyWidget();
    }

    // WidgetTree.findOrCreateWidgetState-ben special case-elve van ez a widget, hogyha ilyet
    // talál, akkor ignorálja a previous WidgetInstantiationt és a KeyWrappereket is
    final class GlobalKeyWidget extends Widget {

        WidgetState<?> replacement(WidgetTree tree) {
            if (widgetState == null)
                widgetState = new WidgetState<>(new GlobalKeyWidgetImpl(), tree);
            else if (widgetState.tree != tree)
                throw new RuntimeException(ui11.GlobalKey.class.getSimpleName() + " reused for different tree: " +
                        tree);
            return widgetState;
        }

        @Override
        protected Widget build() {
            throw new RuntimeException("should not reach here (GKW.b)");
        }

        @Override
        public String toString() {
            // TODO Widget.toString exceptionök?
            return "GlobalKeyWidget{key=" + ui11.GlobalKey.this + ", content=" + content.snoop() + "}";
        }
    }

    final class GlobalKeyWidgetImpl extends Widget {

        @Override
        protected Widget build() {
            Widget w = content.get();
            if (w == null)
                throw new RuntimeException(/* TODO "Content has been removed"*/);
            return w;
        }

        @Override
        public String toString() {
            return GlobalKeyWidgetImpl.class.getSimpleName() + "{w=" + debug_w +
                    ", k1=" + debug_k1 + ", k2=" + debug_k2 + "}";
        }
    }
}
