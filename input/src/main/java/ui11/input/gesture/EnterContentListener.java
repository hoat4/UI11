package ui11.input.gesture;

import org.jspecify.annotations.NonNull;
import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.input.focus.FocusHolder;
import ui11.input.keyboard.KeyCombination;

import java.awt.datatransfer.Transferable;
import java.util.Objects;
import java.util.function.Consumer;

public final class EnterContentListener extends SubstitutedWidget {

    private final @NonNull Consumer<EnterContent> enterContentHandler;
    private final @NonNull FocusHolder focusHolder;
    private final @NonNull Widget content;

    public EnterContentListener(@NonNull Consumer<EnterContent> enterContentHandler,
                                @NonNull FocusHolder focusHolder,
                                @NonNull Widget content) {
        this.enterContentHandler = listenerProxy(Objects.requireNonNull(enterContentHandler));
        this.focusHolder = Objects.requireNonNull(focusHolder);
        this.content = Objects.requireNonNull(content);
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull Consumer<EnterContent> enterContentHandler() {
        return enterContentHandler;
    }

    public @NonNull FocusHolder focusHolder() {
        return focusHolder;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content;
    }

    public record EnterContent(Transferable transferable,
                               EnterContentSource enterContentSource) {

        public interface EnterContentSource {}

        // fizikai billentyűt/billentyűzetet is fel kéne tüntetni
        public record KeyboardEnterContentSource(KeyCombination keyCombination, boolean repeat)
                implements EnterContentSource {}

        public record ClipboardEnterContentSource() implements EnterContentSource {}
    }
}