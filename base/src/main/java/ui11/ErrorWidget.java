package ui11;

/**
 * Megjeleníthető tartalmat (pl. hibaüzenetet) biztosít olyan widgetnek, ami exceptiont dobott,
 * amikor a tartalmát próbálta felépíteni.
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
