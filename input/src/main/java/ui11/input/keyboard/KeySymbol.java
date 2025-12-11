package ui11.input.keyboard;

/**
 * Billentyűt azonosító szimbólum. Tehát {@link TextSymbol betű/szám/irásjel} vagy
 * {@link FunctionSymbol funkcióbillentyű}.
 */
public sealed interface KeySymbol {

    record TextSymbol(String text) implements KeySymbol {
    }

    non-sealed interface FunctionSymbol extends KeySymbol {
    }

    enum StandardFunctionSymbol implements FunctionSymbol {
        LEFT, RIGHT, UP, DOWN, BACKSPACE, DELETE, ENTER, ESCAPE,
        F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, INSERT, PAGE_UP, PAGE_DOWN, HOME, END, F12
    }
}
