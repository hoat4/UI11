package ui11.webcontent;

import ui11.SubstitutedWidget;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Objects;

public final class WebContentFrame extends SubstitutedWidget {

    @Nonnull private final URI url;

    public WebContentFrame(@Nonnull URI url) {
        this.url = Objects.requireNonNull(url);
    }

    @Nonnull
    public URI url() {
        return url;
    }

    // lehet hogy URL-nek kell lennie, de akkor kéne csinálni
    // valahova egy resolve függvényt
}
