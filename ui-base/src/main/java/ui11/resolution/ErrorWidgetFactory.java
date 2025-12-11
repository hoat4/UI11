package ui11.resolution;

// TODO ez lehetne inkább egy widget

import ui11.Widget;

/**
 * Megjeleníthető tartalmat (pl. hibaüzenetet) biztosít olyan widgetnek, ami exceptiont dobott,
 * amikor a tartalmát próbálta felépíteni.
 */
public interface ErrorWidgetFactory {

    Widget makeDelegateCreationError(Throwable t);
}
