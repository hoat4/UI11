package ui11.webcontent;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;

import java.net.URI;
import java.util.Objects;

public final class WebContentFrame extends SubstitutedWidget {

    private final @NonNull URI url;

    public WebContentFrame(@NonNull URI url) {
        this.url = Objects.requireNonNull(url);
    }

    public @NonNull URI url() {
        return url;
    }

    // lehet hogy URL-nek kell lennie, de akkor kéne csinálni
    // valahova egy resolve függvényt
}
