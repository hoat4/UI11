package ui11.layout.singlechild;

import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;

import java.lang.invoke.StringConcatException;
import java.util.Objects;

public class Sticky extends SubstitutedWidget {

    private final Widget content;

    @Remember private Slot2 contentSlot;

    public Sticky(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected Sticky forSubstitution() {
        return new Sticky(
                contentSlot.with(content)
        );
    }

    public Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content(); // TODO
    }
}
