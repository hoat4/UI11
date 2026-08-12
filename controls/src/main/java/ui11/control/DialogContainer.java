package ui11.control;

import ui11.Widget;
import ui11.input.gesture.CloseRequestListener;
import ui11.observable.ObservableMap;
import ui11.observable.SimpleScope;
import ui11.provide.Provide;

import java.util.LinkedHashMap;
import java.util.Objects;

import static ui11.graphics.effect.Overlay.overlay;

public class DialogContainer extends Widget {

    private final DialogContainerState state;
    private final Widget content;

    public DialogContainer(DialogContainerState state, Widget content) {
        this.state = state;
        this.content = content;
    }

    @Override
    protected Widget build() {
        return overlay(overlay -> {
            overlay.accept(content);
            state.overlays.forEach((key, content) -> {
                overlay.accept(withID("dialogContent", key, content));
            });
        });
    }

    @Provide
    private DialogContainerState dialogContainerState() {
        return state;
    }

    public static class DialogContainerState {

        private final ObservableMap<Object, Widget> overlays = ObservableMap.wrap(new LinkedHashMap<>());

        public void open(Widget dialog, SimpleScope scope) {
            Objects.requireNonNull(dialog, "DCS.o");

            dialog = new CloseRequestListener(scope::close, dialog);

            Object key = new Object();
            overlays.put(key, dialog);
            scope.onClose(() -> overlays.remove(key));
        }

        public boolean hasShownDialogs() {
            return !overlays.isEmpty();
        }
    }
}
