package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLInputElement;
import ui11.Widget;
import ui11.control.CheckBox;
import ui11.layout.singlechild.Align;
import ui11.observable.Observable;
import ui11.platform.dom.DOMElementWidget;
import ui11.platform.dom.DOMEnvironment;
import ui11.platform.dom.HTMLElementHint;

import static ui11.css.CSSClassTag.cssClass;
import static ui11.layout.multichild.LinearLayout.row;
import static ui11.layout.multichild.LinearLayout.withWeight;

public class DOMCheckBoxPeer extends Widget {

    private final CheckBox checkBox;

    @Inject private Observable<DOMEnvironment> env;

    @State private HTMLInputElement nativeCheckbox;

    public DOMCheckBoxPeer(CheckBox checkBox) {
        this.checkBox = checkBox;
    }

    @Override
    protected void initState() {
        // TODO observable olvasása innen
        nativeCheckbox = (HTMLInputElement) env.get().document.createElement("input");
        nativeCheckbox.setType("checkbox");
    }

    @Override
    protected Widget build() {
        // TODO ezt csak egyszer kéne
        untilNextRebuild().onClose(nativeCheckbox.onInput(evt -> {
            checkBox.value().set(nativeCheckbox.isChecked());
        })::dispose);

        nativeCheckbox.setChecked(checkBox.value().get());
        nativeCheckbox.setDisabled(checkBox.disabled());

        if (checkBox.graphic() == null)
            return new DOMElementWidget(nativeCheckbox);
        else
            return new HTMLElementHint("label",
                    row(
                            new DOMElementWidget(nativeCheckbox),
                            withWeight(1, Align.leftCenter(
                                    cssClass("checkbox-graphic", checkBox.graphic())
                            ))
                    )
            );
    }
}
