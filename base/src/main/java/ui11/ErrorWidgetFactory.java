package ui11;

// TODO ez lehetne inkább egy widget

/**
 * Megjeleníthető tartalmat (pl. hibaüzenetet) biztosít olyan widgetnek, ami exceptiont dobott,
 * amikor a tartalmát próbálta felépíteni.
 */
public interface ErrorWidgetFactory {

    Widget makeDelegateCreationError(Throwable t);
}
