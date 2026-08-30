package ui11;

/**
 * A widget that represents that an exception occured while building another widget.
 * <p>
 * The appearance can be controlled via {@link ResolverRegistry}, as in case of any {@linkplain SubstitutedWidget}.
 */
public final class ErrorWidget extends SubstitutedWidget {

    private final Throwable throwable;

    public ErrorWidget(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable throwable() {
        return throwable;
    }
}
