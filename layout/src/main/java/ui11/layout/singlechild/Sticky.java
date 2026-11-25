package ui11.layout.singlechild;

import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class Sticky extends SubstitutedWidget {

    private final Widget content;

    public Sticky(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected Sticky forSubstitution() {
        return new Sticky(
                withID("content", content)
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
