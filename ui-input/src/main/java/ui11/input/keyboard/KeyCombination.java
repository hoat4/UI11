package ui11.input.keyboard;

import java.util.Set;

public record KeyCombination(Set<Modifier> modifiers, KeySymbol keySymbol) {

    public enum Modifier {
        CONTROL, ALT, SHIFT
        // TODO AltGr, Meta?
    }
}
