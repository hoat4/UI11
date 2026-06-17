package ui11.text;

import ui11.ErrorWidgetFactory;
import ui11.Widget;

import static ui11.text.TextModifiers.withLineWrapping;

public class TextErrorWidgetFactory implements ErrorWidgetFactory {

    @Override
    public Widget makeDelegateCreationError(Throwable t) {
        // TODO kéne detektálni, ha ennek a delegatecreationje se sikerül
        return withLineWrapping(new Text(t.toString()));
    }
}
