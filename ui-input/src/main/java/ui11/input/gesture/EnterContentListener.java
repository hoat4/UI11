package ui11.input.gesture;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.input.focus.FocusHolder;
import ui11.input.keyboard.KeyCombination;

import javax.annotation.Nonnull;
import java.awt.datatransfer.Transferable;
import java.util.Objects;
import java.util.function.Consumer;

public final class EnterContentListener extends SubstitutedWidget {

    @Listener @Nonnull private final Consumer<EnterContent> enterContentHandler;
    @Nonnull private final FocusHolder focusHolder;
    @Nonnull private final Widget content;

    public EnterContentListener(@Nonnull Consumer<EnterContent> enterContentHandler,
                                @Nonnull FocusHolder focusHolder,
                                @Nonnull Widget content) {
        this.enterContentHandler = Objects.requireNonNull(enterContentHandler);
        this.focusHolder = Objects.requireNonNull(focusHolder);
        this.content = Objects.requireNonNull(content);
    }

    @Nonnull
    public Widget content() {
        return content;
    }

    @Nonnull
    public Consumer<EnterContent> enterContentHandler() {
        return enterContentHandler;
    }

    @Nonnull
    public FocusHolder focusHolder() {
        return focusHolder;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
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