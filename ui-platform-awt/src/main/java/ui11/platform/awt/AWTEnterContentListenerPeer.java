package ui11.platform.awt;

import ui11.Widget;
import ui11.input.focus.FocusRoot;
import ui11.input.gesture.EnterContentListener;
import ui11.input.gesture.EnterContentListener.EnterContent;
import ui11.input.gesture.EnterContentListener.EnterContent.KeyboardEnterContentSource;
import ui11.observable.Observable;

import java.awt.datatransfer.StringSelection;
import java.util.function.Consumer;

public class AWTEnterContentListenerPeer extends Widget {

    private final EnterContentListener widget;

    @Inject private Observable<AWTWindow> window;
    @Inject private Observable<FocusRoot> focusRoot;

    @State private AWTEnterContentListenerPeerState state;

    public AWTEnterContentListenerPeer(EnterContentListener widget) {
        this.widget = widget;
    }

    @Override
    protected void initState() {
        state = new AWTEnterContentListenerPeerState();
    }

    @Override
    protected Widget build() {
        // TODO window observable
        state.enterContentListener = widget.enterContentHandler();

        AWTWindow window = this.window.get(); // untilNextRebuild.onCloseban már túl késő window.get()-et hívni
        Observable.of(() -> focusRoot.get().isFocused(widget.focusHolder())).getAndSubscribe(focused -> {
            if (focused)
                window.enterContentListenerPeer = state;
            else if (window.enterContentListenerPeer == state)
                window.enterContentListenerPeer = null;
        }, untilNextRebuild());
        untilNextRebuild().onClose(() -> {
            if (window.enterContentListenerPeer == state)
                window.enterContentListenerPeer = null;
        });

        return widget.content();
    }

    static class AWTEnterContentListenerPeerState {

        private Consumer<EnterContent> enterContentListener;

        void handleKeyTyped(String s, KeyboardEnterContentSource keyboardEnterContentSource) {
            enterContentListener.accept(new EnterContent(new StringSelection(s), keyboardEnterContentSource));
        }
    }
}