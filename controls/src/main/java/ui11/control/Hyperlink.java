package ui11.control;

import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.net.URI;
import java.util.Objects;

public final class Hyperlink extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @NonNull URI target;

    @Remember private Slot2 contentSlot;

    public Hyperlink(@NonNull Widget content, @NonNull URI target) {
        this.content = Objects.requireNonNull(content);
        this.target = Objects.requireNonNull(target);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected Hyperlink forSubstitution() {
        return new Hyperlink(
                contentSlot.with(content),
                target
        );
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull URI target() {
        return target;
    }
}
