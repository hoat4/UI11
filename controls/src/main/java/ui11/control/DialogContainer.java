package ui11.control;

import ui11.Widget;
import ui11.animation.Scheduler;
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

    @Inject private Scheduler scheduler;

    public DialogContainer(DialogContainerState state, Widget content) {
        this.state = state;
        this.content = content;
    }

    @Override
    protected Widget build() {
        state.scheduler = scheduler;
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

        private Scheduler scheduler;
        private final ObservableMap<Object, Widget> overlays = ObservableMap.wrap(new LinkedHashMap<>());

        public void open(Widget dialog, SimpleScope scope) {
            Objects.requireNonNull(dialog, "DCS.o");
            Objects.requireNonNull(scope, "DCS.s");

            dialog = new CloseRequestListener(scope::close, dialog);

            Object key = new Object();
            overlays.put(key, dialog);
            scope.onClose(() -> {
                // TODO remove this workaround
                // This runLater is needed because if a widget opened a dialog with a untilPause() scope,
                // then that widget is removed from the DialogContainer's content,
                // the untilPause scope will be only closed after the DialogContainer has been built.
                scheduler.runLater(() -> {
                    overlays.remove(key);
                });
            });
        }

        /**
         * TODO ez most a runLateres workaround miatt késik
         */
        public boolean hasShownDialogs() {
            return !overlays.isEmpty();
        }
    }
}
