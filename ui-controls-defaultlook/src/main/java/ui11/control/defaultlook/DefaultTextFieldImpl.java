package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.PlainTextEditor;
import ui11.control.TextField;
import ui11.decoration.Box;
import ui11.graphics.fill.Color;
import ui11.input.focus.FocusHolder;
import ui11.input.focus.FocusRoot;
import ui11.input.pointer.Pointer;
import ui11.input.pointer.Pointer.Button;
import ui11.input.pointer.PointerRegion;
import ui11.layout.singlechild.Padding;
import ui11.observable.Observable;

import javax.annotation.Nullable;

import static ui11.geom.Length.em;
import static ui11.geom.Length.px;

// TODO általánosítás egyéb input fieldekre
public class DefaultTextFieldImpl extends Widget {

    private final TextField textField;

    @Inject private Observable<FocusRoot> focusRoot;

    // TODO focus delegation: ha inputField.editablePlainText fókuszálva lesz, akkor a TextField is legyen fókuszálva
    //      tehát ezt a focusHoldert át kéne adnunk valahogy inputField.content()-nek
    @State private FocusHolder focusHolder;

    public DefaultTextFieldImpl(TextField textField) {
        this.textField = textField;
    }

    @Override
    protected void initState() {
        focusHolder = new FocusHolder();
    }

    @Override
    protected Widget build() {
        Widget content = new PlainTextEditor(textField.text(), textField.onAction(), focusHolder);
        content = new Box(Padding.allSides(em(.2), content)).
                withMinSize(em(10), em(1)).
                withBorder(px(1), Color.BLACK).
                withBackground(focusRoot.get().isFocused(focusHolder) ? Color.WHITE : Color.LIGHTGRAY);
        // most ha a nem a keretre kattintunk, akkor DefaultPlainTextEditorImpl "elfogja" a kattintást
        // és ő maga fókuszálja a focusHoldert. ez ugyan működik, de ki kéne találni értelmesebb megoldást rá.
        content = new PointerRegion(content) {
            @Nullable
            @Override
            public PointerListener onPointerDown(Pointer pointer, Button button) {
                focusRoot.get().requestFocus(focusHolder);
                // ha lenyomott egér mozgatása közben változik meg a focusRoot, akkor mit csináljunk?
                return null;
            }

            @Override
            public void onPointerMove(Pointer pointer, boolean inside) {
            }
        };
        return content;
    }
}
