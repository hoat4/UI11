package ui11.platform.dom.peers;

import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLInputElement;
import ui11.Widget;
import ui11.control.PlainTextEditor;
import ui11.platform.dom.DOMPeerBase;

public class DOMEditableTextPeer extends DOMPeerBase<HTMLInputElement> {

    private final PlainTextEditor plainTextEditor;

    @Remember private boolean focusRequested;

    public DOMEditableTextPeer(PlainTextEditor plainTextEditor) {
        this.plainTextEditor = plainTextEditor;
    }

    @Override
    protected String elementName() {
        return "input";
    }

    @Override
    protected void initElement() {
        elem().getClassList().add("et"); // "editable text"
    }

    @Override
    protected Widget doBuild() {
        untilNextRebuild().onClose(elem().onInput(evt -> {
            String s = elem().getValue();
            PlainTextEditor textField = plainTextEditor;
            if (textField.editablePlainText().maxLength != null && s.length() > textField.editablePlainText().maxLength) {
                s = s.substring(0, textField.editablePlainText().maxLength);
                elem().setValue(s);
            }
            textField.editablePlainText().set(s);
        })::dispose);
        untilNextRebuild().onClose(elem().onKeyDown(evt -> {
            // KeyboardEvent.key supportált elvileg azokon a böngészőkön, ahol grid is supportált
            if (plainTextEditor.onAction() != null && ((KeyboardEvent) evt).getKey().equals("Enter"))
                plainTextEditor.onAction().run();
        })::dispose);

        if (plainTextEditor.editablePlainText().maxLength == null)
            elem().removeAttribute("maxlength");
        else
            elem().setAttribute("maxlength", Integer.toString(plainTextEditor.editablePlainText().maxLength));
        elem().setValue(plainTextEditor.editablePlainText().get());

        // TODO focus-t normálisan. most nem is nagyon működik
        if (!focusRequested && plainTextEditor.focusHolder() != null && plainTextEditor.focusHolder().autofocus) {
            env().window.setTimeout(elem()::focus, 0);
            untilPause().onClose(() -> focusRequested = false);
            focusRequested = true;
        }
        return endingWidget();
    }
}
