package ui11.control;

import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.net.URI;
import java.util.Objects;

public final class Hyperlink extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @NonNull URI target;

    @Inject private Slot contentSlot;

    public Hyperlink(@NonNull Widget content, @NonNull URI target) {
        this.content = Objects.requireNonNull(content);
        this.target = Objects.requireNonNull(target);
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    public @NonNull URI target() {
        return target;
    }
}
