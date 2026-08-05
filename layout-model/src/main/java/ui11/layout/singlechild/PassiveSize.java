package ui11.layout.singlechild;

import org.jspecify.annotations.NonNull;
import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class PassiveSize extends SubstitutedWidget {

    private final @NonNull Widget content;

    @Remember private Slot contentSlot;

    public PassiveSize(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot();
    }

    @Override
    protected PassiveSize forSubstitution() {
        return new PassiveSize(
                contentSlot.with(content)
        );
    }

    public @NonNull Widget content() {
        return content;
    }
}
